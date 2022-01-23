package example.actors_typed

import akka.actor.typed.Behavior
import akka.actor.typed.javadsl.AbstractBehavior
import akka.actor.typed.javadsl.ActorContext
import akka.actor.typed.javadsl.Behaviors
import akka.actor.typed.javadsl.Receive
import akka.http.javadsl.model.ws.Message
import akka.stream.testkit.TestPublisher

class ClientActor(
    context: ActorContext<Message>,
    val probe: TestPublisher.Probe<Message>
) : AbstractBehavior<Message>(context) {


    override fun createReceive(): Receive<Message> {
        return newReceiveBuilder()
            .onMessage(Message::class.java, this::handleMessage)
            .build()
    }
    private fun handleMessage(message: Message):Behavior<Message>{
        probe.sendNext(message)
        return this;
    }

}

fun createClientActor(probe: TestPublisher.Probe<Message>): Behavior<Message> {
    return Behaviors.setup { context -> ClientActor(context,probe) }
}
