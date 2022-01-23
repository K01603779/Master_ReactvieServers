package actors.introduction

import akka.actor.*
import akka.japi.pf.DeciderBuilder
import messages.CreateGroup
import java.sql.SQLException
import java.time.Duration

class SimpleServerActor : UntypedAbstractActor() {
    override fun onReceive(message: Any) {
        when (message) {
            is CreateGroup -> {
                context.actorOf(Props.create(SimpleGroupActor::class.java), message.content)
            }
            else -> println("Unknown Message")
        }
    }

    override fun supervisorStrategy(): SupervisorStrategy {
        return AllForOneStrategy(
            3,
            Duration.ofSeconds(10),
            DeciderBuilder
                .match(UnknownMessage::class.java) {
                    //Log unknown message
                    SupervisorStrategy.resume() as SupervisorStrategy.Directive
                }
                .match(SQLException::class.java) {
                    // restart DB-connections
                    SupervisorStrategy.restart() as SupervisorStrategy.Directive
                }
                .build())
    }
}
