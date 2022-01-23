
const address = "http://localhost:8080/JSP-Store/";
const loginServlet = "LoginServlet";
const logoutServlet = "LogoutServlet";
const transactionServlet = "TransactionServlet";
const searchServlet = "SearchItemServlet";
//const checkOutServlet = "CheckOutServletClient";
const checkOutServlet = "CheckOutServlet";
const registerServlet = "RegisterServlet";
//const cartModificationServlet = "CartModificationServletClient";
const cartModificationServlet = "CartModificationServlet";
const getCartServlet = "GetCartServlet";

export function logoutRequest(callback) {
    let url = new URL(address + logoutServlet);
    fetch(url, {
        method: 'GET',
        credentials: 'include',
        mode: 'cors'
    })
        .then(response => {
            return response.json()
        }
        )
        .then(data => {
            if (data.State === "Success") {
                //document.cookie = "JSESSIONID = " + "";
                callback(true);
            } else {
                callback(false);
            }
        });
}
export function loginRequest(user, password, callback) {
    let url = new URL(address + loginServlet);
    url.searchParams.set('user', user);
    url.searchParams.set('pwd', password);
    fetch(url, {
        method: 'GET',
        credentials: 'include'
    })
        .then(response => {
            return response.json()
        })
        .then(data => {
            if (data.State === "Success") {
                //sessionID = data.SessionID;
                //document.cookie = "JSESSIONID = " + sessionID;
                callback(true);
            } else {
                callback(false);
            }
        });
}

export function viewTransactionsRequest(callback) {
    let url = new URL(address + transactionServlet);
    fetch(url, {
        method: 'GET',
        //mode: 'no-cors',
        credentials: 'include',
        mode: 'cors',
    })
        .then(response => {
            return response.json()
        })
        .then(data => {
            callback(data);
        }).then((response) => {
            console.log('success')
        }).catch(function (err) {
            console.log(err);
        });
}

export function searchItemRequest(searchstr, callback) {
    let url = new URL(address + searchServlet);
    url.searchParams.set('item', searchstr);
    fetch(url, {
        method: "GET",
        mode: "cors",
        credentials: "include"
    }).then(
        response => response.json()).then(data => {
            if (data.State === "Success") {
                //sessionID = data.SessionID;
                //document.cookie = "JSESSIONID = " + sessionID;
                callback(true, data);
            } else {
                callback(false, data);
            }
        }
        )
}
function modifyCart(url, callback) {
    fetch(url, {
        method: "GET",
        mode: "cors",
        credentials: "include"
    }).then(
        response => response.json()).then(data => {
            if (data.State === "Success") {
                //sessionID = data.SessionID;
                //document.cookie = "JSESSIONID = " + sessionID;
                callback(true, data);
            } else {
                callback(false, data);
            }
        }
        );
}

export function addItemRequest(itemid, amount, callback) {
    let url = new URL(address + cartModificationServlet);
    url.searchParams.set('itemid', itemid);
    url.searchParams.set('amount', amount);
    url.searchParams.set('add', true);
    modifyCart(url, callback);
}
export function removeItemRequest(itemid, amount, callback) {
    let url = new URL(address + cartModificationServlet);
    url.searchParams.set('itemid', itemid);
    url.searchParams.set('amount', amount);
    url.searchParams.set('remove', true);
    modifyCart(url, callback);
}
export function checkOutRequest(callback) {
    let url = new URL(address + checkOutServlet);
    fetch(url, {
        method: "GET",
        mode: "cors",
        credentials: "include"
    }).then(
        response => response.json()).then(data => {
            if (data.State === "Success") {
                callback(true, data);
            } else {
                callback(false, data);
            }
        }
        )
}
export function registerRequest(email, firstname, lastname, password, useraddr, creditCard, callback) {
    let url = new URL(address + registerServlet);
    url.searchParams.set('firstName', firstname);
    url.searchParams.set('lastName', lastname);
    url.searchParams.set('address', useraddr);
    url.searchParams.set('email', email);
    url.searchParams.set('password', password);
    url.searchParams.set('card', creditCard);
    fetch(url, {
        method: "GET",
        mode: "cors",
        credentials: "include"
    }).then(
        response => response.json()).then(data => {
            if (data.State === "Success") {
                callback(true, data);
            } else {
                callback(false, data);
            }
        }
        )

}
export function getCartRequest(callback) {
    let url = new URL(address + getCartServlet);
    fetch(url, {
        method: 'GET',
        credentials: 'include',
        mode: 'cors'
    })
        .then(response => {
            return response.json()
        }
        )
        .then(data => {
            if (data.State === "Success") {
                callback(true,data);
            } else {
                callback(false,data);
            }
        });
}