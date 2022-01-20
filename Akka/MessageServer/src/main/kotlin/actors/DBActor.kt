package actors

import akka.actor.ActorRef
import akka.actor.UntypedAbstractActor
import akka.event.Logging
import akka.event.LoggingAdapter
import connectionpool.DBMessageConnector
import connectionpool.Group
import connectionpool.Respondent
import connectionpool.User
import messages.*
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException
import java.util.*

class DBActor(private val connections: DBMessageConnector) : UntypedAbstractActor() {
    private val log: LoggingAdapter = Logging.getLogger(context.system, this)

    override fun onReceive(message: Any?) {
        //log.info("ConnectionActor : Message received by ${this.self} Actor")
        when (message) {
            is DBMessage -> {
                if (message.retryCnt <= 0) {
                    sender.tell(DBErrorMessage(message, "error TODO"), context.parent)
                } else {
                    when (message) {
                        is RequestUser -> {
                            getUser(message)
                        }
                        is FindUser -> {
                            findUser(message)
                        }
                        is FindRespondent -> {
                            getRespondent(message)
                        }
                        is InsertUser -> {
                            createUser(message)
                        }
                        is FindGroup -> {
                            getGroup(message)
                        }
                        is InsertGroup -> {
                            createGroup(message)
                        }
                        is DeleteGroupEntry -> {
                            removeGroupEntry(message)
                        }
                        is DeleteEntity -> {
                            deleteEntity(message)
                        }
                        is UpdateGroupEntry -> {
                            updateGroupEntry(message)
                        }
                        is InsertMessage -> {
                            insertMessage(message)
                        }
                        is FindMessagesOfUser -> {
                            getMessageOfUser(message)
                        }
                        is DeleteMessage ->{
                            deleteMessage(message)
                        }
                    }
                }
            }
        }
    }

    private fun getUser(message: RequestUser) {
        val newMsg =
            RequestUser(message.username, message.password, message.retryCnt - 1, message.sender, message.wsUpgrade)
        val user: User?
        var con: Connection? = null
        try {
            con = connections.getConnection()
            val prep =
                con!!.prepareStatement("select * from respondent where respondentID =? and content =? and type = 0")
            prep.setString(1, message.username)
            prep.setString(2, message.password)
            val rs = prep.executeQuery()
            user = mapUser(rs)
            //log.info("Found User ${user?.id}")
            rs.close()
        } catch (e: Exception) {
            if (con?.isClosed == false) {
                con.close()
            }
            throw DBActorException(e, newMsg, sender)
        } finally {
            if (con?.isClosed == false) {
                con.close()
            }
        }
        this.sender.tell(UserResult(user, success = true, message.sender, message.wsUpgrade), context.parent)
    }

    @Throws(SQLException::class)
    private fun mapUser(set: ResultSet): User? {
        if (set.next()) {
            val username = set.getString("respondentID")
            val password = set.getString("content")
            return User(username, password)
        }
        return null
    }

    @Throws(SQLException::class)
    private fun mapRespondent(set: ResultSet): Respondent? {
        if (set.next()) {
            val type = set.getInt("type")
            val content = set.getString("content")
            val id = set.getString("respondentID")
            if (type == 0) {
                return User(id, content)
            } else if (type == 1) {
                val group = Group(id, content)
                val ret = this.getUserEntriesFromGroup(id, 3)
                group.members.addAll(ret?.result?.key!!.asIterable())
                group.invites.addAll(ret.result.value!!.asIterable())
                return group
            }
        }
        return null
    }

    private fun findUser(message: FindUser) {
        val newMsg = FindUser(message.username, message.retryCnt - 1, message.actorRef, message.message)
        val user: User?
        var con: Connection? = null
        try {
            con = connections.getConnection()
            val prep = con!!
                .prepareStatement("select * from respondent where respondentID =? and type = 0")
            prep.setString(1, message.username)
            val rs = prep.executeQuery()
            user = mapUser(rs)
            rs.close()
        } catch (e: Exception) {
            if (con?.isClosed == false) {
                con.close()
            }
            throw DBActorException(e, newMsg, sender)
        } finally {
            if (con?.isClosed == false) {
                con.close()
            }
        }
        sender.tell(FindUserResult(user, true, message.actorRef, message.message), context.parent)
    }

    private fun getRespondent(message: FindRespondent) {
        val newMsg = FindRespondent(message.id, message.retryCnt - 1, message.message)
        val respondent: Respondent?
        var con: Connection? = null
        try {
            con = connections.getConnection()
            val prep: PreparedStatement = con!!.prepareStatement("select * from respondent where respondentID =?")
            prep.setString(1, message.id)
            val rs: ResultSet = prep.executeQuery()
            respondent = mapRespondent(rs)
            rs.close()
        } catch (e: java.lang.Exception) {
            if (con?.isClosed == false) {
                con.close()
            }
            throw DBActorException(e, newMsg, sender)
        } finally {
            if (con?.isClosed == false) {
                con.close()
            }
        }
        sender.tell(RespondentResult(respondent, true, message.message), context.parent)
    }

    private fun createUser(message: InsertUser) {
        val user: String
        var con: Connection? = null
        val newMsg =
            InsertUser(message.username, message.password, message.retryCnt - 1, message.message, message.actorRef)
        try {
            con = connections.getConnection()
            var prep = con!!
                .prepareStatement("INSERT INTO respondent (respondentID, content, type) VALUES (?,?,0)")
            prep.setString(1, message.username)
            prep.setString(2, message.password)
            prep.execute()
            prep.close()
            prep = con.prepareStatement("SELECT LAST_INSERT_ID()")
            val rset = prep.executeQuery()
            rset.next()
            user = rset.getString("LAST_INSERT_ID()")
        } catch (e: java.lang.Exception) {
            if (con?.isClosed == false) {
                con.close()
            }
            throw DBActorException(e, newMsg, sender)
        } finally {
            if (con?.isClosed == false) {
                con.close()
            }
        }
        sender.tell(InsertUserResult(user, true, message.message, message.actorRef), context.parent)
    }

    private fun getGroup(message: FindGroup) {
        val respondent: Group?
        var con: Connection? = null
        val newMsg = FindGroup(message.groupName, message.retryCnt - 1, message.msg)
        try {
            con = connections.getConnection()
            val prep = con!!
                .prepareStatement("select * from respondent where respondentID =? and type = 1")
            prep.setString(1, message.groupName)
            val rs = prep.executeQuery()
            respondent = mapRespondent(rs) as Group?
            rs.close()
        } catch (e: java.lang.Exception) {
            if (con?.isClosed == false) {
                con.close()
            }
            throw DBActorException(e, newMsg, sender)
        } finally {
            if (con?.isClosed == false) {
                con.close()
            }
        }
        sender.tell(GroupResult(respondent, success = true, message.msg), context.parent)
    }

    private fun createGroup(message: InsertGroup) {
        val group: String
        val newMsg = InsertGroup(message.groupName, message.creator, message.retryCnt - 1, message.msg)
        var con: Connection? = null
        try {
            con = connections.getConnection()
            var prep = con!!
                .prepareStatement("INSERT INTO respondent (respondentID, content, type) VALUES (?,?,1)")
            prep.setString(1, message.groupName)
            prep.setString(1, message.groupName)
            prep.setString(2, message.creator)
            prep.execute()
            prep.close()
            prep = con.prepareStatement("SELECT LAST_INSERT_ID()")
            val rset = prep.executeQuery()
            rset.next()
            group = rset.getString("LAST_INSERT_ID()")
        } catch (e: java.lang.Exception) {
            if (con?.isClosed == false) {
                con.close()
            }
            throw DBActorException(e, newMsg, sender)
        } finally {
            if (con?.isClosed == false) {
                con.close()
            }
        }
        sender.tell(InsertGroupResult(group, success = true, message.msg), context.parent)
    }

    private fun removeGroupEntry(message: DeleteGroupEntry) {
        val newMsg = DeleteGroupEntry(message.groupName, message.username, message.retryCnt - 1)
        var con: Connection? = null
        try {
            con = connections.getConnection()
            val prep = con!!.prepareStatement("delete from groupEntry where userID = ? and groupID = ?")
            prep.setString(1, message.username)
            prep.setString(2, message.groupName)
            prep.execute()
        } catch (e: java.lang.Exception) {
            if (con?.isClosed == false) {
                con.close()
            }
            throw DBActorException(e, newMsg, sender)
        } finally {
            if (con?.isClosed == false) {
                con.close()
            }
        }
        sender.tell(DeleteGroupEntryResult(true), context.parent)
    }

    private fun deleteEntity(message: DeleteEntity) {
        val newMsg = DeleteEntity(message.id, message.retryCnt - 1)
        var con: Connection? = null
        try {
            con = connections.getConnection()
            val prep = con!!.prepareStatement("delete from respondent where respondentID = ?")
            prep.setString(1, message.id)
            prep.execute()
        } catch (e: java.lang.Exception) {
            if (con?.isClosed == false) {
                con.close()
            }
            throw DBActorException(e, newMsg, sender)
        } finally {
            if (con?.isClosed == false) {
                con.close()
            }
        }
        sender.tell(DeleteEntityResult(true), context.parent)
    }

    private fun updateGroupEntry(message: UpdateGroupEntry) {
        var con: Connection? = null
        val newMsg = UpdateGroupEntry(message.groupName, message.username, message.accepted, message.retryCnt - 1)
        try {
            con = connections.getConnection()
            val prep = con!!.prepareStatement("replace  into groupEntry (groupID, userID, accepted) values (?,?,?)")
            prep.setString(1, message.groupName)
            prep.setString(2, message.username)
            prep.setBoolean(3, message.accepted)
            prep.execute()
        } catch (e: java.lang.Exception) {
            if (con?.isClosed == false) {
                con.close()
            }
            throw DBActorException(e, newMsg, sender)
        } finally {
            if (con?.isClosed == false) {
                con.close()
            }
        }
        sender.tell(UpdateGroupEntryResult(true), context.parent)
    }

    private fun getUserEntriesFromGroup(
        groupName: String?,
        retryCnt: Int
    ): DBResult<AbstractMap.SimpleEntry<List<String>?, List<String>?>?>? {
        val members: MutableList<String> = LinkedList()
        val invitees: MutableList<String> = LinkedList()
        var con: Connection? = null
        try {
            con = connections.getConnection()
            val prep = con!!.prepareStatement("select * from groupEntry where groupID = ?")
            prep.setString(1, groupName)
            val rs = prep.executeQuery()
            var accepted: Boolean
            var userID: String
            while (rs.next()) {
                accepted = rs.getBoolean("accepted")
                userID = rs.getString("userID")
                if (accepted) {
                    members.add(userID)
                } else {
                    invitees.add(userID)
                }
            }
            rs.close()
        } catch (e: java.lang.Exception) {
            if (con?.isClosed == false) {
                con.close()
            }
            return if (retryCnt == 0) {
                DBResult(null, false)
            } else {
                getUserEntriesFromGroup(groupName, retryCnt - 1)
            }
        } finally {
            if (con?.isClosed == false) {
                con.close()
            }
        }
        val entry = AbstractMap.SimpleEntry<List<String>?, List<String>?>(members, invitees)
        return DBResult(entry, true)
    }

    private fun insertMessage(message: InsertMessage) {
        var con: Connection? = null
        val newMsg = InsertMessage(message.message, message.retryCnt - 1)
        try {
            con = connections.getConnection()
            val prep =
                con!!.prepareStatement("INSERT INTO message (senderID, receiverID, content,type) VALUES (?,?,?,?)")
            prep.setString(1, message.message.senderID)
            prep.setString(2, message.message.receiverID)
            prep.setString(3, message.message.content)
            prep.setInt(4, message.message.type.ordinal)
            prep.execute()
        } catch (e: java.lang.Exception) {
            if (con?.isClosed == false) {
                con.close()
            }
            throw DBActorException(e, newMsg, sender)
        } finally {
            if (con?.isClosed == false) {
                con.close()
            }
        }
        sender.tell(InsertMessageResult(true), context.parent)
    }

    private fun getMessageOfUser(message: FindMessagesOfUser) {
        val messages = LinkedList<Message>()
        var senderID: String
        var receiverID: String
        var content: String
        var me: Message?
        var prep2: PreparedStatement
        var type: Int
        var messageID: Int
        var con: Connection? = null
        val newMsg = FindMessagesOfUser(message.username, message.retryCnt - 1)
        try {
            con = connections.getConnection()
            val prep = con!!.prepareStatement("select * from message where receiverID = ?")
            prep.setString(1, message.username)
            val rs = prep.executeQuery()
            while (rs.next()) {
                senderID = rs.getString("senderID")
                receiverID = rs.getString("receiverID")
                content = rs.getString("content")
                type = rs.getInt("type")
                messageID = rs.getInt("messageID")
                me = createMessage(senderID, receiverID, content, type)
                if (me != null) {
                    messages.add(me)
                } else {
                    log.error("msg was null")
                }
                /*prep2 = con.prepareStatement("delete from message where messageID = ?")
                prep2.setInt(1, messageID)
                prep2.execute()
                prep2.close()*/
                //this.context.parent.tell(DeleteMessage(messageID,retryCnt = 3), ActorRef.noSender()) //TODO test
            }
            rs.close()
        } catch (e: java.lang.Exception) {
            if (con?.isClosed == false) {
                con.close()
            }
            throw DBActorException(e, newMsg, sender)
        } finally {
            if (con?.isClosed == false) {
                con.close()
            }
        }
        sender.tell(MessagesOfUserResult(messages, true, message.username), context.parent)
    }

    private fun deleteMessage(msg : DeleteMessage){
        var con: Connection? =null
        val newMsg = DeleteMessage(msg.messageID, msg.retryCnt - 1)
        try {
            con= connections.getConnection()!!
            val prep =
                con!!.prepareStatement("delete from message where messageID = ?")
            prep.setInt(1, msg.messageID)
            prep.execute()
            prep.close()
        } catch (e: java.lang.Exception) {
            if (con?.isClosed== false) {
                con.close()
            }
            throw DBActorException(e, newMsg, sender)
        } finally {
            if (con?.isClosed == false) {
                con.close()
            }
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

class DBActorException(val exception: Exception, var msg: DBMessage, var sender: ActorRef) : java.lang.Exception()