package actors

import akka.actor.ActorRef
import akka.actor.UntypedAbstractActor
import akka.event.Logging
import akka.event.LoggingAdapter
import messages.*


/**
 * Handles all the messages from a Group
 */
class GroupActor(
    private val groupName: String,
    val creator: String,
    private val members: HashSet<String>,
    private val invitees: HashSet<String>,
    private val dbRouter: ActorRef
) : UntypedAbstractActor() {
    private val retryCnt = 3
    private val log: LoggingAdapter = Logging.getLogger(context.system, this)


    override fun onReceive(message: Any?) {
        log.info("$groupName received message $message")
        when (message) {
            is Message -> {
                when (message) {
                    is PrivateMessage -> sendMessageToMembers(message)
                    is InviteToGroup -> inviteToGroup(message)
                    is AcceptRequest -> addToGroup(message)
                    is DeclineRequest -> removeFromInvitees(message)
                    is LeaveGroup -> leaveGroup(message)
                }
            }
            is FindUserResult -> findUserResult(message)
            else -> log.error("received unknown  msg type $message")
        }
    }

    private fun addToGroup(msg: AcceptRequest) {
        log.info("user accepted request $msg")
        if (invitees.contains(msg.senderID) && !members.contains(msg.senderID)) {
            members.add(msg.senderID)
            invitees.remove(msg.senderID)
            dbRouter.tell(UpdateGroupEntry(groupName, msg.senderID, true, retryCnt), self)
        }
    }

    private fun removeFromInvitees(msg: DeclineRequest) {
        log.info("GroupActor $groupName received Decline $msg")
        if (invitees.contains(msg.senderID)) {
            invitees.remove(msg.senderID)
            dbRouter.tell(DeleteGroupEntry(groupName, msg.senderID, retryCnt), self)
        }
    }

    private fun inviteToGroup(msg: InviteToGroup) {
        if (members.contains(msg.senderID)) {
            log.info("GroupActor $groupName received InviteRequest")
            val s = sender
            if (!members.contains(msg.content) && !invitees.contains(msg.content)) { // if invitee isn't already a member check if user exists in DB
                dbRouter.tell(
                    FindUser(msg.senderID, retryCnt, s, msg),
                    self
                ) // Sends message to DBActor asking for the User see findUserResult
            }
        }
    }


    private fun leaveGroup(msg: LeaveGroup) {
        log.info("user wants to leave group $msg")
        if (members.contains(msg.senderID)) {
            members.remove(msg.senderID)
            dbRouter.tell(DeleteGroupEntry(groupName, msg.senderID, retryCnt), self)
        }
    }

    /**
     * send a message to all of the groups members
     */
    private fun sendMessageToMembers(message: PrivateMessage) {
        if (members.contains(message.senderID)) {
            log.info("Group $groupName send $message to each member")
            members.forEach {
                val msg = PrivateMessage(groupName, it, message.content)
                this.sender.tell(msg, self)
            }
        }
    }

    /**
     * adds user to invitees if user exists in the DB
     */
    private fun findUserResult(message: FindUserResult) {
        if (message.success && message.message is Message) {
            invitees.add(message.message.content)
            val inv = InviteToGroup(groupName, message.message.content, message.message.content)
            dbRouter.tell(UpdateGroupEntry(groupName, inv.content, false, retryCnt), self)
            message.actorRef.tell(inv, this.self())
        } else {
            log.error("user not found ${message.result}")
        }
    }


}