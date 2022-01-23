package at.mh.kotlin.message.server.manager

import at.mh.kotlin.message.server.messages.Message
import io.ktor.http.cio.websocket.*
import io.ktor.websocket.*
import io.reactivex.rxjava3.core.Observer
import kotlinx.coroutines.runBlocking
import org.apache.logging.log4j.LogManager

class ClientManager(private val username: String, private val wsConnection: DefaultWebSocketServerSession) : Manager() {
    private var serverMgr: Observer<in Message>? = null
    private val logger = LogManager.getLogger("ClientManager")

    override fun subscribeActual(manager: Observer<in Message>?) {
        this.serverMgr = manager
    }


    override fun onNext(message: Message) {
        sendMsg(message)
    }

    fun sendMsg(message: Message) {
        logger.info(" $username received message $message")
        runBlocking {
            wsConnection.send(message.toString())
        }
    }

    override fun onError(e: Throwable?) {
        logger.error("$username received onError ${e?.message}")
    }

    override fun onComplete() {
        logger.info("$username completed")
    }

    fun receiveMessage(message: Message) {
        logger.info("$username Forward WS message to Observer")
        this.serverMgr?.onNext(message)
    }
}