package example.actors_typed

import akka.actor.typed.ActorSystem
import akka.http.javadsl.Http
import akka.http.javadsl.ServerBinding
import akka.http.javadsl.model.AttributeKeys
import akka.http.javadsl.model.HttpRequest
import akka.http.javadsl.model.HttpResponse
import akka.http.javadsl.model.StatusCodes
import akka.http.javadsl.model.ws.WebSocketUpgrade
import akka.stream.javadsl.Flow
import akka.stream.javadsl.Sink
import akka.stream.javadsl.Source
import akka.stream.testkit.TestPublisher
import messages.*
import org.apache.log4j.BasicConfigurator
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.concurrent.CompletionStage
import java.util.concurrent.TimeUnit
import akka.http.javadsl.model.ws.Message as WsMessage

fun main(args: Array<String>) {
    mainWebsocket(args)
}


fun main1(args: Array<String>) {
    BasicConfigurator.configure()
    val testSystem = ActorSystem.create(createServerActor(), "message-server")
    testSystem.tell(PrivateMessage("max","server","hallo"));
    testSystem.tell(CreateGroup("max", "Group1"))
    testSystem.tell(CreateGroup("max", "Group2"))
    testSystem.tell(RemoveGroup("max", "Group1"))
    testSystem.tell(InviteToGroup(senderID = "max", receiverID = "Group1", "max"))
    testSystem.tell(InviteToGroup(senderID = "max", receiverID = "Group1", "marcel"))
    testSystem.tell(PrivateMessage("max", "Group1", "Hallo wie geht's"))
    testSystem.tell(PrivateMessage("Marcel", "Group2", "Hallo wie gehts"))
    testSystem.tell(AcceptRequest(senderID = "max", groupID = "Group1"))
    testSystem.tell(AcceptRequest(senderID = "marcel", groupID = "Group1"))
    testSystem.tell(PrivateMessage("max", "Group1", "Hallo wie geht's euch allen"))
}


fun main2(args: Array<String>) {
    BasicConfigurator.configure()
    //val testSystem = ActorSystem.create(createMainBehaivor(), "testSystem")
    //testSystem.tell("start")
}

fun mainWebsocket(args:Array<String>){
    BasicConfigurator.configure()
    //val serverActor = createServerActor() // TODO ?
    //val serverActor = testSystem.actorOf(createServerActor(),"serveractor");
    val testSystem = ActorSystem.create(createServerActor(), "message-server")
    //testSystem.
    println(testSystem.printTree())
    try {
        val handler: (HttpRequest) -> HttpResponse = {
            handleRequest(it,testSystem,testSystem as ServerActor )
        };
        val serverBindingFuture: CompletionStage<ServerBinding> = Http.get(testSystem)
            .newServerAt("localhost", 8080)
            .bindSync(handler)

        // will throw if binding fails
        serverBindingFuture.toCompletableFuture()[3, TimeUnit.SECONDS]
        println("Press ENTER to stop.")
        BufferedReader(InputStreamReader(System.`in`)).readLine()
    } finally {
        testSystem.terminate()
    }
}

public fun handleRequest(request: HttpRequest,system: ActorSystem<Any>, serverActor: ServerActor): HttpResponse {
    println("Handling request to ${request.uri}");
    if(request.uri.path().equals(("/greeter"))) {

        return request.getAttribute(AttributeKeys.webSocketUpgrade)
            .map {
                println("Websockets upgrade received");
                //var greeterFlow = greeter(system)
                //system.tell(ConnectionSuccess(greeterFlow.second))
                //var response: HttpResponse = it.handleMessagesWith(greeterFlow.first)
                greeter(it,system,serverActor)
            }.orElse(
                HttpResponse.create().withStatus(StatusCodes.BAD_REQUEST).withEntity("Expected WebSocket request")
            );
    }else{
        return HttpResponse.create().withStatus(404);
    }
}

public fun greeter(wsUpgrade: WebSocketUpgrade,system: ActorSystem<Any>,serverActor: ServerActor) : HttpResponse { // TODO WsMessage
    println("Greeter called");

    var actorProbe = TestPublisher.probe<WsMessage>(0,system.classicSystem()) // TODO Context ?
    //var actorRef = ActorSystem.create(createClientActor(actorProbe),"Test");
    var actorRef = (serverActor).createUser(actorProbe);
    var sink = Sink.foreach<WsMessage> {
        actorRef?.tell(it)
    } // TODO
    var flow = Flow.fromSinkAndSource(sink, Source.fromPublisher(actorProbe))

    return wsUpgrade.handleMessagesWith(flow);
}