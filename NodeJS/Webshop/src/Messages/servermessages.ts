import { Cart, Item, Transaction } from "../Entities/entities";
import { MessageTypes } from "../Messages/types";
enum State {
    Success = 0,
    Failure
}
class ReturnMessage {
    state: State;
    type: MessageTypes;
    constructor(state: State) {
        this.state = state;
    }
}
class ReturnMessageLogin extends ReturnMessage {
    type = MessageTypes.Login;
    username: String;
    constructor(state: State, username: String) {
        super(state);
        this.username = username;
    }
}
class ReturnMessageLogout extends ReturnMessage {
    type = MessageTypes.Logout;
    constructor(state: State) {
        super(state);
    }
}
class ReturnMessageRegister extends ReturnMessage {
    type = MessageTypes.Register;
    username: String;
    constructor(state: State, username: String) {
        super(state);
        this.username = username;
    }
}

class ReturnMessageItem extends ReturnMessage {
    items: Item[];
    type = MessageTypes.ItemSearch;
    constructor(state: State, items: Item[]) {
        super(state);
        this.items = items;
    }
}
class ReturnMessageTransaction extends ReturnMessage {
    list: Transaction[];
    type = MessageTypes.Transaction;
    constructor(state: State, list: Transaction[]) {
        super(state);
        this.list = list;
    }
}
class ReturnMessageCart extends ReturnMessage {
    cart: Cart;
    type = MessageTypes.ModifyCart;
    constructor(state: State, cart: Cart) {
        super(state);
        this.cart = cart;
    }
}
class ReturnMessageCheckOut extends ReturnMessage {
    type = MessageTypes.Checkout;
}
export { ReturnMessage, ReturnMessageItem, ReturnMessageTransaction, ReturnMessageCart, ReturnMessageLogin, ReturnMessageLogout, ReturnMessageRegister, ReturnMessageCheckOut, State };
