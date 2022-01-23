package example.actors_typed

import akka.NotUsed
import akka.actor.ActorSystem
import akka.http.javadsl.Http
import akka.http.javadsl.ServerBinding
import akka.http.javadsl.model.AttributeKeys
import akka.http.javadsl.model.HttpRequest
import akka.http.javadsl.model.HttpResponse
import akka.http.javadsl.model.StatusCodes
import akka.http.javadsl.model.ws.Message
import akka.http.javadsl.model.ws.TextMessage
import akka.http.javadsl.model.ws.TextMessage.create
import akka.japi.JavaPartialFunction
import akka.stream.javadsl.Flow
import akka.stream.javadsl.Source
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.concurrent.CompletionStage
import java.util.concurrent.TimeUnit


class WebsocketExample {


}

public fun handleRequest(request : HttpRequest): HttpResponse {
  println("Handling request to ${request.uri}");
    if(request.uri.path().equals(("/greeter"))){

        var result = request.getAttribute(AttributeKeys.webSocketUpgrade)
            .map {
                var greeterFlow= greeter()
                var response: HttpResponse = it.handleMessagesWith(greeterFlow)
                response
            }.orElse(
                HttpResponse.create().withStatus(StatusCodes.BAD_REQUEST).withEntity("Expected WebSocket request")
            )
        return result;
    }else{
        return HttpResponse.create().withStatus(404);
    }
}

public fun greeter() : Flow<Message, Message, NotUsed> {
   return Flow.create<Message>().collect(MyFun());
}


private class MyFun : JavaPartialFunction<Message, Message>() {
    override fun apply(msg: Message, isCheck: Boolean): Message? {
        if (isCheck) {
            if (msg.isText) {
                return null;
            } else {
                throw noMatch();
            }
        } else {
            return handleTextMessage(msg.asTextMessage());
        }
    }
}

private fun handleTextMessage(msg: TextMessage): Message {
    if (msg.isStrict) // optimization that directly creates a simple response...
    {
        return create(msg.getStrictText());
    } else // ... this would suffice to handle all text messages in a streaming fashion
    {
        return create(Source.single("Hello ").concat(msg.getStreamedText()));
    }
}


fun main(args: Array<String>) {
    val system: ActorSystem = ActorSystem.create()
    try {
        val handler: (HttpRequest) -> HttpResponse = {
                handleRequest(it)
            };
        val serverBindingFuture: CompletionStage<ServerBinding> = Http.get(system)
            .newServerAt("localhost", 8080)
            .bindSync(handler)

        // will throw if binding fails
        serverBindingFuture.toCompletableFuture()[3, TimeUnit.SECONDS]
        println("Press ENTER to stop.")
        BufferedReader(InputStreamReader(System.`in`)).readLine()
    } finally {
        system.terminate()
    }
}

