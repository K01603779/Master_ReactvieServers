import { MessageTypes } from "../Messages/types";
class Message {
    type: MessageTypes;
}
class LoginMessage extends Message {
    email: string;
    pwd: string;
    type = MessageTypes.Login;
    constructor(email: string, pwd: string) {
        super();
        this.email = email;
        this.pwd = pwd;
    }
}

class LogoutMessage extends Message {
    type = MessageTypes.Logout;
}

class CheckOutMessage extends Message {
    type = MessageTypes.Checkout;
}

class TransactionsMessage extends Message {
    type = MessageTypes.Transaction;
}

class ItemSearchMessage extends Message {
    type = MessageTypes.ItemSearch;
    searchString: string;
    constructor(search: string) {
        super();
        this.searchString = search;
    }
}

class CartModificationMessage extends Message {
    type = MessageTypes.ModifyCart;
    itemID: number;
    amount: number;
    add: boolean;
    constructor(itemID: number, amount: number, add: boolean) {
        super();
        this.add = add;
        this.amount = amount;
        this.itemID = itemID;
    }
}

class RegisterMessage extends Message {
    type = MessageTypes.Register;
    email: string;
    firstName: string;
    lastName: string;
    address: string;
    password: string;
    creditCard: string;
    constructor(email: string, firstName: string, lastName: string, address: string, password: string, card: string) {
        super();
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.address = address;
        this.password = password;
        this.creditCard = card;
    }
}

class CartMessage extends Message {
    type = MessageTypes.Cart;
}




export { LoginMessage, LogoutMessage, CheckOutMessage, TransactionsMessage, ItemSearchMessage, CartModificationMessage, RegisterMessage, MessageTypes, CartMessage };