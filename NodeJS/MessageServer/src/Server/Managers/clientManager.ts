import { Message } from "../../Messages/messages";
import { Manager } from "./manager";
import { ServerManager } from "./serverManager";
import { getMessageOfUser } from "../../Server/sql-connector";

export class ClientManager implements Manager {

    connection: any;
    username: string;
    user: any; // TODO

    constructor(connection: any, username: string) {
        this.connection = connection;
        this.username = username;
    }

    public receiveMessage(msg: Message) {
        console.log(`ClientManager ${this.username} received message ${JSON.stringify(msg)}`);
        //setImmediate(() => this.handleMessage(msg),msg);
        this.handleMessage(msg);

    }

    sendMessageToClient(message: Message): void {
        this.connection.send(JSON.stringify(message));
    }

    handleMessage(message: Message) {
        if (message != undefined) {
            console.log(`ClientManager ${this.username} handleMessage ${JSON.stringify(message)}`);
            this.connection.send(JSON.stringify(message));
        }
    }
    storeMessagesinDB(message: Message) {
        // TODO 
    }

    setUp(manager: ServerManager) {
        if (manager.addManagers(this.username, this)) {
            console.log(`add of ${this.username} successfull`);
        }
        var name = this.username;
        console.log((new Date()) + ' Connection accepted.');
        this.connection.on('message', function (message) {
            if (message.type === 'utf8') {
                console.log('Received Message: ' + message.utf8Data);
                var msg = JSON.parse(message.utf8Data);
                if (msg.receiverID != "") {
                    console.log(msg.receiverID);
                    manager.handleMsg(msg);
                }
            }
        });
        this.connection.on('close', function (this, reasonCode, description) {
            console.log((new Date()) + ' Peer ' + name + ' disconnected.');
            manager.removeManager(name);
        });
    }
    close(reasonCode, description) {

    }

    getMessagesFromDB(manager: ServerManager) {
        getMessageOfUser(this.username).then(messages => {
            messages.forEach(message => {
                manager.addMessagetoQueue(message);
            });
        });
    }



}