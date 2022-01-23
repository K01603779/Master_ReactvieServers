import { MessageTypes } from "../Messages/types";
class RequestMessage {
    type: MessageTypes;
}
class LoginRequest extends RequestMessage {
    email: string;
    pwd: string;
    type = MessageTypes.Login;
    constructor(email: string, pwd: string) {
        super();
        this.email = email;
        this.pwd = pwd;
    }
}

class LogoutRequest extends RequestMessage {
    type = MessageTypes.Logout;
}

class CheckoutRequest extends RequestMessage {
    type = MessageTypes.Checkout;
}

class TransactionRequest extends RequestMessage {
    type = MessageTypes.Transaction;
}

class SearchRequest extends RequestMessage {
    type = MessageTypes.ItemSearch;
    searchString: string;
    constructor(search: string) {
        super();
        this.searchString = search;
    }
}

class CartModificationRequest extends RequestMessage {
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

class RegistrationRequest extends RequestMessage {
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

class CartRequest extends RequestMessage {
    type = MessageTypes.Cart;
}




export { LoginRequest as LoginMessage, LogoutRequest as LogoutMessage, CheckoutRequest as CheckOutMessage, TransactionRequest as TransactionsMessage, SearchRequest as ItemSearchMessage, CartModificationRequest as CartModificationMessage, RegistrationRequest as RegisterMessage, MessageTypes, CartRequest as CartMessage };