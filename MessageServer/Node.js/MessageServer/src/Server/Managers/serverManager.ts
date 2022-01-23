//import  "../Messages/messages";
import { CreateGroup, ErrorMessage, Message, MsgType, RemoveGroup } from "../../Messages/messages";
import { Manager } from "./manager";
import { GroupManager } from "./groupManager";
import { ClientManager } from "./clientManager";
import { createGroup, deleteEntity, getGroup, getContact, storeUserMsg, retryWithOne } from "../sql-connector";
import { Group, User } from "../../Entities/entities";

export class ServerManager {
    private managers = new Map<string, Manager>();
    //private fifo: Array<{user : string, message: Message }> = new Array<{ user: string, message: Message }>();


    addMessagetoQueue(message: Message) {
        //this.fifo.push(msg);
        console.log(`ServerManager setImmeditate msg ${JSON.stringify(message)}`);
        //setImmediate(() =>{this.handleMessage(msg),msg);
        this.handleMsg(message);
    }

    addManagers(username: string, manager: Manager): boolean {
        if (!this.managers.has(username)) {
            console.log(`ServerManager: Add ${username} to managers`);
            this.managers.set(username, manager);
            return true;
        }
        return false;
    }
    removeManager(username: string) {
        console.log(`ServerManager: Removed ${username} from  managers`);
        var manager = this.managers.get(username);
        this.managers.delete(username);
        // Store undeliverd Messages into Database
        if (manager instanceof ClientManager) {
            // manager.storeMessagesinDB(); TODO
        }
    }

    handleMsg(message: Message) {
        console.log(`ServerManager: Handle Message ${JSON.stringify(message)}`);
        if (message != undefined) {
            console.log(`ServerManager: Send Message to ClientManger ${JSON.stringify(message)}`);
            if (message.receiverID === "server") {
                if (message.type === MsgType.CreateGroup) {
                    const msg: CreateGroup = message as CreateGroup;
                    this.createGroup(msg);
                } else if (message.type === MsgType.RemoveGroup) {
                    const msg: RemoveGroup = message as RemoveGroup;
                    this.deleteGroup(msg);
                }
            }
            else if (this.managers.has(message.receiverID)) {
                this.managers.get(message.receiverID).handleMessage(message);
            } else {
                this.storeMsg(message);
            }
        }

    }

    containsUser(key: string): boolean {
        return this.managers.has(key);
    }

    private deleteGroup(msg: RemoveGroup) {
        const manager = this.managers.get(msg.content);
        console.log(`Request to remove ${JSON.stringify(msg)}`);
        if (manager != null) {
            console.log(`${(manager as GroupManager).getCreator()}  === ${msg.senderID}  [${(manager as GroupManager).getCreator() === msg.senderID}]`);
            if ((manager as GroupManager).getCreator() === msg.senderID) {
                console.log(`ServerManager: Group ${msg.content} has been removed by creator`);
                this.managers.delete(msg.content);
                retryWithOne(deleteEntity, msg.content).catch(() => {
                    this.sendBackMsg(msg);
                });
            }
        } else {
            //getGroup(msg.content)
            retryWithOne(getGroup, msg.content)
                .then(group => {
                    if (group.manager == msg.senderID) {
                        deleteEntity(group.id);
                    }
                }).catch(() => {
                    this.sendBackMsg(msg);
                });
        }
    }

    private createGroup(msg: CreateGroup) {
        let manager = this.managers.get(msg.content);
        if (manager == null) {
            getGroup(msg.content).then(group => { 
                if (group == null) {
                    createGroup(msg.content, msg.senderID).then(group => {
                        manager = new GroupManager(msg.content, msg.senderID, this, [], []);
                        console.log(`ServerManager: Group ${msg.content} has been created by ${msg.senderID}`);
                        this.managers.set(msg.content, manager);
                    }).catch(() => {
                        this.sendBackMsg(msg);
                    });
                } else {
                    console.log("ServerManager: Group already exists");
                }

            }).catch(() => {
                this.sendBackMsg(msg);
            });
        }
    }

    private storeMsg(message: Message) {
        retryWithOne(getContact, message.receiverID)
            .then(contact => {
                if (contact instanceof Group) {
                    console.log("ServerManager: Found Group in DB");
                    let manager = new GroupManager(contact.id, contact.manager, this, contact.requested, contact.accepted);
                    console.log(`ServerManager: Group ${contact.id} has been created by ${contact.manager}`);
                    console.log(`Group requested : ${contact.requested} accepted : ${contact.accepted}`);
                    this.managers.set(contact.id, manager);
                    manager.receiveMessage(message);
                } else if (contact instanceof User) {
                    console.log(`ServerManager insert Message into DB ${JSON.stringify(message)}`);
                    //insertMessageFromUser(message.message)
                    retryWithOne(storeUserMsg, message)
                        .catch(() => {
                            console.log("ServerManager: insert message into DB failed insert error");
                            this.sendBackMsg(message);
                        });
                }

            }).catch(error => {
                console.log("ServerManager: insert message into DB failed");
                this.sendBackMsg(message);
            })
    }

    sendBackMsg(msg: Message) {
        let m: Manager = this.managers.get(msg.senderID);
        if (m != null && m instanceof ClientManager) {
            console.log(`ServerManager send Back ErrorMsg ${JSON.stringify(msg)}`);
            let errorMsg = new ErrorMessage("server", msg.senderID, JSON.stringify(msg));
            m.handleMessage(errorMsg);
        }
    }

}