package actors

import akka.Done
import akka.NotUsed
import akka.actor.*
import akka.event.Logging
import akka.event.LoggingAdapter
import akka.http.javadsl.model.HttpResponse
import akka.http.javadsl.model.ws.TextMessage
import akka.http.javadsl.model.ws.WebSocketUpgrade
import akka.japi.function.Function2
import akka.japi.pf.DeciderBuilder
import akka.routing.DefaultResizer
import akka.routing.SmallestMailboxPool
import akka.stream.Materializer
import akka.stream.javadsl.Flow
import akka.stream.javadsl.Sink
import akka.stream.javadsl.Source
import akka.stream.testkit.TestPublisher
import com.google.gson.Gson
import connectionpool.DBMessageConnector
import connectionpool.Group
import connectionpool.User
import messages.*
import java.time.Duration
import java.util.concurrent.CompletionStage


/**
 * Main Actor of the Server
 * Receives all the messages from the Websockets and Managers and either handles them (if directed to the server)
 * or forwards them to the correct actor (if present) otherwise the message gets stored (if user or group exists in db)
 */
class ServerActor : UntypedAbstractActor() {
    private val actors = HashMap<String, ActorRef>()
    private var connections: DBMessageConnector = DBMessageConnector()
    private val retryCnt = 3
    private val log: LoggingAdapter = Logging.getLogger(context.system, this)

    // Supervision Strategy for the DatabasePoolActors
    private var dbStrategy: SupervisorStrategy? = OneForOneStrategy(
        3,
        Duration.ofMinutes(1),
        DeciderBuilder
                // if DBActorException is thrown check if retrycnt <= 0 if that's the case escalate Error otherwise resume
            .match(DBActorException::class.java) {
                log2.error("Exception caught by DBActor Supervisor Strategy cause ${it.exception.message} ${it.msg.retryCnt}")
                if (it.msg.retryCnt <= 0) {
                    SupervisorStrategy.escalate() as SupervisorStrategy.Directive?
                } else {
                    log2.error("Path ${self.path()}")
                    dbRouter.tell(it.msg, it.sender)
                    SupervisorStrategy.resume() as SupervisorStrategy.Directive?
                }
            }.matchAny {
                log2.info("Match Any Called")
                SupervisorStrategy.escalate() as SupervisorStrategy.Directive?
            }
            .build()
    )
    // SupervisorStrategy  for ServerActor
    private var serverStrategy = OneForOneStrategy(
        3,
        Duration.ofMinutes(1),
        DeciderBuilder
                // when DBActorException gets escalated to ServerActor
                // close all connections and create new ConnectorPool
            .match(DBActorException::class.java) {
                log.error("Exception caught by Server Supervisor Strategy cause ${it.exception.message} ${it.msg.retryCnt} ")
                log.error("DBConnections $connections")
                connections.closeAllAndRestart()
                connections = DBMessageConnector()
                SupervisorStrategy.restart() as SupervisorStrategy.Directive?
            }
            .build()
    )

    private val log2: LoggingAdapter = Logging.getLogger(context.system, dbStrategy)
    private val materializer = Materializer.createMaterializer(context.system)
    // Resizer for DBPool
    private val resizer = DefaultResizer(5, 500)

    //Balancing Pool doesn't work with resizer
    private val dbRouter =
        context.actorOf(
            SmallestMailboxPool(20).withResizer(resizer).withSupervisorStrategy(dbStrategy)
                .props(Props.create(DBActor::class.java, connections)),
            "dbRouter"
        )


    override fun supervisorStrategy(): SupervisorStrategy {
        return serverStrategy
    }


    /**
     * handler for the received Messages
     */
    override fun onReceive(message: Any?) {
        //log.info("received ${message.toString()}")
        when (message) {
            is Message -> {
                // If message of type Message (Messages that are sent by the user)
                handleWSMessage(message)
            }
            // Message sent by the Websockets to initiate Connection
            is ConnectionSuccess -> {
                //Received a Websockets Request from a Client -> Create a new UserActor (if provided credentials are valid)
                createUserActor(message)
            }
            // Results send from the DBActors (used by  login,register,delete or create group sequences)
            is UserResult -> getUserResult(message) // Login Result
            is MessagesOfUserResult -> getMessagesOfUser(message)  // Received DB-Messages of an User
            is GroupResult -> getGroupResult(message)   // (Delete and Create Group ) Responses from DB-Actor
            is InsertGroupResult -> createGroupDBResult(message)  // Creates Group Actor
            is FindUserResult -> onFindUserResult(message) // (Register)
            is InsertUserResult -> createUserResult(message)
            is RespondentResult -> onRespondentResult(message) // used by storeMessage to check if Receiver exists
            is InsertMessageResult -> {
                when {
                    message.success -> log.info("insert successful")
                    else -> log.warning("insert not successful")
                }
            }
        }
    }

    /**
     * Handles all the Messages received from the Websocket (User)
     */
    private fun handleWSMessage(message: Message) {
        when {
            // the receiver of the message is the ServerManager
            message.receiverID == "server" -> {
                when (message) {
                    is CreateGroup -> createGroup(message)
                    is RemoveGroup -> deleteGroup(message)
                    else -> log.error("unrecognized message $message")
                }
            }
            //Forward Message to the corresponding Actor
            actors.containsKey(message.receiverID) -> {
                actors[message.receiverID]?.tell(message, self)
            }
            // User currently not logged in / or GroupManager not set up
            else -> {
                storeMessage(message)
            }
        }
    }

    // Create actor for the new Websockets-Connection
    private fun createUserActor(msg: ConnectionSuccess) {
        //log.info("create User ")
        if (msg.username != "" && msg.password != "") {
            // user is currently  not logged in
            if (!actors.containsKey(msg.username)) {
                if (msg.create != "true") {
                    loginUser(msg)
                } else {
                    // create a new user in DB
                    registerUser(msg)
                }
                return
            }
        }
        sender.tell(HttpResponse.create().withStatus(404), this.self())
    }

    /**
     * initializes the Login-Sequence by sending a Message to the DB-Actor
     */
    private fun loginUser(msg: ConnectionSuccess) {
        val s = sender
        log2.info("Login Called")
        dbRouter.tell(RequestUser(msg.username, msg.password, retryCnt, s, msg.wsUpgrade), self) // Send UserRequest to DBActors
    }

    /*
    * Creates the UserActor for a successful Login or Registration
     */
    private fun createWebSocket(username: String, wsUpgrade: WebSocketUpgrade, s: ActorRef) {
        val probe = TestPublisher.probe<akka.http.javadsl.model.ws.Message>(0, context.system)
        val actorRef = context.actorOf(Props.create(ClientActor::class.java, username, probe), username) // Create Client Actor
        actors.putIfAbsent(username, actorRef)
        val sink = Sink.foreach<akka.http.javadsl.model.ws.Message> {

            //TODO streamedText
            //it.asTextMessage().streamedText. { msg -> self.tell(parseMessage(it),actorRef as ActorRef) }
            //it.asTextMessage().streamedText.limit(10).completionTimeout(Duration.ofSeconds(5)).runFold("")(_+_)
            //it.asTextMessage().streamedText.reduce { arg1, arg2 -> arg1+arg2  }.limit(100).flatMapConcat {  }
            it.asTextMessage().toStrict(2000,materializer
            ).thenAccept { msg ->
                self.tell(parseMessage(msg), actorRef as ActorRef)
            }
           // self.tell(parseMessage(it), actorRef as ActorRef)
        }
        var flow = Flow.fromSinkAndSourceCoupled(sink, Source.fromPublisher(probe)) // Create Flowable to forward WS-Messages to the ServerActor and to send Messages to the client
        flow = flow.watchTermination { prevMatValue: NotUsed, completionStage: CompletionStage<Done?> ->
            completionStage.whenComplete { done: Done?, exc: Throwable ->
                // if Ws-Connection closes remove UserActor
                if (done != null) log.info("The stream materialized $prevMatValue") else {
                    log.info("stream from $username has closed ${exc.message}")
                    this.actors[username]?.tell(PoisonPill.getInstance(), self)
                    this.actors.remove(username)
                }
            }
            prevMatValue
        }
        val response = wsUpgrade.handleMessagesWith(flow)
        //log.info("send Upgrade to client")
        s.tell(response, self)
    }

    private fun registerUser(msg: ConnectionSuccess) {
        val s = sender
        dbRouter.tell(FindUser(msg.username, retryCnt, s, msg), self) // Check if user already exists in DB
    }

    /*
     * Initiates the Group Creation Process
     */
    private fun createGroup(msg: CreateGroup) {
        //log.info("create Group")
        if (!actors.containsKey(msg.content)) {
            dbRouter.tell(FindGroup(msg.content, retryCnt, msg), self) // Ask DB-Actor for Group
        }
    }

    private fun deleteGroup(msg: RemoveGroup) {
        //log.info("received Delete Request $msg")
        if (actors.containsKey(msg.content)) {
            actors[msg.content]?.tell(
                PoisonPill.getInstance(),
                ActorRef.noSender()
            ) // Stops the actor when the message is processed
            actors.remove(msg.content)
        }
        dbRouter.tell(FindGroup(msg.content, retryCnt, msg), self)
    }

    // Parses the received TextMessage (string) into the correct Message
    private fun parseMessage(message: akka.http.javadsl.model.ws.Message): Message? {
        var hashMap: Map<String, Any> = HashMap()
        //hashMap = Gson().fromJson(getTextMessage(message.asTextMessage()).strictText, hashMap.javaClass)
        hashMap = Gson().fromJson(message.asTextMessage().strictText, hashMap.javaClass)
        //hashMap = Gson().fromJson(message, hashMap.javaClass)
        val senderID = hashMap["senderID"].toString()
        val receiverID = hashMap["receiverID"].toString()
        val content = hashMap["content"].toString()
        return when (hashMap["type"]) {
            1.0 -> PrivateMessage(senderID, receiverID, content)
            2.0 -> CreateGroup(senderID, content)
            3.0 -> InviteToGroup(senderID, receiverID, content)
            4.0 -> DeclineRequest(senderID, receiverID)
            5.0 -> AcceptRequest(senderID, receiverID)
            6.0 -> LeaveGroup(senderID, receiverID)
            7.0 -> RemoveGroup(senderID, content)
            else -> null
        }
    }

    private fun storeMessage(message: Message) {
        //log.info("storeMessage called $message")
        dbRouter.tell(FindRespondent(message.receiverID, retryCnt, message), self)
    }

    /**
     * Stores the message into the db is receiver is user otherwise creates a new GroupActor
     */
    private fun onRespondentResult(message: RespondentResult) {
        if (message.success) {
            //log.info("Respondent found ${message.result}")
            when (message.result) {
                is User -> {
                    dbRouter.tell(InsertMessage(message = message.message, retryCnt), self)
                }
                is Group -> {
                    val group = message.result
                    //log.info("Group found $group ${group.manager}")
                    group.members.add(group.manager)
                    val actorRef = context.actorOf(
                        Props.create(
                            GroupActor::class.java,
                            group.id,
                            group.manager,
                            group.members,
                            group.invites,
                            dbRouter
                        )
                    )
                    actors[group.id] = actorRef
                    actorRef.tell(message.message, self)
                }
            }
        } else {
            log.error("store error $message")
        }
    }

    private fun createUserResult(message: InsertUserResult) {
        //log.info("getUser completed")
        if (message.success) {
            //log.info("getUser success ${message.result}")
            createWebSocket(message.message.username, message.message.wsUpgrade, message.actorRef)
        } else {
            message.actorRef.tell(HttpResponse.create().withStatus(404), this.self())
        }
    }

    /*
    * registers the user in the DB and creates a new UserActor for it
    */
    private fun onFindUserResult(message: FindUserResult) {
        if (message.result == null && message.success && message.message is ConnectionSuccess) {
            dbRouter.tell(
                InsertUser(
                    message.message.username,
                    message.message.password,
                    retryCnt,
                    message.message,
                    message.actorRef
                ), self
            )
        } else {
            message.actorRef.tell(HttpResponse.create().withStatus(404), this.self())
        }
    }

    /**
     * Creates an new GroupActor after the Insert into the DB was successful
     */
    private fun createGroupDBResult(message: InsertGroupResult) {
        if (message.success) {
            val members = HashSet<String>()
            members.add(message.msg.senderID)
            val actorRef = context.actorOf(
                Props.create(
                    GroupActor::class.java,
                    message.msg.content,
                    message.msg.senderID,
                    members,
                    HashSet<String>(),
                    dbRouter
                ), message.msg.content
            )
            actors[message.msg.content] = actorRef
        } else {
            log.error("create group error")
        }
    }

    /*
    * Handles Create and Delete Group after group was found in the database
     */
    private fun getGroupResult(message: GroupResult) {
        when (message.msg) {
            is CreateGroup -> {
                if (message.result == null && message.success) {
                    dbRouter.tell(
                        InsertGroup(
                            message.msg.content,
                            message.msg.senderID,
                            retryCnt,
                            message.msg
                        ), self
                    )
                } else {
                    log.error("Group already exits ${message.msg.content}")
                }
            }
            is RemoveGroup -> {
                if (message.success) {
                    if (message.result?.manager.equals(message.msg.senderID)) {
                        dbRouter.tell(
                            DeleteEntity(message.result?.id, retryCnt), self
                        )
                    }
                } else {
                    log.error("delete Group error")
                }
            }
        }
    }

    /**
     * forwards the messages found in the database to the user
     */
    private fun getMessagesOfUser(message: MessagesOfUserResult) {
        if (message.success) {
            message.result?.forEach { msg ->
                //log.info("Message of user ${message.user} $message")
                actors[message.user]?.tell(msg, this.self())
            }
        }
    }

    /**
     * handles the UserResult received after the login
     */
    private fun getUserResult(message: UserResult) {
        if (message.success && message.result != null) { // If user exists
            //log.info("getUser success ${message.result.password}")
            this.createWebSocket(message.result.id, message.wsUpgrade, message.actorRef)
            dbRouter.tell(FindMessagesOfUser(message.result.id, retryCnt), self) // find the DB-Messages of the user
        } else {
            message.actorRef.tell(HttpResponse.create().withStatus(404), this.self())
        }
    }
}