import { Cart, Item, Transaction } from "../Entities/entities";
import { MessageTypes } from "../Messages/types";
enum State {
    Success = 0,
    Failure
}
class ResponseMessage {
    state: State;
    type: MessageTypes;
    constructor(state: State) {
        this.state = state;
    }
}
class LoginResponse extends ResponseMessage {
    type = MessageTypes.Login;
    username: String;
    constructor(state: State, username: String) {
        super(state);
        this.username = username;
    }
}
class LogoutResponse extends ResponseMessage {
    type = MessageTypes.Logout;
    constructor(state: State) {
        super(state);
    }
}
class RegistrationResponse extends ResponseMessage {
    type = MessageTypes.Register;
    username: String;
    constructor(state: State, username: String) {
        super(state);
        this.username = username;
    }
}

class SearchResponse extends ResponseMessage {
    items: Item[];
    type = MessageTypes.ItemSearch;
    constructor(state: State, items: Item[]) {
        super(state);
        this.items = items;
    }
}
class TransactionResponse extends ResponseMessage {
    list: Transaction[];
    type = MessageTypes.Transaction;
    constructor(state: State, list: Transaction[]) {
        super(state);
        this.list = list;
    }
}
class CartResponse extends ResponseMessage {
    cart: Cart;
    type = MessageTypes.ModifyCart;
    constructor(state: State, cart: Cart) {
        super(state);
        this.cart = cart;
    }
}
class CheckoutResponse extends ResponseMessage {
    type = MessageTypes.Checkout;
}
export { ResponseMessage as ReturnMessage, SearchResponse as ReturnMessageItem, TransactionResponse as ReturnMessageTransaction, CartResponse as ReturnMessageCart, LoginResponse as ReturnMessageLogin, LogoutResponse as ReturnMessageLogout, RegistrationResponse as ReturnMessageRegister, CheckoutResponse as ReturnMessageCheckOut, State };
