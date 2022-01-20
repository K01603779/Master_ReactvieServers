package messages

import akka.actor.ActorRef
import akka.http.javadsl.model.ws.WebSocketUpgrade
import connectionpool.Group
import connectionpool.Respondent
import connectionpool.User

// Messages used between DBActor and Managers
// ResultMessages are Messages sent from DBActor -> Manager
// other messages Manager -> DBActor
abstract class DBMessage(val retryCnt: Int)
open class DBResult<T>(val result: T?, val success: Boolean)

class RequestUser(
    val username: String, val password: String, retryCnt: Int,
    var sender: ActorRef, val wsUpgrade: WebSocketUpgrade
) : DBMessage(retryCnt)

class UserResult(user: User?, success: Boolean, val actorRef: ActorRef, val wsUpgrade: WebSocketUpgrade) :
    DBResult<User>(user, success)

class FindUser(val username: String, retryCnt: Int, val actorRef: ActorRef, val message: Any) :
    DBMessage(retryCnt)

class FindUserResult(user: User?, success: Boolean, val actorRef: ActorRef, val message: Any) :
    DBResult<User>(user, success)


class FindRespondent(val id: String, retryCnt: Int, val message: Message) : DBMessage(retryCnt)

class RespondentResult(respondent: Respondent?, success: Boolean, val message: Message) :
    DBResult<Respondent>(respondent, success)

class InsertUser(
    val username: String, val password: String, retryCnt: Int,
    val message: ConnectionSuccess, val actorRef: ActorRef
) : DBMessage(retryCnt)

class InsertUserResult(user: String, success: Boolean, val message: ConnectionSuccess, val actorRef: ActorRef) :
    DBResult<String>(user, success)

class FindGroup(val groupName: String, retryCnt: Int, val msg: Message) : DBMessage(retryCnt)

class GroupResult(group: Group?, success: Boolean, val msg: Message) : DBResult<Group>(group, success)

class InsertGroup(val groupName: String, val creator: String, retryCnt: Int, val msg: CreateGroup) :
    DBMessage(retryCnt)

class InsertGroupResult(groupName: String, success: Boolean, val msg: CreateGroup) :
    DBResult<String>(groupName, success)

class DeleteGroupEntry(val groupName: String, val username: String, retryCnt: Int) : DBMessage(retryCnt)
class DeleteGroupEntryResult(val success: Boolean)

class DeleteEntity(val id: String?, retryCnt: Int) : DBMessage(retryCnt)
class DeleteEntityResult(val success: Boolean)

class UpdateGroupEntry(val groupName: String, val username: String, val accepted: Boolean, retryCnt: Int) :
    DBMessage(retryCnt)

class UpdateGroupEntryResult(val success: Boolean)

class InsertMessage(val message: Message, retryCnt: Int) : DBMessage(retryCnt)
class InsertMessageResult(val success: Boolean)

class FindMessagesOfUser(val username: String, retryCnt: Int) : DBMessage(retryCnt)
class MessagesOfUserResult(list: List<Message>, success: Boolean, var user: String) :
    DBResult<List<Message>>(list, success)

class DeleteMessage(val messageID:Int, retryCnt: Int):DBMessage(retryCnt)

class DBErrorMessage(val message: DBMessage, val reason: String)
class ConnectionSuccess(val username: String, val password: String, val create: String, val wsUpgrade: WebSocketUpgrade)
