package example

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
import akka.stream.javadsl.Sink
import akka.stream.javadsl.Source
import akka.stream.testkit.TestPublisher
import example.old.handleRequest
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.concurrent.CompletionStage
import java.util.concurrent.TimeUnit


class WebsocketExample {


}

public fun handleRequest(request: HttpRequest, system: akka.actor.typed.ActorSystem<Any>): HttpResponse {
  println("Handling request to ${request.uri}");
    if(request.uri.path().equals(("/greeter"))) {

        return request.getAttribute(AttributeKeys.webSocketUpgrade)
            .map {
                println("Websocket upgrade received");
                //var greeterFlow = greeter(system)
                //var source = Source.from
                //var sink
                //it.handleMessagesWith()
                //var response: HttpResponse = it.handleMessagesWith(greeterFlow.first)
                //Timer("Settingup",false).schedule(5000){
                //   greeterFlow.second.sendNext(TextMessage.create("Hallo wie ghets"))
                //}
                //response
                HttpResponse.create().withStatus(404);
            }.orElse(
                HttpResponse.create().withStatus(StatusCodes.BAD_REQUEST).withEntity("Expected WebSocket request")
            );
    }else{
        return HttpResponse.create().withStatus(404);
    }
}

public fun greeter(system: ActorSystem) : Pair<Flow<Message, Message, NotUsed>, TestPublisher.Probe<Message>> {
    println("Greeter called");

    var actorProbe = TestPublisher.probe<Message>(0,system) // TODO check context ??

    //var actorRef = ActorSystem.create(createClientActor(actorProbe),"Test");
    var sink = Sink.foreach<Message> {}


    var flow = Flow.fromSinkAndSource(sink,Source.fromPublisher(actorProbe))

    return Pair(flow,actorProbe)
}


private class MyFun : JavaPartialFunction<Message, Message>() {

    override fun apply(msg: Message, isCheck: Boolean): Message? {
        println("MyFun apply " + msg);
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

private fun handleTextMessage(msg: TextMessage): Message? {
    println("handleTextMessage " +msg);
    if (msg.isStrict) // optimization that directly creates a simple response...
    {
        create((msg.strictText));
        return null;
        //return create(msg.getStrictText());
    } else // ... this would suffice to handle all text messages in a streaming fashion
    {
        return create(Source.single("Hello ").concat(msg.getStreamedText()));
    }
}


fun main(args: Array<String>) {
    val system: ActorSystem = ActorSystem.create()
    try {
        val handler: (HttpRequest) -> HttpResponse = {
                handleRequest(it,system)
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

