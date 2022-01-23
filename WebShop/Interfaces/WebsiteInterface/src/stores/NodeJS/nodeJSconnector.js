import { LoginMessage, LogoutMessage, CheckOutMessage, TransactionsMessage, ItemSearchMessage, CartModificationMessage, RegisterMessage, CartMessage } from "../NodeJS/messages/clientmessages.js";
import { MessageTypes } from "../NodeJS/messages/types.js";
import { loggedIn, user, register, transactions, searchresults, cart } from "../store";
var connection = new WebSocket('ws://localhost:8080/', 'echo-protocol');

export function requestLogin(email, password) {
    var message = new LoginMessage(email, password);
    connection.send(JSON.stringify(message));
}

export function requestLogout() {
    var message = new LogoutMessage();
    connection.send(JSON.stringify(message));
}
export function requestRegisterUser(email, lastName, firstName, address, password, card) {
    var msg = new RegisterMessage(email, firstName, lastName, address, password, card);
    connection.send(JSON.stringify(msg));

}
export function requestSearch(searchStr) {
    var msg = new ItemSearchMessage(searchStr);
    connection.send(JSON.stringify(msg));

}
export function sendGetTransactions() {
    var msg = new TransactionsMessage();
    connection.send(JSON.stringify(msg));

}
function sendGetCart() {
    var msg = new CartMessage();
    console.log("send CartMsg");
    connection.send(JSON.stringify(msg));
}
function sendModifyCart(itemID, amount, add) {
    var msg = new CartModificationMessage(itemID, amount, add);
    connection.send(JSON.stringify(msg));
}

export function requestRemoveItem(itemID, amount) {
    sendModifyCart(itemID, amount, false);
}

export function requestAddItem(itemID, amount) {
    sendModifyCart(itemID, amount, true);
}

export function requestCheckOut() {
    var msg = new CheckOutMessage();
    connection.send(JSON.stringify(msg));
}

connection.onmessage = function (event) {
    if (event.data != "") {
        console.log(event.data);
        var msg = JSON.parse(event.data);
        switch (msg.type) {
            case MessageTypes.Login:
                console.log("Login")
                if (msg.state == 0) {
                    loggedIn.set(true);
                    register.set(false);
                    user.set(msg.username);
                    sendGetTransactions();
                    sendGetCart();
                } else {
                    alert("Login not successfull");
                }
                break;
            case MessageTypes.Logout:
                console.log("Logout")
                if (msg.state == 0) {
                    loggedIn.set(false);
                    user.set("no user");
                    transactions.set([]);
                    cart.set([]);
                }
                break;
            case MessageTypes.Register:
                console.log("Register");
                if (msg.state == 0) {
                    loggedIn.set(true);
                    register.set(false);
                    user.set(msg.username);
                } else {
                    alert("Registration was not successfull");
                }
                break;
            case MessageTypes.ItemSearch:
                console.log("Item Search");
                if (msg.state == 0) {
                    searchresults.set(msg.items);
                }else {
                    alert("Search not Successful");
                }
                break;
            case MessageTypes.ModifyCart:
                console.log("cart modification")
                if (msg.state == 0) {
                    console.log(msg.cart.entries);
                    cart.set(msg.cart.entries);
                }else {
                    alert("Add/Delete not Sucessfull");
                }
                break;
            case MessageTypes.Transaction:
                console.log("Transaction");
                if (msg.state == 0) {
                    transactions.set(msg.list);
                }
                break;
            case 6: //TODO checkout
                console.log("Checkout");
                if (msg.state == 0) {
                    sendGetTransactions();
                    cart.set([]);
                }else {
                    alert("Checkout not successful")
                }
                break;
            default:
                console.log("message unknown");
                break;
        }
    }
}