import actors.ServerActor
import akka.actor.ActorRef
import akka.actor.ActorSystem
import akka.actor.Props
import akka.event.Logging
import akka.event.LoggingAdapter
import akka.http.javadsl.Http
import akka.http.javadsl.ServerBinding
import akka.http.javadsl.model.AttributeKeys.webSocketUpgrade
import akka.http.javadsl.model.HttpRequest
import akka.http.javadsl.model.HttpResponse
import akka.http.javadsl.model.StatusCodes
import akka.pattern.Patterns
import akka.stream.Attributes
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import messages.ConnectionSuccess
import org.apache.log4j.BasicConfigurator
import org.apache.log4j.lf5.LogLevel
import scala.concurrent.Await
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.concurrent.CompletionStage
import java.util.concurrent.TimeUnit

fun main() {
    startServer()
}

fun startServer() {
    BasicConfigurator.configure()
    val myConfig = ConfigFactory.parseString("akka.http.server.idle-timeout = 3000 s")
    val regularConfig = ConfigFactory.load()
    val combined = myConfig.withFallback(regularConfig)
    val complete = ConfigFactory.load(combined)
    // Creates the ActorSystem
    val testSystem = ActorSystem.create("message-server", complete)
    testSystem.eventStream.setLogLevel(Logging.ErrorLevel())

    val serverActor = testSystem.actorOf(Props.create(ServerActor::class.java), "serverActor")

    try {
        val handler: (HttpRequest) -> HttpResponse = {
            handleWebSocketRequests(it, testSystem, serverActor)
        }
        val serverBindingFuture: CompletionStage<ServerBinding> = Http.get(testSystem)
            .newServerAt("localhost", 8080)
            .bindSync(handler) // Bind the Websocket Handler to Akka's Http Service
        serverBindingFuture.toCompletableFuture()[3, TimeUnit.SECONDS]
        println("Press ENTER to stop.")
        BufferedReader(InputStreamReader(System.`in`)).readLine()
    } finally {
        testSystem.terminate()
    }
}

fun handleWebSocketRequests(request: HttpRequest, system: ActorSystem, serverActor: ActorRef): HttpResponse {
    if (request.uri.path().equals(("/greeter"))) {
        // Get username,password and create from the RequestParameter
        var userHeader = request.getHeader("username")
        var passwordHeader = request.getHeader("password")
        var createHeader = request.getHeader("create")
        var username = if (userHeader.isPresent) userHeader.get().value() else ""
        var password = if (passwordHeader.isPresent) passwordHeader.get().value() else ""
        var create = if (createHeader.isPresent) createHeader.get().value() else ""
        println(username)
        return request.getAttribute(webSocketUpgrade)
            .map {
                // Receive the WebSocketUpgrade
                val timeout = Timeout(50000, TimeUnit.MILLISECONDS)
                var future = Patterns.ask(serverActor, ConnectionSuccess(username, password, create, it), timeout) // ask -> Send Message to Akka and await response
                var obj = Await.result(future, timeout.duration())
                obj as HttpResponse
            }.orElse(
                HttpResponse.create().withStatus(StatusCodes.BAD_REQUEST).withEntity("Expected WebSocket request")
            )
    } else {
        return HttpResponse.create().withStatus(404)
    }
}