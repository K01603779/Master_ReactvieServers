"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
const types_1 = require("../Messages/types");
var State;
(function (State) {
    State[State["Success"] = 0] = "Success";
    State[State["Failure"] = 1] = "Failure";
})(State || (State = {}));
exports.State = State;
class ReturnMessage {
    constructor(state) {
        this.state = state;
    }
}
exports.ReturnMessage = ReturnMessage;
class ReturnMessageLogin extends ReturnMessage {
    constructor(state, username) {
        super(state);
        this.type = types_1.MessageTypes.Login;
        this.username = username;
    }
}
exports.ReturnMessageLogin = ReturnMessageLogin;
class ReturnMessageLogout extends ReturnMessage {
    constructor(state) {
        super(state);
        this.type = types_1.MessageTypes.Logout;
    }
}
exports.ReturnMessageLogout = ReturnMessageLogout;
class ReturnMessageRegister extends ReturnMessage {
    constructor(state, username) {
        super(state);
        this.type = types_1.MessageTypes.Register;
        this.username = username;
    }
}
exports.ReturnMessageRegister = ReturnMessageRegister;
class ReturnMessageItem extends ReturnMessage {
    constructor(state, items) {
        super(state);
        this.type = types_1.MessageTypes.ItemSearch;
        this.items = items;
    }
}
exports.ReturnMessageItem = ReturnMessageItem;
class ReturnMessageTransaction extends ReturnMessage {
    constructor(state, list) {
        super(state);
        this.type = types_1.MessageTypes.Transaction;
        this.list = list;
    }
}
exports.ReturnMessageTransaction = ReturnMessageTransaction;
class ReturnMessageCart extends ReturnMessage {
    constructor(state, cart) {
        super(state);
        this.type = types_1.MessageTypes.ModifyCart;
        this.cart = cart;
    }
}
exports.ReturnMessageCart = ReturnMessageCart;
class ReturnMessageCheckOut extends ReturnMessage {
    constructor() {
        super(...arguments);
        this.type = types_1.MessageTypes.Checkout;
    }
}
exports.ReturnMessageCheckOut = ReturnMessageCheckOut;
//# sourceMappingURL=servermessages.js.map