package at.mh.kotlin.message.server.messages

import com.google.gson.JsonObject

enum class MessageType {
    Error,
    Private,
    CreateGroup,
    InviteToGroup,
    DeclineRequest,
    AcceptRequest,
    LeaveGroup,
    RemoveGroup

}

abstract class Message(var senderID: String, var receiverID: String, var content: String, var type: MessageType) {
    override fun toString(): String {
        val json = JsonObject()
        json.addProperty("senderID", senderID)
        json.addProperty("receiverID", receiverID)
        json.addProperty("content", content)
        json.addProperty("type", type.ordinal)
        return json.toString()
    }
}

class ErrorMessage(senderID: String, receiverID: String, text: String) :
    Message(senderID, receiverID, text, MessageType.Error)

class PrivateMessage(senderID: String, receiverID: String, content: String) :
    Message(senderID, receiverID, content, MessageType.Private)

class InviteToGroup(senderID: String, receiverID: String, inviteeID: String) :
    Message(senderID, receiverID, inviteeID, MessageType.InviteToGroup)

class DeclineRequest(senderID: String, groupID: String) : Message(senderID, groupID, "", MessageType.DeclineRequest)

class AcceptRequest(senderID: String, groupID: String) : Message(senderID, groupID, "", MessageType.AcceptRequest)

class LeaveGroup(senderID: String, groupID: String) : Message(senderID, groupID, "", MessageType.LeaveGroup)

class RemoveGroup(senderID: String, groupID: String) : Message(senderID, "server", groupID, MessageType.RemoveGroup)

class CreateGroup(senderID: String, groupID: String) :
    Message(senderID, "server", groupID, MessageType.CreateGroup)


