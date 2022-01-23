#!/usr/bin/env node
import { CartModificationMessage, CheckOutMessage, ItemSearchMessage, LoginMessage, LogoutMessage, TransactionsMessage } from "../Messages/clientmessages";
import { SessionManager } from "./SessionManager";
var WebSocketServer = require('websocket').server;
var http = require('http');

/*const session = new WebshopSessionManager(connectionCnt++, null);
session.login(new LoginMessage("marcel.homolka@jku.at", "12345"));
setTimeout(() => {
    console.log("Search Item");
    session.getItems(new ItemSearchMessage("Apple"))
}, 3000);
setTimeout(() => {
    console.log("CartModification");
    session.modifyCart(new CartModificationMessage(1, 10, true))
}, 4000);
//setTimeout(() => session.checkOut(new CheckOutMessage()),5000);
//setTimeout(() =>session.getTransactions(new TransactionsMessage()) ,6000);
setTimeout(() => {
    console.log("LogOut");
    session.logout(new LogoutMessage())
}, 6000);*/


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

function originIsAllowed(origin) {
    // put logic here to detect whether the specified origin is allowed.
    return true;
}

wsServer.on('request', function (request) {
    if (!originIsAllowed(request.origin)) {
        // Make sure we only accept requests from an allowed origin
        request.reject();
        console.log((new Date()) + ' Connection from origin ' + request.origin + ' rejected.');
        return;
    }
    var connection = request.accept('echo-protocol', request.origin);
    const session = new SessionManager(connection);
    console.log((new Date()) + ' Connection accepted.');
    connection.on('message', function (message) {
        if (message.type === 'utf8') {
            session.handleMessage(message.utf8Data);

        }
        else if (message.type === 'binary') {
            console.log('Received Binary Message of ' + message.binaryData.length + ' bytes');
            connection.sendBytes(message.binaryData);
            connection.sendUTF(message.binaryData);
        }
    });
    connection.on('close', function (reasonCode, description) {
        console.log((new Date()) + ' Peer ' + connection.remoteAddress + ' disconnected.');
    });
});