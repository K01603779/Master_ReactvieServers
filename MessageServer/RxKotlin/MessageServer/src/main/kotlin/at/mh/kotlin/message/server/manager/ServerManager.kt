package at.mh.kotlin.message.server.manager

import at.mh.kotlin.message.server.db.DBConnR2DBC
import at.mh.kotlin.message.server.db.Group
import at.mh.kotlin.message.server.db.User
import at.mh.kotlin.message.server.messages.CreateGroup
import at.mh.kotlin.message.server.messages.Message
import at.mh.kotlin.message.server.messages.RemoveGroup
import io.ktor.websocket.*
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.cancel
import org.apache.logging.log4j.LogManager


/**
 * Server Manager which handles all the incoming messages and forwards them to the corresponding GroupManager
 */
class ServerManager : Manager() {
    private val logger = LogManager.getLogger("ServerManager")

    // Map of all the active managers (Groups + Users)
    private var managers: HashMap<String, Manager> = HashMap()

    //private var d2 = DBConnRx2JDBC()
    private var db = DBConnR2DBC()

    /**
     *  Handles all messages received by the server from the managers (Group + ClientManager)
     * @param message The received Message
     */
    override fun onNext(message: Message) {
        logger.info("receivedMessage $message")
        when {
            // if the message was sent to the server -> message gets handle by ServerManager
            message.receiverID == "server" -> {
                when (message) {
                    is CreateGroup -> createGroup(message)
                    is RemoveGroup -> deleteGroup(message)
                    else -> logger.error("unrecognized message $message")
                }
            }
            // If an Manager is active forward the message
            managers.containsKey(message.receiverID) -> {
                logger.info("Manager found forward message to manager ${message.receiverID}")
                managers[message.receiverID]!!.onNext(message)
            }
            // otherwise store it
            else -> {
                logger.info("try storing message")
                storeMessage(message)
            }

        }
    }

    override fun onComplete() {
        logger.info("onCompleted Received ")
    }

    override fun onError(e: Throwable?) {
        logger.error("received onError ${e?.message}")
    }

    /**
     * Called when a manager stops
     * @param manager the manager that get removed
     */
    fun closeManager(manager: String) {
        logger.info("Manager closed $manager")
        managers.remove(manager)
    }

    /**
     * when receiver exists and is
     * user -> stores the message into the database
     * group -> create groupManager and forward the message
     * @param message the processed message
     */
    private fun storeMessage(message: Message) {
        // Get Respondent from DB
        logger.info("storing message $message")
        db.getRespondent(message.receiverID)
            .onErrorComplete {
                logger.warn("Not found ${message.receiverID}")
                true
            }.subscribe { respondent ->
                when (respondent) {
                    //If respondent is user -> store message
                    is User -> {
                        db.insertMessage(message)
                            .onErrorComplete {
                                logger.info("DB-Error $it")
                                false
                            }
                            .doOnComplete {
                                logger.info("Insert Message successfully $message")
                            }.subscribe()
                    }
                    // otherwise create GroupManager and forward the message
                    is Group -> {
                        if (!managers.containsKey(respondent.id)) {
                            logger.info("Found Group ${respondent.id}")
                            val groupManager =
                                GroupManager(
                                    respondent.id,
                                    respondent.manager,
                                    respondent.members,
                                    respondent.invites,
                                    db,
                                    this
                                )
                            managers[respondent.id] = groupManager
                            groupManager.subscribe(this)
                        }
                        managers[respondent.id]!!.onNext(message)
                    }
                }
            }
        logger.info("leave storing message")

    }

    /**
     * Creates a new group
     * @param message the message that contains the request
     */
    private fun createGroup(message: CreateGroup) {
        // If no groupManager exists in managers and in the DB create a new groupManager
        if (!managers.containsKey(message.content)) {
            db.groupExists(message.content)
                .onErrorReturn {
                    logger.info("DB-Error $it")
                    true
                }
                .subscribe { exists ->
                    if (!exists) {
                        db.createGroup(message.content, message.senderID)
                            .onErrorComplete {
                                logger.warn("DB-Error $it")
                                false
                            }
                            .doOnComplete {
                                logger.info("Group Creation successful")
                                val members = HashSet<String>()
                                // creator is  part of group members
                                members.add(message.senderID)
                                val groupManager =
                                    GroupManager(message.content, message.senderID, members, HashSet(), db, this)
                                managers[message.content] = groupManager
                                groupManager.subscribe(this)
                            }.subscribe()
                    } else {
                        logger.error("Group does already exit or db-error check logger above")
                    }
                }
        }
    }

    /**
     * deletes a group
     * @param message the delete request
     */
    private fun deleteGroup(message: RemoveGroup) {
        // can only delete group if group exist and the creator of the group initiated the deletion
        logger.info("handling Delete group ${message.content}")
        if (managers.containsKey(message.content)) {
            if (managers[message.content] is GroupManager && (managers[message.content] as GroupManager).groupCreator == message.senderID) {
                managers[message.content]?.onComplete()
                managers.remove(message.content)
                db.deleteEntity(message.content)
                    .onErrorComplete {
                        logger.warn("DB-Error $it")
                        false
                    }
                    .subscribe {
                        logger.info("Group deletion ${message.content} successful")
                    }
            }
        } else {
            db.getGroup(message.content)
                .onErrorComplete {
                    logger.warn("DB-Error $it")
                    true
                }
                .subscribe { group ->
                    if (group.manager == message.senderID) {
                        db.deleteEntity(group.id)
                            .onErrorComplete {
                                logger.error("DB-error $it")
                                false
                            }
                            .subscribe {
                                logger.info("Group deletion ${group.id} successful")
                            }
                    }
                }
        }
    }

    fun removeManager(name: String) {
        managers.remove(name)
    }

    /**
     * Login received via  WebSocket
     * @param username the username of the client
     * @param password the password
     * @param wsConnection the WebSocketSession
     */
    fun loginUserManager(username: String, password: String, wsConnection: DefaultWebSocketServerSession):
            ClientManager {
        //logger.info("LoginUserManager")
        val user = ClientManager(username, wsConnection)
        // check if user with username and pwd exist in DB
        //logger.info("Call userExists")
        db.userExits(username, password)
            .onErrorReturn {
                logger.error("doOnError $username $it")
                false
            }
            .subscribe { foundUser ->
                // if user was found and user isn't already logged in create Manager
                if (!managers.containsKey(username) && foundUser) {
                    logger.info("User found $username")
                    managers[username] = user
                    user.subscribeOn(Schedulers.computation()).subscribe(this)
                    db.getMessageOfUser(username)
                        .onErrorReturn {
                            logger.warn("DB-error $it")
                            null
                        }
                        .subscribe {
                            logger.info("Message from db $it")
                            user.sendMsg(it)
                        }

                } else {
                    // Otherwise cancel Websockets connection
                    logger.error("User $username not found")
                    wsConnection.incoming.cancel()
                    wsConnection.outgoing.close()
                }
            }
            //.subscribe()
        //logger.info("Finished calling user exists")
        return user
    }

    /**
     * Register received via  WebSocket
     * @param username the username of the client
     * @param password the password
     * @param wsConnection the WebSocketSession
     */
    fun registerUserManager(
        username: String,
        password: String,
        wsConnection: DefaultWebSocketServerSession
    ): ClientManager {
        val user = ClientManager(username, wsConnection)
        logger.info("Register User $username")
        if (!managers.containsKey(username)) {
            db.userExits(username)
                .onErrorReturn {
                    logger.info("Error during DB call $it")
                    username
                }
                .subscribe { foundUser ->
                    logger.info("Received Username $foundUser")
                    if (foundUser == username) {
                        logger.info("User $username already exist")
                        wsConnection.cancel()
                    } else {
                        db.createUser(username, password)
                            .onErrorComplete {
                                logger.warn("DB error $it")
                                false
                            }
                            .subscribe {
                                logger.info("User $username registered")
                                managers[username] = user
                                user.subscribeOn(Schedulers.computation()).subscribe(this)
                            }
                    }
                }
        } else {
            wsConnection.cancel()
        }
        return user
    }
}