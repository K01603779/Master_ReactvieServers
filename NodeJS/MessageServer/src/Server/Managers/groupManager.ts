import { Message, MsgType, InviteToGroup } from "../../Messages/messages";
import { ServerManager } from "./ServerManager";
import { Manager } from "./manager";
import { updateGroupEntiry, removeGroupEntity, getUserFromDB, retryWithOne, retryWithThree, retryWithTwo } from "../sql-connector";


export class GroupManager implements Manager {
    private serverManger: ServerManager;
    private creator: string;
    private groupID: string;
    fifo: Array<Message> = [];

    private timer = setTimeout(() => { this.removefromManager() }, 500000);
    private invitee: Map<string, string> = new Map<string, string>();
    private members: Map<string, string> = new Map<string, string>();

    constructor(groupID: string, username: string, serverManager: ServerManager, invitee: string[], accepted: string[]) {
        this.groupID = groupID;
        this.members.set(username, username);
        this.serverManger = serverManager;
        this.creator = username;
        invitee.forEach(req => {
            this.invitee.set(req, req);
        });
        accepted.forEach(acc => {
            this.members.set(acc, acc);
        });
    }

    getCreator(): string {
        return this.creator;
    }

    inviteToGroup(username: string, msg: Message) {
        if (!this.invitee.has(username) && !this.members.has(username)) {
            //getUserFromDB(username)
            retryWithOne(getUserFromDB, username) 
                .then(user => {
                    console.log(`GroupManger: ${this.groupID} has received a request to add ${username}`);
                    this.invitee.set(username, username);
                    const newMsg = new InviteToGroup(this.groupID, username, username);
                    this.serverManger.addMessagetoQueue(newMsg);
                    retryWithThree(updateGroupEntiry, this.groupID, username, false).catch(() => {
                        this.serverManger.sendBackMsg(msg);
                    });
                    //updateGroupEntiry(this.groupID, username, false);
                }).catch(error => {
                    console.log(`GroupManager : ${this.groupID} receive request for user that doesn't exists ${username}`);
                })
        }
    }

    public receiveMessage(msg: Message) {
        clearTimeout(this.timer);
        this.timer = setTimeout(() => { this.removefromManager() }, 500000);
        //setImmediate((msg) => this.handleMessage(msg), msg);
        this.handleMessage(msg);
    }

    removefromManager() {
        console.log(`GroupManager ${this.groupID}:  hasn't received any messages => removing from managers`);
        this.serverManger.removeManager(this.groupID);
    }


    handleMessage(message: Message) {
        if (message != undefined) {
            if (this.invitee.has(message.senderID) || this.members.has(message.senderID) || this.creator === message.senderID) {
                switch (message.type) {
                    case MsgType.Private:
                        this.sendMessagetoParticipants(message);
                        break;
                    case MsgType.InviteToGroup:
                        const req = (message as InviteToGroup).content;
                        this.inviteToGroup(req, message);
                        break;
                    case MsgType.AcceptRequest:
                        if (this.invitee.has(message.senderID)) {
                            console.log(`GroupManger ${this.groupID}: ${message.senderID} has accepted the request`);
                            this.members.set(message.senderID, message.senderID);
                            this.invitee.delete(message.senderID);
                            retryWithThree(updateGroupEntiry, this.groupID, message.senderID, true).catch(() => {
                                this.serverManger.sendBackMsg(message);
                            });                        
                        }
                        break;
                    case MsgType.DeclineRequest:
                        if (this.invitee.has(message.senderID)) {
                            console.log(`GroupManger ${this.groupID}: ${message.senderID} has declined the request`);
                            this.invitee.delete(message.senderID);
                            retryWithTwo(removeGroupEntity, this.groupID, message.senderID).catch(() => {
                                this.serverManger.sendBackMsg(message);
                            })
                            //removeGroupEntity(this.groupID, message.senderID);
                        }
                        break;
                    case MsgType.LeaveGroup:
                        console.log(`GroupManger ${this.groupID}: ${message.senderID} leaves the group`);
                        this.members.delete(message.senderID);
                        retryWithTwo(removeGroupEntity, this.groupID, message.senderID).catch(() => {
                            this.serverManger.sendBackMsg(message);
                        });
                        //removeGroupEntity(this.groupID, message.senderID);
                        break;
                }
            }
        }
    }

    sendMessagetoParticipants(message: Message) {
        var senderID = message.senderID;
        this.members.forEach(member => {
            const msg = Object.assign({}, message);
            msg.senderID = `${this.groupID}->${senderID}`;
            msg.receiverID = member;
            console.log(`Create Groupmessage for ${member}`);
            this.serverManger.addMessagetoQueue(msg);
        });
    }





}