import { writable } from "svelte/store";
//import { loginRequest, logoutRequest, searchItemRequest, viewTransactionsRequest, addItemRequest, removeItemRequest, checkOutRequest, registerRequest,getCartRequest } from "./JavaEE/javaconnector.js";
//import { requestLogin, requestLogout, requestRegisterUser, requestAddItem, requestRemoveItem, requestSearch, requestCheckOut } from "../stores/JavaEE/javastore.js";
import { requestLogin, requestLogout, requestRegisterUser, requestAddItem, requestRemoveItem, requestSearch, requestCheckOut } from "../stores/NodeJS/nodeJSconnector.js";

export var loggedIn = writable(false);
export var user = writable("no user");
export var register = writable(false);
export var transactions = writable([]);
export var searchresults = writable([]);
export var cart = writable([]);

export function login(username, password) {
    requestLogin(username, password);
    /*loginRequest(username, password, function (response) {
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
    //getTransactions();*/


}
export function logout() {
    /*logoutRequest(function (response) {
        if (response === true) {
            loggedIn.set(false);
            user.set("no user");
            transactions.set([]);
            setCart([]);
        } else {
            alert("logout not sucessfull")
        }
    });*/
    requestLogout();
}
export function showRegister() {
    register.set(true);
}
export function registerUser(email, password, firstname, lastname, address, creditCard) {
    /*registerRequest(email, firstname, lastname, password, address,creditCard, function (result, data) {
        if (result == true) {
            loggedIn.set(true);
            register.set(false);
            user.set(email);
            getTransactions();
        } else {
            alert("Registration was not successfull");
        }
    });*/
    requestRegisterUser(email, firstname, lastname, password, address, creditCard);
}
export function search(param) {
    requestSearch(param);
    /*searchItemRequest(param, function (response, result) {
        if (response === true) {
            searchresults.set(result.search);
        } else {
            alert("search not sucessfull")
        }
    });*/
}
export function addItem(item, amount) {
    requestAddItem(item, amount);
    /*addItemRequest(item, amount, function (result, data) {
        if (result == true) {
            setCart(data.cart);
        }
    });*/
}
export function removeItem(item, amount) {
    requestRemoveItem(item, amount);
    /*removeItemRequest(item, amount, function (result, data) {
        if (result == true) {
            setCart(data.cart);
        }
    });*/
}
export function checkOut() {
    requestCheckOut();/*
    checkOutRequest(function (result, data) {
        if (result == true) {
            getTransactions();
            setCart([]);
        }
    });*/
}

