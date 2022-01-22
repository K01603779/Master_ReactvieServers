"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.CartMessage = exports.MessageTypes = exports.RegisterMessage = exports.CartModificationMessage = exports.ItemSearchMessage = exports.TransactionsMessage = exports.CheckOutMessage = exports.LogoutMessage = exports.LoginMessage = void 0;
const types_1 = require("../Messages/types");
Object.defineProperty(exports, "MessageTypes", { enumerable: true, get: function () { return types_1.MessageTypes; } });
class RequestMessage {
}
class LoginRequest extends RequestMessage {
    constructor(email, pwd) {
        super();
        this.type = types_1.MessageTypes.Login;
        this.email = email;
        this.pwd = pwd;
    }
}
exports.LoginMessage = LoginRequest;
class LogoutRequest extends RequestMessage {
    constructor() {
        super(...arguments);
        this.type = types_1.MessageTypes.Logout;
    }
}
exports.LogoutMessage = LogoutRequest;
class CheckoutRequest extends RequestMessage {
    constructor() {
        super(...arguments);
        this.type = types_1.MessageTypes.Checkout;
    }
}
exports.CheckOutMessage = CheckoutRequest;
class TransactionRequest extends RequestMessage {
    constructor() {
        super(...arguments);
        this.type = types_1.MessageTypes.Transaction;
    }
}
exports.TransactionsMessage = TransactionRequest;
class SearchRequest extends RequestMessage {
    constructor(search) {
        super();
        this.type = types_1.MessageTypes.ItemSearch;
        this.searchString = search;
    }
}
exports.ItemSearchMessage = SearchRequest;
class CartModificationRequest extends RequestMessage {
    constructor(itemID, amount, add) {
        super();
        this.type = types_1.MessageTypes.ModifyCart;
        this.add = add;
        this.amount = amount;
        this.itemID = itemID;
    }
}
exports.CartModificationMessage = CartModificationRequest;
class RegistrationRequest extends RequestMessage {
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
exports.RegisterMessage = RegistrationRequest;
class CartRequest extends RequestMessage {
    constructor() {
        super(...arguments);
        this.type = types_1.MessageTypes.Cart;
    }
}
exports.CartMessage = CartRequest;
//# sourceMappingURL=clientmessages.js.map