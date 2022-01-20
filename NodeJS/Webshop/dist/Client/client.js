#!/usr/bin/env node
"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
const clientmessages_1 = require("../Messages/clientmessages");
var WebSocketClient = require('websocket').client;
//var prompt = require('prompt-sync')();
var client = new WebSocketClient();
client.on('connectFailed', function (error) {
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
            console.log("Received: '" + message.utf8Data + "'");
        }
    });
    function sendLogin(email, password) {
        var message = new clientmessages_1.LoginMessage(email, password);
        connection.send(JSON.stringify(message));
    }
    function sendLogout() {
        var message = new clientmessages_1.LogoutMessage();
        connection.send(JSON.stringify(message));
    }
    function sendRegisterUser(email, lastName, firstName, address, password, card) {
        var msg = new clientmessages_1.RegisterMessage(email, firstName, lastName, address, password, card);
        connection.send(JSON.stringify(msg));
    }
    function sendSearchItem(searchStr) {
        var msg = new clientmessages_1.ItemSearchMessage(searchStr);
        connection.send(JSON.stringify(msg));
    }
    function sendGetTransactions() {
        var msg = new clientmessages_1.TransactionsMessage();
        connection.send(JSON.stringify(msg));
    }
    function sendModifyCart(itemID, amount, add) {
        var msg = new clientmessages_1.CartModificationMessage(itemID, amount, add);
        connection.send(JSON.stringify(msg));
    }
    function sendCheckOut() {
        var msg = new clientmessages_1.CheckOutMessage();
        connection.send(JSON.stringify(msg));
    }
    function sendTest() {
        //sendNumber();
        sendLogin("marcel.homolka@jku.at", "12345");
        sendSearchItem("Apple");
        sendModifyCart(1, 10, true);
        sendCheckOut();
        sendGetTransactions();
        sendLogout();
        //sendCheckOut();
        //setTimeout(sendTest, 1000);
    }
    sendTest();
});
client.connect('ws://localhost:8080/', 'echo-protocol');
//# sourceMappingURL=client.js.map