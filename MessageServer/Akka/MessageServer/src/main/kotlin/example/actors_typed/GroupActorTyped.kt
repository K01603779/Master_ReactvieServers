package example.actors_typed

import akka.actor.typed.Behavior
import akka.actor.typed.javadsl.AbstractBehavior
import akka.actor.typed.javadsl.ActorContext
import akka.actor.typed.javadsl.Behaviors
import akka.actor.typed.javadsl.Receive
import messages.*


class GroupActor(
    context: ActorContext<Message>,
    val groupID: String,
    private val members: HashSet<String>,
    private val invitees: HashSet<String>,
    private val parent :ServerActor
) : AbstractBehavior<Message>(context) {
    override fun createReceive(): Receive<Message> {
        return newReceiveBuilder()
            .onMessage(PrivateMessage::class.java, this::handlePrivateMessage)
            .onMessage(InviteToGroup::class.java, this::handleInvite)
            .onMessage(AcceptRequest::class.java, this::handleAccept)
            .onMessage(DeclineRequest::class.java, this::handleDecline)
            .onMessage(LeaveGroup::class.java, this::handleLeave)
            .build()
    }

    private fun handlePrivateMessage(message: PrivateMessage): Behavior<Message> {
        println("Group $groupID handle GroupMsg ${message.senderID} content ${message.content}")
        for (member in members) {
            var msg = PrivateMessage(senderID = "$groupID->${message.senderID}", member, message.content)
            sendMessage(msg)
        }
        return this
    }

    private fun handleInvite(message: InviteToGroup): Behavior<Message> {
        println("Group $groupID handle Invite ${message.content} ")
        if (!(members.contains(message.content) && invitees.contains(message.content))) {
            invitees.add(message.content)
            sendMessage(InviteToGroup(groupID, message.content, message.content))
        }
        return this
    }

    private fun handleAccept(message: AcceptRequest): Behavior<Message> {
        println("Group $groupID handle Accept ${message.senderID} ")
        if (invitees.contains(message.senderID)) {
            members.add(message.senderID)
            invitees.remove(message.senderID)
        }
        return this
    }

    private fun handleDecline(message: DeclineRequest): Behavior<Message> {
        println("Group $groupID handle Decline ${message.senderID} ")
        invitees.remove(message.senderID)
        return this
    }

    private fun handleLeave(message: LeaveGroup): Behavior<Message> {
        println("Group $groupID handle Leave ${message.senderID} ")
        members.remove(message.senderID)
        return this
    }

    private fun sendMessage(message: Message) {
        //TODO sendMessage to Parent
        //val parentActorRef: ActorRef = context.classicActorContext().parent()
       //(parentActorRef ! message)
        parent.context.self.tell(message); // TODO check if correct
    }


}

fun createGroupActor(groupID: String, members: HashSet<String>, invitees: HashSet<String>,actor: ServerActor): Behavior<Message> {
    return Behaviors.setup { context -> GroupActor(context, groupID, members, invitees,actor) }
}