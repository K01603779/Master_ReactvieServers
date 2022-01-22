#!/usr/bin/env node
import { ServerManager } from "../Server/Managers/serverManager";
import { ClientManager } from "../Server/Managers/clientManager";
import { getUser, createUser, retryWithTwo } from "../Server/sql-connector";

const retryCnt = 3;
var WebSocketServer = require('websocket').server;
var http = require('http');

var server = http.createServer(function (request, response) {
    console.log((new Date()) + ' Received request for ' + request.url);
    response.writeHead(404);
    response.end();
});
server.listen(8080, function () {
    console.log((new Date()) + ' Server is listening on port 8080');
});

var wsServer = new WebSocketServer({
    httpServer: server,
    // You should not use autoAcceptConnections for production
    // applications, as it defeats all standard cross-origin protection
    // facilities built into the protocol and the browser.  You should
    // *always* verify the connection's origin and decide whether or not
    // to accept it.
    autoAcceptConnections: false
});
var manager = new ServerManager();

function originIsAllowed(origin) {
    // put logic here to detect whether the specified origin is allowed.
    return true;
}
function setUpManager(request: any, username: string): ClientManager {
    //var connection = request.accept('echo-protocol', request.origin);
    var connection = request.accept(null, request.origin);
    const clientManager = new ClientManager(connection, username);
    clientManager.setUp(manager);
    return clientManager;
}
function getContentFromParameter(parameter:string): Array<string> {
    console.log("Ressource");
    // /username=test&password=1234&create=false
    var parameters = parameter.split("&");
    var username = parameters[0].substring(10);
    var password = parameters[1].substring(9);
    var create= parameters[2].substring(7);
    console.log(`userrname ${username} password ${password} create ${create}`); 
    return [username,password,create];
    
}

wsServer.on('request', function (request) {
    if (!originIsAllowed(request.origin)) {
        // Make sure we only accept requests from an allowed origin
        request.reject();
        console.log((new Date()) + ' Connection from origin ' + request.origin + ' rejected.');
        return;
    }
    var username = request.httpRequest.headers.username;
    var password = request.httpRequest.headers.password;
    var create = request.httpRequest.headers.create;
    if(username == undefined){
        var arr = getContentFromParameter(request.resource);
        username=arr[0];
        password=arr[1];
        create=arr[2];
    }
    if (manager.containsUser(username)) {
        request.reject();
        console.log(`User ${username} already logged in`);
    } else {
        if (create === "true") {
            console.log("Creation == true");
            //createUser(username, password)
            retryWithTwo(createUser, username, password)
                .then(user => {
                    console.log(`UserID ${user.id}`);
                    setUpManager(request, user.id);
                }).catch(error => {
                    console.log("Create User rejected");
                    request.reject();
                });
        }
        else {
            console.log("Creation == false");
            //getUser(username, password)
            retryWithTwo(getUser, username, password)
                .then(user => {
                    console.log(`UserID ${user.id}`);
                    const client = setUpManager(request, user.id);
                    client.getMessagesFromDB(manager);
                    /*getMessageFromUser(user.id).then(messages => {
                        messages.forEach(message => {
                            manager.addMessagetoQueue({ user: user.id, message });
                            // TODO check if not blocking
                        })
                    });*/
                }).catch(error => {
                    console.log("Get User rejected");
                    request.reject();
                });
        }
    }
    
});

