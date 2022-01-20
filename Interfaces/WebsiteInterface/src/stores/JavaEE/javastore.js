

import { loginRequest, logoutRequest, searchItemRequest, viewTransactionsRequest, addItemRequest, removeItemRequest, checkOutRequest, registerRequest,getCartRequest } from "../JavaEE/javaconnector.js";
import {loggedIn,user,register,transactions,searchresults,cart} from "../store";

export function requestLogin(username, password) {
    // TODO login
    loginRequest(username, password, function (response) {
        if (response === true) {
            loggedIn.set(true);
            register.set(false);
            user.set(username);
            getTransactions();
            getCart();
            
        } else {
            alert("login not sucessfull")
        }
    })
    //getTransactions();

}
export function requestLogout() {
    logoutRequest(function (response) {
        if (response === true) {
            loggedIn.set(false);
            user.set("no user");
            transactions.set([]);
            cart.set([]);
        } else {
            alert("logout not sucessfull")
        }
    });
}

export function requestRegisterUser(email, password, firstname, lastname, address,creditCard) {
    registerRequest(email, firstname, lastname, password, address,creditCard, function (result, data) {
        if (result == true) {
            loggedIn.set(true);
            register.set(false);
            user.set(email);
            getTransactions();
        } else {
            alert("Registration was not successfull");
        }
    });

}

function getTransactions() {
    viewTransactionsRequest(function (response) {
        transactions.set(response.list);
    });
}
export function requestSearch(param) {
    searchItemRequest(param, function (response, result) {
        if (response === true) {
            searchresults.set(result.search);
        } else {
            alert("search not sucessfull")
        }
    });
}
export function requestAddItem(item, amount) {
    addItemRequest(item, amount, function (result, data) {
        if (result == true) {
            cart.set(data.cart);
        }
    });
}
export function requestRemoveItem(item, amount) {
    removeItemRequest(item, amount, function (result, data) {
        if (result == true) {
            cart.set(data.cart);
        }
    });
}
export function requestCheckOut() {
    checkOutRequest(function (result, data) {
        if (result == true) {
            getTransactions();
            cart.set([]);
        }
    });
}

function getCart() {
    getCartRequest(function (result, data) {
        console.log(result);
        if (result == true) {
            cart.set(data.cart);
        }else{
            cart.set([]);
        }
    });
}
