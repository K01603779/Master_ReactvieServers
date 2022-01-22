"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
const types_1 = require("../Messages/types");
exports.MessageTypes = types_1.MessageTypes;
class RequestMessage {
}
class LoginMessage extends RequestMessage {
    constructor(email, pwd) {
        super();
        this.type = types_1.MessageTypes.Login;
        this.email = email;
        this.pwd = pwd;
    }
}
exports.LoginMessage = LoginMessage;
class LogoutMessage extends RequestMessage {
    constructor() {
        super(...arguments);
        this.type = types_1.MessageTypes.Logout;
    }
}
exports.LogoutMessage = LogoutMessage;
class CheckOutMessage extends RequestMessage {
    constructor() {
        super(...arguments);
        this.type = types_1.MessageTypes.Checkout;
    }
}
exports.CheckOutMessage = CheckOutMessage;
class TransactionsMessage extends RequestMessage {
    constructor() {
        super(...arguments);
        this.type = types_1.MessageTypes.Transaction;
    }
}
exports.TransactionsMessage = TransactionsMessage;
class ItemSearchMessage extends RequestMessage {
    constructor(search) {
        super();
        this.type = types_1.MessageTypes.ItemSearch;
        this.searchString = search;
    }
}
exports.ItemSearchMessage = ItemSearchMessage;
class CartModificationMessage extends RequestMessage {
    constructor(itemID, amount, add) {
        super();
        this.type = types_1.MessageTypes.ModifyCart;
        this.add = add;
        this.amount = amount;
        this.itemID = itemID;
    }
}
exports.CartModificationMessage = CartModificationMessage;
class RegisterMessage extends RequestMessage {
    constructor(email, firstName, lastName, address, password, card) {
        super();
        this.type = types_1.MessageTypes.Register;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.address = address;
        this.password = password;
        this.creditCard = card;
    }
}
exports.RegisterMessage = RegisterMessage;
class CartMessage extends RequestMessage {
    constructor() {
        super(...arguments);
        this.type = types_1.MessageTypes.Cart;
    }
}
exports.CartMessage = CartMessage;
//# sourceMappingURL=clientmessages.js.map