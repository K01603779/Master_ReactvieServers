package example.old

import akka.actor.typed.Behavior
import akka.actor.typed.javadsl.AbstractBehavior
import akka.actor.typed.javadsl.ActorContext
import akka.actor.typed.javadsl.Behaviors
import akka.actor.typed.javadsl.Receive
import akka.stream.testkit.TestPublisher
import akka.http.javadsl.model.ws.Message as WsMessage

class oldClientActor(
    context: ActorContext<WsMessage>,
    val probe: TestPublisher.Probe<WsMessage>
) : AbstractBehavior<WsMessage>(context) {


    override fun createReceive(): Receive<WsMessage> {
        return newReceiveBuilder()
            .onMessage(WsMessage::class.java, this::handleMessage)
            .build()
    }
    private fun handleMessage(message: WsMessage):Behavior<WsMessage>{
        probe.sendNext(message)
        return this;
    }

}

fun createClientActor(probe: TestPublisher.Probe<WsMessage>): Behavior<WsMessage> {
    return Behaviors.setup { context -> oldClientActor(context,probe) }
}
