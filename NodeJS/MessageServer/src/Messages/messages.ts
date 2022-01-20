
enum MsgType {
    Error = 0,
    Private = 1,
    CreateGroup,
    InviteToGroup,
    DeclineRequest,
    AcceptRequest,
    LeaveGroup,
    RemoveGroup

}
class Message {
    senderID: string;
    receiverID: string;
    type: MsgType;
    content: string;
    constructor(senderID: string, receiverID: string) {
        this.senderID = senderID;
        this.receiverID = receiverID;
    }
}

class ErrorMessage extends Message {
    type = MsgType.Error
    constructor(senderID: string, receiverID: string, text: string) {
        super(senderID, receiverID);
        this.content = text;
    }
}


class PrivateMessage extends Message {
    type = MsgType.Private
    constructor(senderID: string, receiverID: string, text: string) {
        super(senderID, receiverID);
        this.content = text;
    }
}
class InviteToGroup extends Message {
    type = MsgType.InviteToGroup;
    constructor(senderID: string, receiverID: string, user: string) {
        super(senderID, receiverID);
        this.content = user;
    }
}
class DeclineRequest extends Message {
    type = MsgType.DeclineRequest;
    constructor(senderID: string, group: string) {
        super(senderID, group);
    }
}
class AcceptRequest extends Message {
    type = MsgType.AcceptRequest;
    constructor(senderID: string, group: string) {
        super(senderID, group);
    }
}
class LeaveGroup extends Message {
    type = MsgType.LeaveGroup;
    constructor(senderID: string, group: string) {
        super(senderID, group);
    }
}
class RemoveGroup extends Message {
    type = MsgType.RemoveGroup;
    constructor(senderID: string, groupname: string) {
        super(senderID, "server");
        this.content = groupname;
    }
}
class CreateGroup extends Message {
    type = MsgType.CreateGroup;
    constructor(senderID: string, groupname: string) {
        super(senderID, "server");
        this.content = groupname;
    }
}



export { MsgType, Message, PrivateMessage, InviteToGroup, DeclineRequest, AcceptRequest, LeaveGroup, RemoveGroup, CreateGroup, ErrorMessage };