"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
class Message {
}
class LoginMessage extends Message {
    constructor(email, pwd) {
        super();
        this.type = MessageTypes.Login;
        this.email = email;
        this.pwd = pwd;
    }
}
exports.LoginMessage = LoginMessage;
class LogoutMessage extends Message {
    constructor() {
        super(...arguments);
        this.type = MessageTypes.Logout;
    }
}
exports.LogoutMessage = LogoutMessage;
class CheckOutMessage extends Message {
    constructor() {
        super(...arguments);
        this.type = MessageTypes.Checkout;
    }
}
exports.CheckOutMessage = CheckOutMessage;
class TransactionsMessage extends Message {
    constructor() {
        super(...arguments);
        this.type = MessageTypes.Transaction;
    }
}
exports.TransactionsMessage = TransactionsMessage;
class ItemSearchMessage extends Message {
    constructor() {
        super(...arguments);
        this.type = MessageTypes.ItemSearch;
    }
}
exports.ItemSearchMessage = ItemSearchMessage;
class CartModificationMessage extends Message {
    constructor() {
        super(...arguments);
        this.type = MessageTypes.ModifyCart;
    }
}
exports.CartModificationMessage = CartModificationMessage;
class RegisterMessage extends Message {
    constructor() {
        super(...arguments);
        this.type = MessageTypes.Register;
    }
}
exports.RegisterMessage = RegisterMessage;
var MessageTypes;
(function (MessageTypes) {
    MessageTypes[MessageTypes["Login"] = 1] = "Login";
    MessageTypes[MessageTypes["Logout"] = 2] = "Logout";
    MessageTypes[MessageTypes["Register"] = 3] = "Register";
    MessageTypes[MessageTypes["ItemSearch"] = 4] = "ItemSearch";
    MessageTypes[MessageTypes["ModifyCart"] = 5] = "ModifyCart";
    MessageTypes[MessageTypes["Checkout"] = 6] = "Checkout";
    MessageTypes[MessageTypes["Transaction"] = 7] = "Transaction";
})(MessageTypes || (MessageTypes = {}));
exports.MessageTypes = MessageTypes;
//# sourceMappingURL=messages.js.map