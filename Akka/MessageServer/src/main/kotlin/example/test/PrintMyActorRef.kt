package example.test

import akka.actor.typed.Behavior
import akka.actor.typed.javadsl.AbstractBehavior
import akka.actor.typed.javadsl.ActorContext
import akka.actor.typed.javadsl.Behaviors
import akka.actor.typed.javadsl.Receive

class PrintMyActorRef(context: ActorContext<String>) : AbstractBehavior<String>(context) {


    override fun createReceive(): Receive<String> {
        return newReceiveBuilder().onMessageEquals("printit",this::printIt).build();
    }

    private fun printIt() : Behavior<String> {
        val secondRef = context.spawn(Behaviors.empty<String>(),"second-actor");
        println("Second $secondRef");
        return this;
    }
}
fun createPrintMyActor() : Behavior<String>{
    return Behaviors.setup(::PrintMyActorRef);
}