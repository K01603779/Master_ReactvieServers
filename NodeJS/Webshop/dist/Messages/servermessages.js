"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.State = exports.ReturnMessageCheckOut = exports.ReturnMessageRegister = exports.ReturnMessageLogout = exports.ReturnMessageLogin = exports.ReturnMessageCart = exports.ReturnMessageTransaction = exports.ReturnMessageItem = exports.ReturnMessage = void 0;
const types_1 = require("../Messages/types");
var State;
(function (State) {
    State[State["Success"] = 0] = "Success";
    State[State["Failure"] = 1] = "Failure";
})(State || (State = {}));
exports.State = State;
class ResponseMessage {
    constructor(state) {
        this.state = state;
    }
}
exports.ReturnMessage = ResponseMessage;
class LoginResponse extends ResponseMessage {
    constructor(state, username) {
        super(state);
        this.type = types_1.MessageTypes.Login;
        this.username = username;
    }
}
exports.ReturnMessageLogin = LoginResponse;
class LogoutResponse extends ResponseMessage {
    constructor(state) {
        super(state);
        this.type = types_1.MessageTypes.Logout;
    }
}
exports.ReturnMessageLogout = LogoutResponse;
class RegistrationResponse extends ResponseMessage {
    constructor(state, username) {
        super(state);
        this.type = types_1.MessageTypes.Register;
        this.username = username;
    }
}
exports.ReturnMessageRegister = RegistrationResponse;
class SearchResponse extends ResponseMessage {
    constructor(state, items) {
        super(state);
        this.type = types_1.MessageTypes.ItemSearch;
        this.items = items;
    }
}
exports.ReturnMessageItem = SearchResponse;
class TransactionResponse extends ResponseMessage {
    constructor(state, list) {
        super(state);
        this.type = types_1.MessageTypes.Transaction;
        this.list = list;
    }
}
exports.ReturnMessageTransaction = TransactionResponse;
class CartResponse extends ResponseMessage {
    constructor(state, cart) {
        super(state);
        this.type = types_1.MessageTypes.ModifyCart;
        this.cart = cart;
    }
}
exports.ReturnMessageCart = CartResponse;
class CheckoutResponse extends ResponseMessage {
    constructor() {
        super(...arguments);
        this.type = types_1.MessageTypes.Checkout;
    }
}
exports.ReturnMessageCheckOut = CheckoutResponse;
//# sourceMappingURL=servermessages.js.map