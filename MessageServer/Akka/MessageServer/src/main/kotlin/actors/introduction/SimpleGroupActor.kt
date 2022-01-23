package actors.introduction

import akka.actor.UntypedAbstractActor
import akka.event.Logging
import akka.event.LoggingAdapter
import messages.InviteToGroup
import messages.PrivateMessage
import java.sql.SQLException

class SimpleGroupActor : UntypedAbstractActor() {
    private val log: LoggingAdapter = Logging.getLogger(context.system, this)
    override fun onReceive(message: Any) {
        when (message) {
            is PrivateMessage -> sender.tell("Echo ${message.content}", self)
            is InviteToGroup -> inviteUser(message)
            else -> throw  UnknownMessage()
        }
    }

    private fun inviteUser(message: Any) {
        throw SQLException()
    }
}

class UnknownMessage : java.lang.Exception()