package at.mh.kotlin.message.server.db

import at.mh.kotlin.message.server.messages.*
import dev.miku.r2dbc.mysql.MySqlConnectionConfiguration
import dev.miku.r2dbc.mysql.MySqlConnectionFactory
import io.r2dbc.spi.*
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.util.concurrent.TimeUnit


class DBConnR2DBC {

    private val configuration: MySqlConnectionConfiguration = MySqlConnectionConfiguration.builder()
        .host("127.0.0.1")
        .user("root")
        .port(3306) // optional, default 3306
        .password("12345") // optional, default null, null means has no password
        .database("warehouse") // optional, default null, null means not specifying the database
        //.useClientPrepareStatement()
        .build()

    private var connectionFactory: ConnectionFactory = MySqlConnectionFactory.from(configuration)


    fun getUser(username: String): Maybe<String> {
        var con: Connection? = null
        return Single.fromPublisher(connectionFactory.create())
            .subscribeOn(Schedulers.io())
            .toFlowable()
            .flatMap { connection: Connection ->
                closeConnection(con)
                con = connection
                connection
                    .createStatement("select respondentID from respondent where respondentID =? and type = 0")
                    .bind(0, username)
                    .execute()
            }.flatMap { result: Result ->
                result
                    .map { row: Row, _: RowMetadata? ->
                        row.get("respondentID", String::class.java)
                    }
            }
            .retryWhen { completed -> completed.take(3).delay(10, TimeUnit.SECONDS) }
            .firstElement()
            .doFinally {
                closeConnection(con)
            }
    }

    fun userExits(username: String): Single<String> {
        var con: Connection? = null
        return Single.fromPublisher(connectionFactory.create())
            .subscribeOn(Schedulers.io())
            .toFlowable()
            .flatMap { connection: Connection ->
                closeConnection(con)
                con = connection
                connection
                    .createStatement("select * from respondent where respondentID =? and type = 0")
                    .bind(0, username)
                    .execute()
            }
            .flatMap { result: Result ->
                println("Map Result $username")
                result
                    .map { row: Row, _: RowMetadata? ->
                        println(row)
                        row.get("respondentID", String::class.java)
                    }
            }
            .retryWhen { completed -> completed.take(3).delay(10, TimeUnit.SECONDS) }
            .first("")
            .doFinally {
                closeConnection(con)
            }
    }

    fun userExits(username: String, password: String): Single<Boolean> {
        println("Pool Metadata ${connectionFactory.metadata}")
        var con: Connection? = null
        return Single.fromPublisher(connectionFactory.create())
            .subscribeOn(Schedulers.io())
            .toFlowable()
            .flatMap { connection: Connection ->
                closeConnection(con)
                con = connection
                connection
                    .createStatement("select count(*) from respondent where respondentID =? and content =? and type = 0")
                    .bind(0, username)
                    .bind(1, password)
                    .execute()
            }
            .flatMap { result: Result ->
                result
                    .map { row: Row, _: RowMetadata? ->
                        row.get("count(*)", Long::class.java)
                    }
            }
            .map {
                it > 0
            }
            .firstOrError()
            .retryWhen { completed -> completed.take(3).delay(1, TimeUnit.SECONDS) }
            .doFinally {
                closeConnection(con)
            }
    }

    private fun closeConnection(con: Connection?) {
        if (con != null) {
            //Mono.from(con!!.close()).subscribe()
            Completable.fromPublisher(con.close()).subscribe()
            //var test : Mono = con!!.close()

        }
    }

    fun getRespondent(id: String): Maybe<Respondent> {
        var con: Connection? = null
        return Single.fromPublisher(connectionFactory.create())
            .subscribeOn(Schedulers.io())
            .toFlowable()
            .flatMap { connection: Connection ->
                closeConnection(con)
                con = connection
                connection
                    .createStatement("select * from respondent where respondentID =?")
                    .bind(0, id)
                    .execute()
            }
            .flatMap { result ->
                result.map { row: Row, _: RowMetadata? ->
                    if (row.get("type") == 0) {
                        User(row.get("respondentID") as String, row.get("content") as String)
                    } else {
                        val group = Group(row.get("respondentID") as String, row.get("content") as String)
                        group.members.add(group.manager)
                        getMembersFromGroup(group.id).subscribeOn(Schedulers.io()).blockingForEach {
                            group.members.add(it)
                        }
                        // Blocking so that the first message isn't processed with empty members and invitee
                        getInvitesFromGroup(group.id).subscribeOn(Schedulers.io()).blockingForEach {
                            group.invites.add(it)
                        }
                        group
                    }
                }
            }
            .retryWhen { completed -> completed.take(3).delay(10, TimeUnit.SECONDS) }
            .firstElement()
            .doFinally { closeConnection(con) }
    }

    fun createUser(username: String, password: String): Completable {
        var con: Connection? = null
        return Single.fromPublisher(connectionFactory.create())
            .subscribeOn(Schedulers.io())
            .toFlowable()
            .flatMap { connection: Connection ->
                closeConnection(con)
                con = connection
                connection
                    .createStatement("INSERT INTO respondent (respondentID, content, type) VALUES (?,?,0)")
                    .bind(0, username)
                    .bind(1, password)
                    .execute()
            }
            .retryWhen { completed -> completed.take(3).delay(10, TimeUnit.SECONDS) }
            .ignoreElements()
            .doFinally { closeConnection(con) }
    }

    fun groupExists(groupName: String): Single<Boolean> {
        var con: Connection? = null
        return Single.fromPublisher(connectionFactory.create())
            .subscribeOn(Schedulers.io())
            .toFlowable()
            .flatMap { connection: Connection ->
                closeConnection(con)
                con = connection
                connection
                    .createStatement("select count(*) from respondent where respondentID =? and type = 1")
                    .bind(0, groupName)
                    .execute()
            }
            .flatMap { result: Result ->
                result
                    .map { row: Row, _: RowMetadata? ->
                        row.get("count(*)", Long::class.java)
                    }
            }
            .map {
                it > 0
            }
            .retryWhen { completed -> completed.take(3).delay(10, TimeUnit.SECONDS) }
            .firstOrError()
            .doFinally { closeConnection(con) }
    }

    fun getGroup(groupName: String): Maybe<Group> {
        var con: Connection? = null
        return Single.fromPublisher(connectionFactory.create())
            .subscribeOn(Schedulers.io())
            .toFlowable()
            .flatMap { connection: Connection ->
                closeConnection(con)
                con = connection
                connection
                    .createStatement("select * from respondent where respondentID =:id and type = 1")
                    .bind(0, groupName)
                    .execute()
            }
            .flatMap { result: Result ->
                result
                    .map { row: Row, _: RowMetadata? ->
                        val group = Group(row.get("respondentID") as String, row.get("content") as String)
                        group.members.add(group.manager)
                        /*getMembersFromGroup(group.id).blockingSubscribe {
                            group.members.add(it)
                        }
                        // Blocking so that the first message isn't processed with empty members and invitee
                        getInvitesFromGroup(group.id).blockingSubscribe {
                            group.invites.add(it)
                        } */ // currently not needed since it only is used by delete
                        group
                    }
            }
            .retryWhen { completed -> completed.take(3).delay(10, TimeUnit.SECONDS) }
            .firstElement()
            .doFinally { closeConnection(con) }
    }

    fun createGroup(groupName: String, creator: String): Completable {
        var con: Connection? = null
        return Single.fromPublisher(connectionFactory.create())
            .subscribeOn(Schedulers.io())
            .toFlowable()
            .flatMap { connection: Connection ->
                closeConnection(con)
                con = connection
                connection
                    .createStatement("INSERT INTO respondent (respondentID, content, type) VALUES (?,?,1)")
                    .bind(0, groupName)
                    .bind(1, creator)
                    .execute()
            }
            .retryWhen { completed -> completed.take(3).delay(10, TimeUnit.SECONDS) }
            .ignoreElements()
            .doFinally { closeConnection(con) }
    }

    fun removeGroupEntry(groupName: String, username: String): Completable {
        var con: Connection? = null
        return Single.fromPublisher(connectionFactory.create())
            .subscribeOn(Schedulers.io())
            .toFlowable()
            .flatMap { connection: Connection ->
                closeConnection(con)
                con = connection
                connection
                    .createStatement("delete from groupEntry where userID = ? and groupID = ?")
                    .bind(0, username)
                    .bind(1, groupName)
                    .execute()
            }
            .retryWhen { completed -> completed.take(3).delay(10, TimeUnit.SECONDS) }
            .ignoreElements()
            .doFinally { closeConnection(con) }
    }

    fun deleteEntity(id: String): Completable {
        var con: Connection? = null
        return Single.fromPublisher(connectionFactory.create())
            .subscribeOn(Schedulers.io())
            .toFlowable()
            .flatMap { connection: Connection ->
                closeConnection(con)
                con = connection
                connection
                    .createStatement("delete from respondent where respondentID = ?")
                    .bind(0, id)
                    .execute()
            }
            .retryWhen { completed -> completed.take(3).delay(10, TimeUnit.SECONDS) }
            .ignoreElements()
            .doFinally { closeConnection(con) }
    }

    fun updateGroupEntry(groupName: String, username: String, accepted: Boolean): Completable {
        var con: Connection? = null
        return Single.fromPublisher(connectionFactory.create())
            .subscribeOn(Schedulers.io())
            .toFlowable()
            .flatMap { connection: Connection ->
                closeConnection(con)
                con = connection
                connection
                    .createStatement("replace  into groupEntry (groupID, userID, accepted) values (?,?,?)")
                    .bind(0, groupName)
                    .bind(1, username)
                    .bind(2, accepted)
                    .execute()
            }
            .retryWhen { completed -> completed.take(3).delay(10, TimeUnit.SECONDS) }
            .ignoreElements()
            .doFinally { closeConnection(con) }
    }

    private fun getInvitesFromGroup(groupName: String): Flowable<String> {
        var con: Connection? = null
        return Single.fromPublisher(connectionFactory.create())
            .subscribeOn(Schedulers.io())
            .toFlowable()
            .flatMap { connection: Connection ->
                closeConnection(con)
                con = connection
                connection
                    .createStatement("select userID from groupEntry where groupID = ? and accepted = 0")
                    .bind(0, groupName)
                    .execute()
            }
            .flatMap { result: Result ->
                result
                    .map { row: Row, _: RowMetadata? ->
                        row.get("userID", String::class.java)
                    }
            }
            .retryWhen { completed -> completed.take(3).delay(10, TimeUnit.SECONDS) }
            .doFinally { closeConnection(con) }
    }

    private fun getMembersFromGroup(groupName: String): Flowable<String> {
        var con: Connection? = null
        return Single.fromPublisher(connectionFactory.create())
            .subscribeOn(Schedulers.io())
            .toFlowable()
            .flatMap { connection: Connection ->
                println("GetConnection FromGroup")
                closeConnection(con)
                con = connection
                connection
                    .createStatement("select userID from groupEntry where groupID = ? and accepted = 1")
                    .bind(0, groupName)
                    .execute()
            }
            .flatMap { result: Result ->
                result
                    .map { row: Row, _: RowMetadata? ->
                        row.get("userID", String::class.java)
                    }
            }
            .retryWhen { completed -> completed.take(3).delay(10, TimeUnit.SECONDS) }
            .doFinally { closeConnection(con) }
    }

    fun insertMessage(message: Message): Completable {
        var con: Connection? = null
        return Single.fromPublisher(connectionFactory.create())
            .subscribeOn(Schedulers.io())
            .toFlowable()
            .flatMap { connection: Connection ->
                closeConnection(con)
                con = connection
                connection
                    .createStatement("INSERT INTO message (senderID, receiverID, content,type) VALUES (?,?,?,?)")
                    .bind(0, message.senderID)
                    .bind(1, message.receiverID)
                    .bind(2, message.content)
                    .bind(3, message.type.ordinal)
                    .execute()
            }
            // TODO test doFinally here and delete the con?.close() in map
            .retryWhen { completed -> completed.take(3).delay(10, TimeUnit.SECONDS) }
            .ignoreElements()
            .doFinally { closeConnection(con) }
    }

    fun getMessageOfUser(username: String): Flowable<Message> {
        var con: Connection? = null
        return Single.fromPublisher(connectionFactory.create())
            .subscribeOn(Schedulers.io())
            .toFlowable()
            .flatMap { connection: Connection ->
                closeConnection(con)
                con = connection
                connection
                    .createStatement("select * from message where receiverID = ?")
                    .bind(0, username)
                    .execute()
            }
            .flatMap { result: Result ->
                result
                    .map { row: Row, _: RowMetadata? ->
                        //deleteMessage(row["messageID"] as Int) //TODO use names
                        createMessage(row[1] as String, row[2] as String, row[3] as String, row[4] as Int)
                    }
            }
            .filter { message: Message? -> message != null }
            .map { it!! }
            .retryWhen { completed -> completed.take(3).delay(10, TimeUnit.SECONDS) }
            .doFinally {
                closeConnection(con)
                //con?.close()
            }
    }

    private fun deleteMessage(id: Int): Disposable {
        var con: Connection? = null
        return Single.fromPublisher(connectionFactory.create())
            .subscribeOn(Schedulers.io())
            .toFlowable()
            .flatMap { connection: Connection ->
                closeConnection(con)
                con = connection
                connection
                    .createStatement("delete from message where messageID = ?")
                    .bind(0, id)
                    .execute()
            }.ignoreElements()
            .retryWhen { completed -> completed.take(3).delay(10, TimeUnit.SECONDS) }
            .onErrorComplete {
                println("DB-Error while deleting")
                true
            }
            .doFinally { closeConnection(con) }
            .subscribe()

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