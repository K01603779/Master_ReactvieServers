#!/usr/bin/env node
import { LoginMessage, LogoutMessage, CheckOutMessage, TransactionsMessage, ItemSearchMessage, CartModificationMessage, RegisterMessage } from "../Messages/clientmessages";
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

    function sendLogin(email: string, password: string) {
        var message = new LoginMessage(email, password);
        connection.send(JSON.stringify(message));
    }

    function sendLogout() {
        var message = new LogoutMessage();
        connection.send(JSON.stringify(message));
    }
    function sendRegisterUser(email: string, lastName: string, firstName: string, address: string, password: string, card: string) {
        var msg = new RegisterMessage(email, firstName, lastName, address, password, card);
        connection.send(JSON.stringify(msg));

    }
    function sendSearchItem(searchStr: string) {
        var msg = new ItemSearchMessage(searchStr);
        connection.send(JSON.stringify(msg));

    }
    function sendGetTransactions() {
        var msg = new TransactionsMessage();
        connection.send(JSON.stringify(msg));

    }
    function sendModifyCart(itemID: number, amount: number, add: boolean) {
        var msg = new CartModificationMessage(itemID, amount, add);
        connection.send(JSON.stringify(msg));

    }
    function sendCheckOut() {
        var msg = new CheckOutMessage();
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