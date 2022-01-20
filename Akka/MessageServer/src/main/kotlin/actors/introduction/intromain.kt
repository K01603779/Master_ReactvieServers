package actors.introduction

import akka.actor.ActorRef
import akka.actor.ActorSystem
import akka.actor.Props
import messages.CreateGroup
import messages.PrivateMessage

fun main() {
    val actorSystem = ActorSystem.create("message-server")
    val masterActorRef = actorSystem.actorOf(Props.create(SimpleServerActor::class.java), "myMaster1")
    masterActorRef.tell(CreateGroup("simple1","simple1"), ActorRef.noSender())
    masterActorRef.tell(CreateGroup("simple1","simple2"), ActorRef.noSender())
    println("Tell unknown")
    actorSystem.actorSelection(masterActorRef.path().child("simple1")).tell("Hallo", ActorRef.noSender())
    actorSystem.actorSelection(masterActorRef.path().child("simple1")).tell(PrivateMessage("","",""), ActorRef.noSender())
    actorSystem.actorSelection(masterActorRef.path().child("simple1")).tell("Hallo", ActorRef.noSender())

}