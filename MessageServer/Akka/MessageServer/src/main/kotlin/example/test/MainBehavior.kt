package example.test

import akka.actor.typed.Behavior
import akka.actor.typed.javadsl.AbstractBehavior
import akka.actor.typed.javadsl.ActorContext
import akka.actor.typed.javadsl.Behaviors
import akka.actor.typed.javadsl.Receive

class MainBehavior(context :ActorContext<String>) : AbstractBehavior<String>(context){

    override fun createReceive(): Receive<String> {
        return newReceiveBuilder().onMessageEquals("start", this::start).build();
    }

    private fun  start() :Behavior<String>{

        val firstRef = context.spawn(createPrintMyActor(),"first-actor");
        println("First $firstRef");
        firstRef.tell("printit");
        return Behaviors.same();
    }

}

fun createMainBehaivor() : Behavior<String> {
    return Behaviors.setup(::MainBehavior);
}