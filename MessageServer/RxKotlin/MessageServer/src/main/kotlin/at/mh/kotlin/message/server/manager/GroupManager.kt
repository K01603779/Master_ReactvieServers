package at.mh.kotlin.message.server.manager

import at.mh.kotlin.message.server.db.DBConnR2DBC
import at.mh.kotlin.message.server.messages.*
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.rxjava3.core.Observer
import org.apache.logging.log4j.LogManager
import java.util.concurrent.TimeUnit

/**
 * Handles all the messages of a Group
 */
class GroupManager(
    private val groupName: String,
    val groupCreator: String,
    private val members: HashSet<String>,
    private val invitees: HashSet<String>,
    private val db: DBConnR2DBC,
    private val serverManager: ServerManager
) : Manager() {
    private val logger = LogManager.getLogger("GroupManager")
    private var serverMgr: Observer<in Message>? = null
    private var disposable: Disposable? = null

    /**
     * can be used to remove Groups if they haven't received messages for a specified amount of time
     */
    private fun startTimeout(timeInMinutes: Long) {
        disposable?.dispose()
        disposable = Observable.timer(timeInMinutes, TimeUnit.MINUTES).doOnNext {
            logger.error("$groupName hasn't received messages since $timeInMinutes min removing him from active list")
            this.serverMgr?.onComplete()
            serverManager.removeManager(groupName)
        }.subscribe()
    }

    override fun onComplete() {
    }

    /**
     * Handles all an incoming Message
     */
    override fun onNext(message: Message) {
        logger.info("$groupName received message $message")
        when (message) {
            is PrivateMessage -> sendMessageToMembers(message)
            is InviteToGroup -> inviteToGroup(message)
            is AcceptRequest -> addToGroup(message)
            is DeclineRequest -> removeFromInvitees(message)
            is LeaveGroup -> leaveGroup(message)
            else -> logger.error("Unrecognized Message $message")
        }
    }

    override fun onError(e: Throwable?) {
        logger.error("$groupName received onError ${e?.message}")
    }


    override fun subscribeActual(manager: Observer<in Message>?) {
        this.serverMgr = manager
    }

    /**
     * Sends the received message to all of its members
     */
    private fun sendMessageToMembers(message: PrivateMessage) {
        logger.info(" $groupName Private message received $message ${members.firstOrNull()}")
        members.forEach { serverMgr?.onNext(PrivateMessage(groupName, it, message.content)) }
    }

    /**
     * Initiate an invite
     */
    private fun inviteToGroup(message: InviteToGroup) {
        // check if initiator is a group member
        if (members.contains(message.senderID)) {
            logger.info("$groupName received InviteRequest")
            // check if invitee isn't already a member and hasn't already received an invite
            if (!members.contains(message.content) && !invitees.contains(message.content)) {
                // add user to invitees and update GroupEntry in DB
                db.getUser(message.content)
                    .onErrorComplete {
                        logger.info("DB-Error $it")
                        true
                    }
                    .subscribe { userID ->
                        logger.info("User found $userID")
                        db.updateGroupEntry(groupName, message.content, accepted = false)
                            .onErrorComplete {
                                logger.error("DB-Error $it")
                                false
                            }
                            .doOnComplete {
                                logger.info("Invite saved in to DB")
                                invitees.add(message.content)
                                serverMgr?.onNext(InviteToGroup(groupName, message.content, message.content))
                            }.subscribe()

                    }
            } else {
                logger.info("${message.content} already in invitees or group")
            }
        } else {
            logger.info("sender is not in members ${message.senderID}")
        }

    }

    /**
     * Add a User to the Group
     */
    private fun addToGroup(message: AcceptRequest) {
        logger.info("user accepted request $message")
        // Check if AcceptMsg was sent by an invitee
        if (invitees.contains(message.senderID) && !members.contains(message.senderID)) {
            db.updateGroupEntry(groupName, message.senderID, true)
                .onErrorComplete {
                    logger.warn("DB-Error $it")
                    false
                }
                .doOnComplete {
                    logger.info("User added to Members in DB")
                    // add to members and update GroupEntry
                    members.add(message.senderID)
                    invitees.remove(message.senderID)
                }.subscribe()
        }
    }

    /**
     * Invitee declined request
     */
    private fun removeFromInvitees(message: DeclineRequest) {
        // check if sender was an invitee and then remove them
        if (invitees.contains(message.senderID)) {
            db.removeGroupEntry(groupName, message.senderID)
                .onErrorComplete {
                    logger.warn("DB-Error $it")
                    false
                }
                .doOnComplete {
                    logger.info("Invitee removed ${message.senderID}")
                    invitees.remove(message.senderID)
                }.subscribe()
        }
    }

    /**
     * Remove member from group
     */
    private fun leaveGroup(message: LeaveGroup) {
        if (members.contains(message.senderID)) {
            db.removeGroupEntry(groupName, message.senderID)
                .onErrorComplete {
                    logger.warn("DB-Error $it")
                    false
                }.doOnComplete {
                    logger.info("User left group successfully ${message.senderID}")
                    members.remove(message.senderID)
                }
        }
    }
}