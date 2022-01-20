package example.actors_typed

import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.javadsl.AbstractBehavior
import akka.actor.typed.javadsl.ActorContext
import akka.actor.typed.javadsl.Behaviors
import akka.actor.typed.javadsl.Receive
import akka.stream.testkit.TestPublisher
import messages.ConnectionSuccess
import messages.CreateGroup
import messages.Message
import messages.RemoveGroup

class ServerActor(context: ActorContext<Any>) : AbstractBehavior<Any>(context) {
    private val actors = HashMap<String, ActorRef<Any>>() // Todo ActorRef<Message>

    /*
    TODO error handling // actor rebuilding
    TODO connection / disconnection of clients
    TODO DB Statements
    TODO Hierarchy
    TODO instead of actors try to get the actor from the context ?
     */
    override fun createReceive(): Receive<Any> {
        return newReceiveBuilder()
            .onMessage(RemoveGroup::class.java, this::handleRemoveGroup)
            .onMessage(CreateGroup::class.java, this::handleCreateGroup)
            .onMessage(Message::class.java, this::handleMessage)
            .onMessage(ConnectionSuccess::class.java,this::handleCreateUser)
            // TODO connection-created
            // TODO disconnect
            .build()

    }

    private  fun handleCreateUser(message:ConnectionSuccess):Behavior<Any>{
        //val userActor = context.spawn(createClientActor(message.probe),"TODO")
        //val userActor = createUser(message.probe)
        //actors.putIfAbsent("TODO",userActor as ActorRef<Any>) // TODO
        return this
    }
    fun createUser(probe: TestPublisher.Probe<akka.http.javadsl.model.ws.Message>): ActorRef<akka.http.javadsl.model.ws.Message>? {
        return context.spawn(createClientActor(probe), "TODO")
    }

    private fun handleCreateGroup(message: CreateGroup): Behavior<Any> {
        val groupActor = context.spawn(
            createGroupActor(message.content, HashSet<String>(), HashSet<String>(), this),
            message.content
        )
        actors.putIfAbsent(message.content, groupActor as ActorRef<Any>) // TODO
        println("ServerActor : received CreateGroup senderID: ${message.senderID} receiverID:${message.receiverID} content ${message.content}")
        return this
    }

    private fun handleRemoveGroup(message: RemoveGroup): Behavior<Any> {
        println("ServerActor: received RemovedGroup from ${message.senderID} Message send to ${message.receiverID} content ${message.content} type ${message.type}")
        return this
    }

    private fun handleMessage(message: Message): Behavior<Any> {
        println("ServerActor: received Message from ${message.senderID} Message send to ${message.receiverID} content ${message.content} type ${message.type}")
        actors.get(message.receiverID)?.tell(message)
        return this
    }

}

fun createServerActor(): Behavior<Any> {
    return Behaviors.setup(::ServerActor)
}