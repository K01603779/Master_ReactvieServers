"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
const clientmessages_1 = require("../Messages/clientmessages");
const servermessages_1 = require("../Messages/servermessages");
const sql_connector_1 = require("./sql-connector");
class WebshopSessionManager {
    constructor(sessionID, connection) {
        this.sessionID = sessionID;
        this.connection = connection;
    }
    sendMsg(msg) {
        this.connection.send(JSON.stringify(msg));
        // console.log(`sendMsg to Client ${JSON.stringify(msg)}`);
    }
    handleMessage(message) {
        var msg = JSON.parse(message);
        console.log(`message from ${message}`);
        switch (msg.type) {
            case clientmessages_1.MessageTypes.Logout:
                console.log("Logout msg received");
                setImmediate((msg) => this.logout(msg), msg);
                break;
            case clientmessages_1.MessageTypes.Login:
                console.log("Login msg received");
                setImmediate((msg) => this.login(msg), msg);
                break;
            case clientmessages_1.MessageTypes.Register:
                console.log("Register msg received");
                setImmediate((msg) => this.registerUser(msg), msg);
                break;
            case clientmessages_1.MessageTypes.Transaction:
                console.log("Tranaction msg received");
                setImmediate((msg) => this.transactions(msg), msg);
                break;
            case clientmessages_1.MessageTypes.ModifyCart:
                console.log("Modify msg received");
                setImmediate((msg) => this.modifyCart(msg), msg);
                break;
            case clientmessages_1.MessageTypes.Checkout:
                console.log("Checkout msg received");
                setImmediate((msg) => this.checkOutCart(msg), msg);
                break;
            case clientmessages_1.MessageTypes.ItemSearch:
                console.log("ItemSearch msg received");
                setImmediate((msg) => this.getItems(msg), msg);
                break;
            case clientmessages_1.MessageTypes.Cart:
                console.log("Cart Message");
                setImmediate(() => this.returnCart(true));
                break;
            default:
                console.log(`unknown message ${msg}`);
                break;
        }
    }
    registerUser(msg) {
        var regUser = { password: msg.password, email: msg.email, firstName: msg.firstName, lastName: msg.lastName, address: msg.address, creditCard: msg.creditCard, userID: null };
        var retmsg = new servermessages_1.ReturnMessageRegister(servermessages_1.State.Failure, "");
        /*createUser(regUser).then(user => {
            this.user = user;
            retmsg = new ReturnMessageRegister(State.Success, user.email);
            this.sendMsg(retmsg);
        }, (reject) => {
            this.sendMsg(retmsg);
        })*/
        sql_connector_1.retryWithOne(sql_connector_1.createUser, regUser).then(user => {
            this.user = user;
            retmsg = new servermessages_1.ReturnMessageRegister(servermessages_1.State.Success, user.email);
            this.sendMsg(retmsg);
        }).catch(() => {
            this.sendMsg(retmsg);
        });
    }
    login(msg) {
        var retmsg = new servermessages_1.ReturnMessageLogin(servermessages_1.State.Failure, "");
        console.log(`Login recevied from ${msg.email}`);
        if (this.user == null) {
            console.log("Get User will be called");
            sql_connector_1.retryWithTwo(sql_connector_1.getUser, msg.email, msg.pwd).then(result => {
                //getUser(msg.email, msg.pwd).then(result => {
                if (result != null) {
                    this.user = result;
                    retmsg = new servermessages_1.ReturnMessageLogin(servermessages_1.State.Success, this.user.email);
                    if (this.cart != null) {
                        sql_connector_1.retryWithTwo(sql_connector_1.updateCart, this.user, this.cart).catch(() => {
                            //TODO 
                            console.log("Error while updating cart after login " + this.user.email);
                        });
                    }
                }
                this.sendMsg(retmsg);
            }).catch(() => {
                this.sendMsg(retmsg);
            });
            console.log("After Get User was called");
            ;
        }
        else {
            this.sendMsg(retmsg);
        }
    }
    logout(msg) {
        var retmsg = new servermessages_1.ReturnMessageLogout(servermessages_1.State.Failure);
        if (this.user != null) {
            this.user = null;
            this.cart = { cartID: 0, entries: new Array() };
            retmsg = new servermessages_1.ReturnMessageLogout(servermessages_1.State.Success);
        }
        this.sendMsg(retmsg);
    }
    transactions(msg) {
        var retmsg = new servermessages_1.ReturnMessage(servermessages_1.State.Failure);
        if (this.user != null) {
            sql_connector_1.retryWithOne(sql_connector_1.getTransactions, this.user).then(transactions => {
                //getTransactions(this.user).then(transactions => {
                if (transactions != null) {
                    retmsg = new servermessages_1.ReturnMessageTransaction(servermessages_1.State.Success, transactions);
                }
                this.sendMsg(retmsg);
            }).catch(() => {
                this.sendMsg(retmsg);
            });
        }
        else {
            this.sendMsg(retmsg);
        }
    }
    getItems(msg) {
        var retmsg = new servermessages_1.ReturnMessageItem(servermessages_1.State.Failure, null);
        if (msg.searchString == "") {
            sql_connector_1.retryWith(sql_connector_1.getAllItems).then(items => {
                //getAllItems().then(items => {
                console.log("Found Items " + JSON.stringify(items));
                if (items != null) {
                    retmsg = new servermessages_1.ReturnMessageItem(servermessages_1.State.Success, items);
                }
                this.sendMsg(retmsg);
            }).catch(() => {
                this.sendMsg(retmsg);
            });
        }
        else {
            sql_connector_1.retryWithOne(sql_connector_1.getAllItemBySearch, msg.searchString).then(items => {
                console.log("Found Items " + msg.searchString);
                //getAllItemBySearch(msg.searchString).then(items => {
                if (items != null) {
                    retmsg = new servermessages_1.ReturnMessageItem(servermessages_1.State.Success, items);
                }
                this.sendMsg(retmsg);
            }).catch(() => {
                this.sendMsg(retmsg);
            });
        }
    }
    modifyCart(msg) {
        if (this.user != null) {
            this.modifyCartDB(msg);
        }
        else {
            if (this.cart == null) {
                this.cart = { cartID: 0, entries: new Array() };
            }
            this.modifyCartSession(msg);
        }
    }
    modifyCartSession(msg) {
        var entry;
        if (msg.itemID != null && msg.amount != null && msg.amount != 0) {
            sql_connector_1.retryWithOne(sql_connector_1.getItemByID, msg.itemID).then(item => {
                //getItemByID(msg.itemID).then((item) => {
                if (msg.add == true) {
                    entry = this.addItem(msg.itemID, msg.amount, this.cart, item);
                }
                else {
                    entry = this.removeItem(msg.itemID, msg.amount, this.cart, item);
                }
                var index = this.cart.entries.indexOf(entry);
                if (index >= 0) {
                    if (entry.amount == 0) {
                        this.cart.entries.splice(index, 1);
                    }
                    else {
                        this.cart.entries[index] = entry;
                    }
                }
                else {
                    if (entry.amount > 0) {
                        this.cart.entries.push(entry);
                    }
                }
                var cartmsg = new servermessages_1.ReturnMessageCart(servermessages_1.State.Success, this.cart);
                this.sendMsg(cartmsg);
            }).catch(() => {
                var cartmsg = new servermessages_1.ReturnMessageCart(servermessages_1.State.Failure, null);
                this.sendMsg(cartmsg);
            });
        }
    }
    modifyCartDB(msg) {
        var entry;
        console.log("Modify Cart ", this.user);
        if (msg.itemID != null && msg.amount != null) {
            sql_connector_1.retryWithOne(sql_connector_1.getItemByID, msg.itemID).then(item => {
                //getItemByID(msg.itemID).then((item) => {
                sql_connector_1.retryWithOne(sql_connector_1.getCartFromUser, this.user).then(cart => {
                    //getCartFromUser(this.user).then(cart => {
                    console.log(`received cart ${JSON.stringify(cart)}`);
                    if (msg.add == true) {
                        entry = this.addItem(msg.itemID, msg.amount, cart, item);
                        console.log(`entry ${JSON.stringify(entry)}`);
                        sql_connector_1.retryWithTwo(sql_connector_1.modifyCartEntry, entry, cart).then(success => {
                            //modifyCartEntry(entry, cart).then(success => {
                            this.returnCart(success);
                        }).catch(() => {
                            this.sendMsg(new servermessages_1.ReturnMessageCart(servermessages_1.State.Failure, null));
                        });
                    }
                    else {
                        entry = this.removeItem(msg.itemID, msg.amount, cart, item);
                        console.log(`newentry ${entry.item}`);
                        if (entry != null) {
                            if (entry.amount <= 0) {
                                console.log(`delete entry ${entry}`);
                                sql_connector_1.retryWithTwo(sql_connector_1.deleteEntry, cart, entry).then(success => {
                                    //deleteEntry(cart, entry).then(success => {
                                    this.returnCart(success);
                                }).catch(() => {
                                    this.sendMsg(new servermessages_1.ReturnMessageCart(servermessages_1.State.Failure, null));
                                });
                            }
                            else {
                                sql_connector_1.retryWithTwo(sql_connector_1.modifyCartEntry, entry, cart).then(success => {
                                    //modifyCartEntry(entry, cart).then(success => {
                                    this.returnCart(success);
                                }).catch(() => {
                                    this.sendMsg(new servermessages_1.ReturnMessageCart(servermessages_1.State.Failure, null));
                                });
                            }
                        }
                        else {
                            this.sendMsg(new servermessages_1.ReturnMessageCart(servermessages_1.State.Failure, null));
                        }
                    }
                }).catch(() => {
                    this.sendMsg(new servermessages_1.ReturnMessageCart(servermessages_1.State.Failure, null));
                });
            }).catch(() => {
                this.sendMsg(new servermessages_1.ReturnMessageCart(servermessages_1.State.Failure, null));
            });
        }
    }
    returnCart(success) {
        var msg = new servermessages_1.ReturnMessage(servermessages_1.State.Failure);
        if (success) {
            sql_connector_1.retryWithOne(sql_connector_1.getCartFromUser, this.user).then(cart => {
                //getCartFromUser(this.user).then(cart => {
                msg = new servermessages_1.ReturnMessageCart(servermessages_1.State.Success, cart);
                this.sendMsg(msg);
            }).catch(() => {
                this.sendMsg(msg);
            });
        }
        else {
            this.sendMsg(msg);
        }
    }
    addItem(itemID, amount, cart, item) {
        var retEntry = null;
        cart.entries.forEach((entry) => {
            if (entry.item.itemID == itemID) {
                retEntry = entry;
                retEntry.amount = retEntry.amount + amount;
            }
        });
        if (retEntry == null) {
            retEntry = { item: item, amount: amount };
        }
        return retEntry;
    }
    removeItem(itemID, amount, cart, item) {
        var retEntry = null;
        cart.entries.forEach((entry) => {
            if (entry.item.itemID == itemID) {
                retEntry = entry;
                retEntry.amount -= amount;
            }
        });
        return retEntry;
    }
    //TODO Checkout check Credentials
    checkOutCart(msg) {
        var retmsg = new servermessages_1.ReturnMessageCheckOut(servermessages_1.State.Failure);
        if (this.user != null) {
            // send checkout to mysql server
            sql_connector_1.retryWithOne(sql_connector_1.checkOut, this.user).then(ret => {
                //checkOut(this.user).then(ret => {
                if (ret) {
                    retmsg = new servermessages_1.ReturnMessageCheckOut(servermessages_1.State.Success);
                }
                console.log("send CheckoutMsg");
                this.sendMsg(retmsg);
            }).catch(() => {
                this.sendMsg(retmsg);
            });
        }
        else {
            // return error msg 
            this.sendMsg(retmsg);
        }
    }
}
exports.WebshopSessionManager = WebshopSessionManager;
//# sourceMappingURL=WebshopSessionManager.js.map