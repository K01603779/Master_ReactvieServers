package at.mh.kotlin.message.server

import at.mh.kotlin.message.server.manager.ServerManager
import at.mh.kotlin.message.server.manager.ClientManager
import at.mh.kotlin.message.server.messages.*
import com.google.gson.Gson
import io.ktor.application.*
import io.ktor.http.cio.websocket.*
import io.ktor.routing.*
import io.ktor.websocket.*
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.core.LoggerContext
import org.apache.logging.log4j.core.config.Configuration
import org.apache.logging.log4j.core.config.LoggerConfig


fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

/**
 * uses KTOR
 * https://ktor.io/
 */
@Suppress("unused")
fun Application.module() {
    val ctx: LoggerContext = LogManager.getContext(false) as LoggerContext
    val config: Configuration = ctx.configuration
    val loggerConfig: LoggerConfig = config.getLoggerConfig(LogManager.ROOT_LOGGER_NAME)
    loggerConfig.level = Level.WARN
    ctx.updateLoggers()
    val logger = LogManager.getRootLogger()
    logger.info("Starting Server ..... ")
    val serverManager = ServerManager()
    install(WebSockets)
    routing {
        webSocket("/chat") {
            val username = call.request.headers["username"]
            val password = call.request.headers["password"] ?: ""
            val create = call.request.headers["create"]
            val manager: ClientManager? = if (create == "true") {
                username?.let {
                    serverManager.registerUserManager(it, password, this)
                }
            } else {
                username?.let {
                    serverManager.loginUserManager(it, password, this)
                }
            }
            for (frame in incoming) {
                frame as? Frame.Text ?: continue
                val receivedText = frame.readText()
                logger.info("Received Message $receivedText")
                manager?.receiveMessage(parseMessage(receivedText)!!)
            }
            logger.info("Closed")
            serverManager.closeManager(username ?: "")

        }
    }
}

private fun parseMessage(message: String): Message? {
    var hashMap: Map<String, Any> = HashMap()
    hashMap = Gson().fromJson(message, hashMap.javaClass)
    val senderID = hashMap["senderID"].toString()
    val receiverID = hashMap["receiverID"].toString()
    val content = hashMap["content"].toString()
    return when (hashMap["type"]) {
        1.0 -> PrivateMessage(senderID, receiverID, content)
        2.0 -> CreateGroup(senderID, content)
        3.0 -> InviteToGroup(senderID, receiverID, content)
        4.0 -> DeclineRequest(senderID, receiverID)
        5.0 -> AcceptRequest(senderID, receiverID)
        6.0 -> LeaveGroup(senderID, receiverID)
        7.0 -> RemoveGroup(senderID, content)
        else -> null
    }
}