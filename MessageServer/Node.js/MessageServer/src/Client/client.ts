#!/usr/bin/env node
import { PrivateMessage, AcceptRequest, DeclineRequest, InviteToGroup, LeaveGroup, Message, MsgType, CreateGroup, RemoveGroup } from "../Messages/messages";
var WebSocketClient = require('websocket').client;
//var prompt = require('prompt-sync')();
var client = new WebSocketClient();
var username = process.argv[2];
var password = process.argv[3];
var create = false;
if (process.argv[4] !== undefined) {
    create = true;
}
console.log(`Logging in as ${username} with ${password} and ${create}`);

client.on('connectFailed', function (error) {
    console.log('Connect Error: ' + error.toString());
});
client.on('error',function(error) {
    console.log('Connect Error: ' + error.toString());
});

client.on('connect', function (connection) {
    console.log('WebSocket Client Connected');
    connection.on('error', function (error) {
        console.log("Connection Error: " + error.toString());
    });
    connection.on('close', function () {
        console.log('echo-protocol Connection Closed');
    });
    connection.on('message', function (message) {
        if (message.type === 'utf8') {
            var msg: Message = JSON.parse(message.utf8Data);
            switch (msg.type) {
                case MsgType.Private:
                    console.log(`received Msg from ${msg.senderID} => ${(msg as PrivateMessage).content}`);
                    console.log(`ReceivedMsg ${message.utf8Data}`);
                    break;
                case MsgType.InviteToGroup:
                    console.log(`Receive request to join group ${(msg as InviteToGroup).senderID}`);
                    break;
                default:
                    console.log(`ReceivedMsg ${message.utf8Data}`);
                    break;
            }
        }
    });

    function sendMessage(to: string, msg: string) {
        var message = new PrivateMessage(username, to, msg);
        connection.send(JSON.stringify(message));
    }
    function acceptRequest(to: string) {
        var message = new AcceptRequest(username, to);
        connection.send(JSON.stringify(message));
    }
    function declineRequest(to: string) {
        var message = new DeclineRequest(username, to);
        connection.send(JSON.stringify(message));
    }
    function createGroup(groupname: string) {
        var message = new CreateGroup(username, groupname);
        connection.send(JSON.stringify(message));
    }
    function removeGroup(groupname: string) {
        var message = new RemoveGroup(username, groupname);
        connection.send(JSON.stringify(message));
    }
    function addToGroup(user: string, groupname: string) {
        var message = new InviteToGroup(username, groupname, user);
        connection.send(JSON.stringify(message));
    }
    function leaveGroup(groupname: string) {
        var message = new LeaveGroup(username, groupname);
        connection.send(JSON.stringify(message));
    }

    //sendMessage(username, "This is the first send to myself");
    var stdin = process.openStdin();

    stdin.addListener("data", function (d) {
        const re = /\s*(?:;|$)/;
        // note:  d is an object, and when converted to a string it will
        // end with a linefeed.  so we (rather crudely) account for that  
        // with toString() and then trim() 
        const cmd = d.toString().trim();
        console.log("you entered: [" +
            cmd + "]");
        const split = cmd.split(re);
        handleSplit(split);

    });
    function handleSplit(split: string[]) {
        if (split.length > 0) {
            if (split[0] === "send") {
                sendMessage(split[2], split[1]);
            }
            else if (split[0] === "create") {
                createGroup(split[1]);
            }
            else if (split[0] === "remove") {
                removeGroup(split[1]);
            }
            else if (split[0] === "accept") {
                acceptRequest(split[1]);
            }
            else if (split[0] === "decline") {
                declineRequest(split[1]);
            }
            else if (split[0] === "add") {
                addToGroup(split[1], split[2]);
            }
            else if (split[0] === "leave") {
                leaveGroup(split[1]);
            }
        }
    }
});
//const url = 'ws://localhost:8080/'; // Node.js
//const url = 'ws://localhost:8080/JSP-Message/message';
//const url = 'ws://localhost:8080/greeter'; //akka
const url = 'ws://localhost:8080/chat'; //RxKotlin


//const url = 'ws://192.168.0.18:8080/greeter'; //akka
//const url = 'ws://192.168.0.18:8080/JSP-Message/message';


//const url = 'ws://localhost:8080/JSP-Message/messageasync'
//client.connect(url, 'echo-protocol', null, { username: username, password: password, create: create }); // NodeJS
//client.connect(url,null,null,{ username: username, password: password, create: create }); //JAVA EE
client.connect(url,null,null,{ username: username, password: password, create: create }); // akka ,reactiveX

