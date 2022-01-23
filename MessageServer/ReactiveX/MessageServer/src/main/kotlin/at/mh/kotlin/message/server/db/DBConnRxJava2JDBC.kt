package at.mh.kotlin.message.server.db

import at.mh.kotlin.message.server.messages.*
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Single
import org.davidmoten.rx.jdbc.Database

class DBConnRxJava2JDBC {
    //private val logger = LogManager.getLogger("Database call")

    /*
    * uses https://github.com/davidmoten/rxjava2-jdbc
     */

    private lateinit var db: Database

    init {
        initDB()
    }

    private fun initDB() {
        try {
            db = Database
                .nonBlocking()
                //.nonBlocking()
                // the jdbc url of the connections to be placed in the pool
                .url("jdbc:mysql://localhost:55555/warehouse")
                .user("root")
                .password("")
                // an unused connection will be closed after thirty minutes
                .maxIdleTime(30, java.util.concurrent.TimeUnit.MINUTES)
                // connections are checked for healthiness on checkout if the connection
                .healthCheck(org.davidmoten.rx.jdbc.pool.DatabaseType.MYSQL)
                // has been idle for at least 5 seconds
                .idleTimeBeforeHealthCheck(5, java.util.concurrent.TimeUnit.SECONDS)
                // the maximum number of connections in the pool
                .maxPoolSize(8)
                // if a connection fails creation then retry after 30 seconds
                //.connectionRetryInterval(10, TimeUnit.SECONDS)
                .connectionListener {
                    db.close()
                    initDB()
                    it.ifPresent { kotlin.io.println("Unable to Connect to DB ${it.message}") }
                }
                .build()
        } catch (e: Exception) {
            println("Error")
        }
    }
    // private val db : Database = Database.from("jdbc:mysql://localhost:55555/warehouse",8)


    /*
     * Returns a user with the given name
     */
    fun getUser(username: String): Maybe<String> {
        return db.select("select respondentID from respondent where respondentID =:username and type = 0")
            .parameter("username", username)
            .getAs(String::class.java)
            .firstElement()
    }

    /*
     * Check if the user exits in the Database
     */
    fun userExits(username: String): Single<Boolean> {//Flowable<Boolean> {
        return db.select("select count(*) from respondent where respondentID =:username and type = 0")
            .parameter("username", username)
            .getAs(Long::class.java)
            .map {
                it > 0
            }
            .firstOrError()

    }

    /*
    * Check if user exists
    */
    fun userExits(username: String, password: String): Single<Boolean> {

        return db.select("select count(*) from respondent where respondentID =:user and content =:password and type = 0")
            .parameter("user", username)
            .parameter("password", password)
            .queryTimeoutSec(2000)
            .getAs(Long::class.java)
            .map {
                it > 0

            }
            .doOnError {
                println("Error received $it")
            }
            .firstOrError()
            .retry(3)

    }

    /**
     * Returns the Respondent (Group or User) with the given ID
     */
    fun getRespondent(id: String?): Maybe<Respondent> {
        return db.select("select * from respondent where respondentID =:id")
            .parameter("id", id)
            .getAs(String::class.java, String::class.java, Int::class.java)
            .map { tuple ->
                if (tuple._3() == 0) {
                    User(tuple.value1(), tuple.value2())
                } else {
                    val group = Group(tuple.value1(), tuple.value2())
                    group.members.add(tuple.value2())
                    getMembersFromGroup(group.id).blockingIterable().forEach {
                        group.members.add(it)
                    }
                    // Blocking so that the first message isn't processed with empty members and invitee
                    getInvitesFromGroup(group.id).blockingIterable().forEach {
                        group.invites.add(it)
                    }
                    group
                }
            }.firstElement()
    }

    /**
     * Creates a user
     */
    fun createUser(username: String?, password: String?): Completable {
        return db.update("INSERT INTO respondent (respondentID, content, type) VALUES (:id,:pwd,0)")
            .parameter("id", username)
            .parameter("pwd", password)
            .complete()
    }

    /*
    * Checks if group with given groupName exists
     */
    fun groupExists(groupName: String): Single<Boolean> {
        return db.select("select count(*) from respondent where respondentID =:groupName and type = 1")
            .parameter("groupName", groupName)
            .getAs(Long::class.java).map {
                it > 0
            }.firstOrError()
    }

    /** TODO
     * Return the group with the given groupName
     */
    fun getGroup(groupName: String?): Maybe<Group> {
        return db.select("select * from respondent where respondentID =:id and type = 1")
            .parameter("id", groupName)
            .getAs(String::class.java, String::class.java, Integer::class.java)
            .map { tuple ->
                val group = Group(tuple.value1(), tuple.value2())
                group.members.add(tuple.value2())
                getMembersFromGroup(group.id).subscribe {
                    println("Found member for ${group.id} $it")
                    group.members.add(it)
                }
                getInvitesFromGroup(group.id).subscribe {
                    println("Found invitee for ${group.id} $it")
                    group.invites.add(it)
                }
                group
            }.firstElement()
    }

    /*
    * Create group
    */
    fun createGroup(groupName: String?, creator: String?): Completable {
        return db.update("INSERT INTO respondent (respondentID, content, type) VALUES (:id,:creator,1)")
            .parameter("id", groupName)
            .parameter("creator", creator)
            .complete()
    }

    fun removeGroupEntry(groupName: String?, username: String?): Completable {
        return db.update("delete from groupEntry where userID = :id and groupID = :group")
            .parameter("id", username)
            .parameter("group", groupName)
            .complete()
    }

    fun deleteEntity(id: String?): Completable {
        return db.update("delete from respondent where respondentID = :id")
            .parameter("id", id)
            .complete()
    }

    fun updateGroupEntry(groupName: String?, username: String?, accepted: Boolean): Completable {
        return db.update("replace  into groupEntry (groupID, userID, accepted) values (:group,:user,:accept)")
            .parameter("group", groupName)
            .parameter("user", username)
            .parameter("accept", accepted)
            .complete()
    }

    private fun getInvitesFromGroup(groupName: String?): Flowable<String> {
        return db.select("select userID from groupEntry where groupID = :id and accepted = 0")
            .parameter("id", groupName)
            .getAs(String::class.java)
    }

    private fun getMembersFromGroup(groupName: String?): Flowable<String> {
        return db.select("select userID from groupEntry where groupID = :id and accepted = 1")
            .parameter("id", groupName)
            .getAs(String::class.java)
    }


    fun insertMessage(message: Message): Completable {
        return db.update("INSERT INTO message (senderID, receiverID, content,type) VALUES (:sender,:receiver,:content,:type)")
            .parameter("sender", message.senderID)
            .parameter("receiver", message.receiverID)
            .parameter("content", message.content)
            .parameter("type", message.type.ordinal)
            .complete()
    }


    fun getMessageOfUser(username: String?): Flowable<Message> {
        return db.select("select * from message where receiverID = :receiver")
            .parameter("receiver", username)
            .getAs(Integer::class.java, String::class.java, String::class.java, String::class.java, Int::class.java)
            .map {
                db.update("delete from message where messageID = :id").parameter("id", it.value1()).complete()
                    .subscribe()
                createMessage(it._2(), it._3(), it._4(), it._5())
            }
    }

    private fun createMessage(senderID: String, receiverID: String, content: String, type: Int): Message? {
        return when (type) {
            MessageType.Private.ordinal -> PrivateMessage(senderID, receiverID, content)
            MessageType.AcceptRequest.ordinal -> AcceptRequest(senderID, receiverID)
            MessageType.DeclineRequest.ordinal -> DeclineRequest(senderID, receiverID)
            MessageType.CreateGroup.ordinal -> CreateGroup(senderID, content)
            MessageType.RemoveGroup.ordinal -> RemoveGroup(senderID, content)
            MessageType.InviteToGroup.ordinal -> InviteToGroup(senderID, receiverID, content)
            MessageType.LeaveGroup.ordinal -> LeaveGroup(senderID, receiverID)
            else -> null
        }
    }


}