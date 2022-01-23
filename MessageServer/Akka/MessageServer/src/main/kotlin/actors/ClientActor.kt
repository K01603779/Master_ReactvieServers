package actors

import akka.actor.OneForOneStrategy
import akka.actor.SupervisorStrategy
import akka.actor.UntypedAbstractActor
import akka.event.Logging
import akka.event.LoggingAdapter
import akka.http.javadsl.model.ws.TextMessage
import akka.japi.pf.DeciderBuilder
import akka.stream.testkit.TestPublisher
import messages.Message
import java.time.Duration
import akka.http.javadsl.model.ws.Message as WsMessage

/**
 * Actor that handles an connected client
 */
class ClientActor(val name: String, private val probe: TestPublisher.Probe<WsMessage>) : UntypedAbstractActor() {
    private val log: LoggingAdapter = Logging.getLogger(context.system, this)

    private var strategy: SupervisorStrategy? = OneForOneStrategy(
        10,
        Duration.ofMinutes(1),
        DeciderBuilder
            .match(ArithmeticException::class.java) {
                SupervisorStrategy.escalate() as SupervisorStrategy.Directive?
            }
            .matchAny {
                SupervisorStrategy.resume() as SupervisorStrategy.Directive?
            }.build()
    )

    override fun supervisorStrategy(): SupervisorStrategy? {
        return strategy
    }

    override fun onReceive(message: Any?) {
        //log.info("received messages ${message.toString()}")
        when (message) {
            is Message -> handleMessage(message)
            else -> {
                log.error("unsupported message")
            }
        }
    }

    // Forward message to the websocket
    private fun handleMessage(message: Message) {
        //log.info("received message $message")
        probe.sendNext(parseMessage(message))
    }

    // Parses message into a TextMessage (used by Akkas Websocket implementation)
    private fun parseMessage(message: Message): WsMessage {
        //log.info("parsed message $message")
        return TextMessage.create(message.toString())
    }


}
