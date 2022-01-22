import { Cart, CartEntry, Item, User } from "../Entities/entities";
import { LoginMessage, LogoutMessage, CheckOutMessage, TransactionsMessage, ItemSearchMessage, CartModificationMessage, RegisterMessage, MessageTypes } from "../Messages/clientmessages";
import { ReturnMessage, ReturnMessageItem, ReturnMessageTransaction, ReturnMessageCart, ReturnMessageLogin, ReturnMessageLogout, ReturnMessageRegister, State, ReturnMessageCheckOut } from "../Messages/servermessages";
import { getUser, getTransactions, getAllItems, getItemsByName, getCartFromUser, updateCart, checkOut, modifyCartEntry, createUser, deleteEntry, getItemByID, deleteCart, checkOut_old } from "../Server/sql-connector";
class WebshopSessionManager {

    sessionID: number;
    user: User;
    cart: Cart;
    connection: any;
    constructor(sessionID: number, connection: any) {
        this.sessionID = sessionID;
        this.connection = connection;
    }

    private sendMsg(msg: any) {
        this.connection.send(JSON.stringify(msg));
        // console.log(`sendMsg to Client ${JSON.stringify(msg)}`);
    }

    public handleMessage(message: any) {
        var msg = JSON.parse(message);
        console.log(`message from ${message}`);
        switch (msg.type) {
            case MessageTypes.Logout:
                console.log("Logout msg received");
                setImmediate((msg) => this.logout(msg),msg);
                break;
            case MessageTypes.Login:
                console.log("Login msg received");
                setImmediate((msg) =>this.login(msg),msg);
                break;
            case MessageTypes.Register:
                console.log("Register msg received");
                setImmediate((msg) => this.registerUser(msg),msg);
                break;
            case MessageTypes.Transaction:
                console.log("Tranaction msg received");
                setImmediate((msg) =>this.transactions(msg),msg);
                break;
            case MessageTypes.ModifyCart:
                console.log("Modify msg received");
                setImmediate((msg) => this.modifyCart(msg),msg);
                break;
            case MessageTypes.Checkout:
                console.log("Checkout msg received");
                setImmediate((msg) => this.checkOutCart(msg),msg);
                break;
            case MessageTypes.ItemSearch:
                console.log("ItemSearch msg received");
                setImmediate((msg) => this.getItems(msg),msg);
                break;
            case MessageTypes.Cart:
                console.log("Cart Message");
                setImmediate(() => this.returnCart(true));
                break;
            default:
                console.log(`unknown message ${msg}`);
                break;
        }
    }


    public registerUser(msg: RegisterMessage) {
        var regUser: User = { password: msg.password, email: msg.email, firstName: msg.firstName, lastName: msg.lastName, address: msg.address, creditCard: msg.creditCard, userID: null };
        var retmsg: ReturnMessageRegister = new ReturnMessageRegister(State.Failure, "");
        createUser(regUser).then(user => {
            this.user = user;
            retmsg = new ReturnMessageRegister(State.Success, user.email);
            this.sendMsg(retmsg);
        }, (reject) => {
            this.sendMsg(retmsg);
        })
    }

    public login(msg: LoginMessage) {
        var retmsg: ReturnMessage = new ReturnMessageLogin(State.Failure, "");
        console.log(`Login recevied from ${msg.email}`);
        if (this.user == null) {
            console.log("Get User will be called")
            getUser(msg.email, msg.pwd).then(result => {
                if (result != null) {
                    this.user = result;
                    retmsg = new ReturnMessageLogin(State.Success, this.user.email);
                }
                if (this.cart != null) {
                    updateCart(this.user, this.cart);
                }
                this.sendMsg(retmsg);
            }, () => {
                this.sendMsg(retmsg);
            });
            console.log("After Get User was called");;
        }
        else {
            this.sendMsg(retmsg);
        }
    }

    public logout(msg: LogoutMessage) {
        var retmsg = new ReturnMessageLogout(State.Failure);
        if (this.user != null) {
            this.user = null;
            this.cart = { cartID: 0, entries: new Array<CartEntry>() };
            retmsg = new ReturnMessageLogout(State.Success);
        }
        this.sendMsg(retmsg);
    }

    public transactions(msg: TransactionsMessage) {
        var retmsg = new ReturnMessage(State.Failure);
        if (this.user != null) {
            getTransactions(this.user).then(transactions => {
                if (transactions != null) {
                    retmsg = new ReturnMessageTransaction(State.Success, transactions);
                }
                this.sendMsg(retmsg);
            })
        } else {
            this.sendMsg(retmsg);
        }

    }

    public getItems(msg: ItemSearchMessage) {
        var retmsg = new ReturnMessage(State.Failure);
        if (msg.searchString == "") {
            getAllItems().then(items => {
                if (items != null) {
                    retmsg = new ReturnMessageItem(State.Success, items);
                }
                this.sendMsg(retmsg);
            });
        } else {
            getItemsByName(msg.searchString).then(items => {
                if (items != null) {
                    retmsg = new ReturnMessageItem(State.Success, items);
                }
                this.sendMsg(retmsg);
            });

        }
    }

    public modifyCart(msg: CartModificationMessage) {
        if (this.user != null) {
            this.modifyCartDB(msg);
        } else {
            if (this.cart == null) {
                this.cart = { cartID: 0, entries: new Array<CartEntry>() };
            }
            this.modifyCartSession(msg);
        }
    }

    public modifyCartSession(msg: CartModificationMessage) {
        var entry: CartEntry;
        if (msg.itemID != null && msg.amount != null && msg.amount != 0) {
            getItemByID(msg.itemID).then((item) => {
                if (msg.add == true) {
                    entry = this.addItem(msg.itemID, msg.amount, this.cart, item);
                } else {
                    entry = this.removeItem(msg.itemID, msg.amount, this.cart, item);
                }
                var index = this.cart.entries.indexOf(entry);
                if (index >= 0) {
                    if (entry.amount == 0) {
                        this.cart.entries.splice(index, 1);
                    } else {
                        this.cart.entries[index] = entry;
                    }
                } else {
                    if (entry.amount > 0) {
                        this.cart.entries.push(entry);
                    }
                }
                var cartmsg = new ReturnMessageCart(State.Success, this.cart);
                this.sendMsg(cartmsg);
            });
        }
    }
    public modifyCartDB(msg: CartModificationMessage) {
        var entry: CartEntry;
        console.log("Modify Cart ", this.user);
        if (msg.itemID != null && msg.amount != null) {
            getItemByID(msg.itemID).then((item) => {
                getCartFromUser(this.user).then(cart => {
                    console.log(`received cart ${JSON.stringify(cart)}`);
                    if (msg.add == true) {
                        entry = this.addItem(msg.itemID, msg.amount, cart, item);
                        console.log(`entry ${JSON.stringify(entry)}`);
                        modifyCartEntry(entry, cart).then(success => {
                            this.returnCart(success);
                        });
                    } else {
                        entry = this.removeItem(msg.itemID, msg.amount, cart, item);
                        console.log(`newentry ${entry.item}`);
                        if (entry != null) {
                            if (entry.amount <= 0) {
                                console.log(`delete entry ${entry}`);
                                deleteEntry(cart, entry).then(success => {
                                    this.returnCart(success);
                                });
                            }
                            else {
                                modifyCartEntry(entry, cart).then(success => {
                                    this.returnCart(success);
                                })
                            }
                        } else {
                            this.sendMsg(new ReturnMessageCart(State.Failure, null));
                        }
                    }
                })
            }, () => {
                this.sendMsg(new ReturnMessageCart(State.Failure, null));
            });

        }
    }
    private returnCart(success: boolean) {
        var msg = new ReturnMessage(State.Failure);
        if (success) {
            getCartFromUser(this.user).then(cart => {
                msg = new ReturnMessageCart(State.Success, cart);
                this.sendMsg(msg);
            });
        }
        else {
            this.sendMsg(msg);
        }
    }

    addItem(itemID: number, amount: number, cart: Cart, item: Item): CartEntry {
        var retEntry: CartEntry = null;
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
    
    removeItem(itemID: number, amount: number, cart: Cart, item: Item): CartEntry {
        var retEntry: CartEntry = null;
        cart.entries.forEach((entry) => {
            if (entry.item.itemID == itemID) {
                retEntry = entry;
                retEntry.amount -= amount;
            }
        });
        return retEntry;
    }

    public checkOutCart(msg: CheckOutMessage) {
        var retmsg = new ReturnMessageCheckOut(State.Failure);
        if (this.user != null) {
            // send checkout to mysql server
            checkOut(this.user,this.cart).then(ret => {
                if (ret) {
                    retmsg = new ReturnMessageCheckOut(State.Success);
                }
                console.log("send CheckoutMsg");
                this.sendMsg(retmsg);
            });
        } else {
            // return error msg 
            this.sendMsg(retmsg);
        }
    }
}
export { WebshopSessionManager };
