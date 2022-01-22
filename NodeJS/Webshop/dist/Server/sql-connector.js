"use strict";
var __awaiter = (this && this.__awaiter) || function (thisArg, _arguments, P, generator) {
    function adopt(value) { return value instanceof P ? value : new P(function (resolve) { resolve(value); }); }
    return new (P || (P = Promise))(function (resolve, reject) {
        function fulfilled(value) { try { step(generator.next(value)); } catch (e) { reject(e); } }
        function rejected(value) { try { step(generator["throw"](value)); } catch (e) { reject(e); } }
        function step(result) { result.done ? resolve(result.value) : adopt(result.value).then(fulfilled, rejected); }
        step((generator = generator.apply(thisArg, _arguments || [])).next());
    });
};
Object.defineProperty(exports, "__esModule", { value: true });
exports.retryWith = exports.retryWithTwo = exports.retryWithOne = exports.updateCart = exports.deleteCart = exports.createCart = exports.deleteEntry = exports.modifyCartEntry = exports.getCartFromUser = exports.checkOut = exports.checkOut_old = exports.getTransactionEntries = exports.getTransactions = exports.getItemByID = exports.getItemsByName = exports.getAllItems = exports.createUser = exports.getUser = void 0;
var mysql = require('mysql');
var pool = mysql.createPool({
    connectionLimit: 10,
    host: 'localhost',
    port: '55555',
    user: 'root',
    password: '',
    database: 'warehouse'
});
function getUser(username, pwd) {
    return __awaiter(this, void 0, void 0, function* () {
        return new Promise((resolve, reject) => {
            console.log(`Login ${username} pwd ${pwd}`);
            pool.query('select * from users WHERE email = ? AND password = ?', [username, pwd], function (error, results, fields) {
                if (error) {
                    reject(null);
                }
                else {
                    if (results[0] == undefined) {
                        resolve(null);
                    }
                    else {
                        var user = { userID: results[0].userID, firstName: results[0].firstName, lastName: results[0].lastName, address: results[0].address, password: results[0].password, creditCard: results[0].creditCard, email: results[0].email };
                        resolve(user);
                    }
                }
            });
        });
    });
}
exports.getUser = getUser;
function createUser(user) {
    return __awaiter(this, void 0, void 0, function* () {
        return new Promise((resolve, reject) => {
            pool.query('INSERT INTO users (firstName, lastName, password, email, address,creditCard) VALUES (?,?,?,?,?,?)', [user.firstName, user.lastName, user.password, user.email, user.email, user.email, user.address, user.creditCard], function (error, results, fields) {
                if (error) {
                    reject(null);
                }
                else {
                    var regUser = { userID: results.insertId, firstName: user.firstName, lastName: user.lastName, address: user.address, password: user.password, creditCard: user.creditCard, email: user.email };
                    resolve(regUser);
                }
            });
        });
    });
}
exports.createUser = createUser;
function getAllItems() {
    return __awaiter(this, void 0, void 0, function* () {
        return new Promise((resolve, reject) => {
            pool.query('select * from items', function (error, results, fields) {
                if (error) {
                    reject(null);
                }
                else {
                    var array = new Array();
                    var item;
                    results.forEach(function (value) {
                        item = { itemName: value.itemName, price: value.price, itemDescription: value.itemdesc, itemID: value.ItemID };
                        array.push(item);
                    });
                    resolve(array);
                }
            });
        });
    });
}
exports.getAllItems = getAllItems;
function getItemsByName(searchstr) {
    return __awaiter(this, void 0, void 0, function* () {
        return new Promise((resolve, reject) => {
            pool.query('select * from items where itemName LIKE ?', ["%" + searchstr + "%"], function (error, results, fields) {
                if (error) {
                    reject(null);
                }
                else {
                    var array = new Array();
                    var item;
                    results.forEach(function (value) {
                        item = { itemName: value.itemName, price: value.price, itemDescription: value.itemdesc, itemID: value.ItemID };
                        array.push(item);
                    });
                    resolve(array);
                }
            });
        });
    });
}
exports.getItemsByName = getItemsByName;
function getItemByID(itemID) {
    return __awaiter(this, void 0, void 0, function* () {
        return new Promise((resolve, reject) => {
            pool.query('select * from items where itemID = ?', [itemID], function (error, results, fields) {
                if (error) {
                    reject(null);
                }
                else {
                    var item = { itemName: results[0].itemName, price: results[0].price, itemDescription: results[0].itemDescription, itemID: itemID };
                    resolve(item);
                }
            });
        });
    });
}
exports.getItemByID = getItemByID;
// TODO 
function getTransactions(user) {
    return __awaiter(this, void 0, void 0, function* () {
        return new Promise((resolve, reject) => {
            pool.query('select * from transactions  where userID = ?', [user.userID], function (error, results, fields) {
                return __awaiter(this, void 0, void 0, function* () {
                    if (error) {
                        reject([]);
                    }
                    else {
                        let list = Array();
                        let i = 0;
                        var transaction;
                        var entries;
                        yield new Promise((resolve, reject) => __awaiter(this, void 0, void 0, function* () {
                            for (i <= 0; i < results.length; i++) {
                                entries = yield getTransactionEntries(results[i].transactionID);
                                transaction = { transactionID: results[i].transactionID, userID: user.userID, entries: entries, date: results[i].orderDate };
                                list.push(transaction);
                            }
                            resolve(list);
                        }));
                        resolve(list);
                    }
                });
            });
        });
    });
}
exports.getTransactions = getTransactions;
function getTransactionEntries(transactionID) {
    return __awaiter(this, void 0, void 0, function* () {
        return new Promise((resolve, reject) => {
            pool.query('select * from transactionEntries,items  where transactionID = ? and transactionEntries.itemID = items.itemID', [transactionID], function (error, results, fields) {
                return __awaiter(this, void 0, void 0, function* () {
                    var entries = Array();
                    if (error)
                        return reject([]);
                    var entry;
                    var item;
                    var i;
                    for (i = 0; i < results.length; i++) {
                        item = { itemID: results[i].itemID, itemDescription: results[i].itemdesc, price: results[i].price, itemName: results[i].itemName };
                        entry = { item: item, amount: results[i].amount, transactionID };
                        entries.push(entry);
                    }
                    resolve(entries);
                });
            });
        });
    });
}
exports.getTransactionEntries = getTransactionEntries;
//TODO change method that it resolves like javaEE
function checkOut_old(user) {
    return __awaiter(this, void 0, void 0, function* () {
        // set autocommit
        return new Promise((resolve, reject) => {
            getCartFromUser(user).then(cart => {
                if (cart != null && cart.entries.length > 0) {
                    console.log(`userID ${user.userID} cartLength ${cart.entries.length}`);
                    pool.query('insert into  transactions (userID, orderDate) values (?,?);', [user.userID, new Date()], function (error, results, fields) {
                        return __awaiter(this, void 0, void 0, function* () {
                            if (error)
                                return reject(false);
                            var id = results.insertId;
                            console.log("id", id);
                            cart.entries.forEach(function (entry) {
                                pool.query('INSERT INTO transactionEntries (transactionID,itemID,amount) VALUES (?,?,?)', [id, entry.item.itemID, entry.amount], function (error, results, fields) {
                                    return __awaiter(this, void 0, void 0, function* () {
                                        if (error)
                                            return reject(false);
                                    });
                                });
                            });
                            console.log("delete Cart");
                            deleteCart(cart).then((results) => {
                                resolve(results);
                            }, (reject) => {
                                reject(reject);
                            });
                            //resolve(true);
                        });
                    });
                }
                else {
                    resolve(false);
                }
            }).catch(() => {
                reject(false);
            });
        });
    });
}
exports.checkOut_old = checkOut_old;
function insertTransactionEntry(id, entry) {
    return __awaiter(this, void 0, void 0, function* () {
        return new Promise((resolve, reject) => {
            pool.query('INSERT INTO transactionEntries (transactionID,itemID,amount) VALUES (?,?,?)', [id, entry.item.itemID, entry.amount], function (error, results, fields) {
                return __awaiter(this, void 0, void 0, function* () {
                    if (error)
                        return resolve(false);
                    resolve(true);
                });
            });
        });
    });
}
function checkOut(user, cart) {
    return __awaiter(this, void 0, void 0, function* () {
        return new Promise((resolve, reject) => {
            pool.query('insert into  transactions (userID, orderDate) values (?,?);', [user.userID, new Date()], function (error, results, fields) {
                return __awaiter(this, void 0, void 0, function* () {
                    if (error)
                        return reject(false);
                    var id = results.insertId;
                    console.log("id", id);
                    var result = true;
                    var i = 0;
                    for (i = 0; i < cart.entries.length; i++) {
                        if (!(yield insertTransactionEntry(id, cart.entries[i]))) {
                            reject(false);
                        }
                    }
                    resolve(result);
                });
            });
        });
    });
}
exports.checkOut = checkOut;
function getCartEntries(cartID) {
    return __awaiter(this, void 0, void 0, function* () {
        return new Promise((resolve, reject) => {
            pool.query('SELECT * FROM cartEntries,items where cartID = ? && cartEntries.itemID = items.itemID ', [cartID], function (error, results, fields) {
                return __awaiter(this, void 0, void 0, function* () {
                    var entries = Array();
                    if (error)
                        return reject([]);
                    var entry;
                    var item;
                    var i;
                    for (i = 0; i < results.length; i++) {
                        item = { itemID: results[i].itemID, itemDescription: results[i].itemdesc, price: results[i].price, itemName: results[i].itemName };
                        entry = { item: item, amount: results[i].amount };
                        entries.push(entry);
                    }
                    resolve(entries);
                });
            });
        });
    });
}
// TODO 
function getCartFromUser(user) {
    return __awaiter(this, void 0, void 0, function* () {
        return new Promise((resolve, reject) => {
            pool.query('SELECT * FROM carts where userID = ?', [user.userID], function (error, results, fields) {
                return __awaiter(this, void 0, void 0, function* () {
                    if (error) {
                        reject(null);
                    }
                    else {
                        if (results[0] == undefined) {
                            createCart(user).then(cart => { resolve(cart); });
                        }
                        else {
                            getCartEntries(results[0].cartID).then(entries => {
                                var cart = { cartID: results[0].cartID, entries: entries };
                                resolve(cart);
                            }).catch(() => {
                                // TODO 
                                reject(null);
                            });
                        }
                    }
                });
            });
        });
    });
}
exports.getCartFromUser = getCartFromUser;
function modifyCartEntry(cartEntry, cart) {
    return __awaiter(this, void 0, void 0, function* () {
        return new Promise((result, reject) => {
            pool.query('replace into cartEntries (cartID, itemID,amount) values (?,?,?)', [cart.cartID, cartEntry.item.itemID, cartEntry.amount], function (error, results, fields) {
                return __awaiter(this, void 0, void 0, function* () {
                    if (error) {
                        reject(false);
                    }
                    else {
                        result(true);
                    }
                });
            });
        });
    });
}
exports.modifyCartEntry = modifyCartEntry;
function deleteEntry(cart, cartEntry) {
    return __awaiter(this, void 0, void 0, function* () {
        return new Promise((result, reject) => {
            pool.query('delete from cartEntries where cartID = ? and itemID =?', [cart.cartID, cartEntry.item.itemID], function (error, results, fields) {
                return __awaiter(this, void 0, void 0, function* () {
                    if (error) {
                        reject(false);
                    }
                    else {
                        result(true);
                    }
                });
            });
        });
    });
}
exports.deleteEntry = deleteEntry;
function createCart(user) {
    return __awaiter(this, void 0, void 0, function* () {
        return new Promise((result, reject) => {
            pool.query('insert into carts (userID) values (?)', [user.userID], function (error, results, fields) {
                return __awaiter(this, void 0, void 0, function* () {
                    if (error) {
                        reject(false);
                    }
                    else {
                        result(getCartFromUser(user));
                    }
                });
            });
        });
    });
}
exports.createCart = createCart;
function deleteCart(cart) {
    return __awaiter(this, void 0, void 0, function* () {
        return new Promise((result, reject) => {
            console.log(`begin delete cart ${cart.cartID}`);
            pool.query('delete from cartEntries where cartID = ?', [cart.cartID], function (error, results, fields) {
                return __awaiter(this, void 0, void 0, function* () {
                    console.log(`error ${error}`);
                    if (error) {
                        reject(false);
                    }
                    else {
                        console.log("delete cart");
                        pool.query('delete from carts where cartID = ?', [cart.cartID], function (error, results, fields) {
                            return __awaiter(this, void 0, void 0, function* () {
                                console.log("delete cart");
                                console.log(`${error}`);
                                if (error) {
                                    reject(false);
                                }
                                else {
                                    result(true);
                                }
                            });
                        });
                    }
                });
            });
        });
    });
}
exports.deleteCart = deleteCart;
function updateCart(user, cart) {
    return __awaiter(this, void 0, void 0, function* () {
        getCartFromUser(user).then((result) => __awaiter(this, void 0, void 0, function* () {
            var dbcart = result;
            cart.entries.forEach(function (value) {
                modifyCartEntry(value, dbcart);
            });
        }));
    });
}
exports.updateCart = updateCart;
function retryWithOne(call, param) {
    return __awaiter(this, void 0, void 0, function* () {
        return call(param)
            .catch(() => {
            return call(param);
        })
            .catch(() => {
            return call(param);
        }).catch(() => {
            return call(param);
        });
    });
}
exports.retryWithOne = retryWithOne;
function retryWithTwo(call, param1, param2) {
    return __awaiter(this, void 0, void 0, function* () {
        return call(param1, param2)
            .catch(() => {
            return call(param1, param2);
        })
            .catch(() => {
            return call(param1, param2);
        }).catch(() => {
            return call(param1, param2);
        });
    });
}
exports.retryWithTwo = retryWithTwo;
function retryWith(call) {
    return __awaiter(this, void 0, void 0, function* () {
        return call()
            .catch(() => {
            return call();
        })
            .catch(() => {
            return call();
        }).catch(() => {
            return call();
        });
    });
}
exports.retryWith = retryWith;
//# sourceMappingURL=sql-connector.js.map