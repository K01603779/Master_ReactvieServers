import { TransactionEntry, User, Transaction, Item, Cart, CartEntry } from "../Entities/entities";

var mysql = require('mysql');
var pool = mysql.createPool({
    connectionLimit: 10,
    host: 'localhost',
    port: '55555',
    user: 'root',
    password: '',
    database: 'warehouse'
});


export async function getUser(username: string, pwd: string): Promise<User> { // TODO change
    return new Promise<User>((resolve, reject) => {
        console.log(`Login ${username} pwd ${pwd}`)
        pool.query('select * from users WHERE email = ? AND password = ?', [username, pwd], function (error, results, fields) {
            if (error) { reject(null); }
            else {
                if (results[0] == undefined) { resolve(null) } else {
                    var user: User = { userID: results[0].userID, firstName: results[0].firstName, lastName: results[0].lastName, address: results[0].address, password: results[0].password, creditCard: results[0].creditCard, email: results[0].email };
                    resolve(user);
                }
            }
        });
    });
}

export async function createUser(user: User): Promise<User> {
    return new Promise<User>((resolve, reject) => {
        pool.query('INSERT INTO users (firstName, lastName, password, email, address,creditCard) VALUES (?,?,?,?,?,?)', [user.firstName, user.lastName, user.password, user.email, user.email, user.email, user.address, user.creditCard], function (error, results, fields) {
            if (error) { reject(null); } else {
                var regUser: User = { userID: results.insertId, firstName: user.firstName, lastName: user.lastName, address: user.address, password: user.password, creditCard: user.creditCard, email: user.email };
                resolve(regUser);
            }
        });
    });
}

export async function getAllItems(): Promise<Array<Item>> {
    return new Promise<Array<Item>>((resolve, reject) => {
        pool.query('select * from items', function (error, results, fields) {
            if (error) { reject(null); }
            else {
                var array: Array<Item> = new Array<Item>();
                var item: Item;
                results.forEach(function (value) {
                    item = { itemName: value.itemName, price: value.price, itemDescription: value.itemdesc, itemID: value.ItemID };
                    array.push(item);
                });
                resolve(array);
            }
        });
    });
}

export async function getItemsByName(searchstr: string): Promise<Array<Item>> {
    return new Promise<Array<Item>>((resolve, reject) => {
        pool.query('select * from items where itemName LIKE ?', ["%" + searchstr + "%"], function (error, results, fields) {
            if (error) { reject(null); } else {
                var array: Array<Item> = new Array<Item>();
                var item: Item;
                results.forEach(function (value) {
                    item = { itemName: value.itemName, price: value.price, itemDescription: value.itemdesc, itemID: value.ItemID };
                    array.push(item);
                });
                resolve(array);
            }
        });

    });
}

export async function getItemByID(itemID: number): Promise<Item> {
    return new Promise<Item>((resolve, reject) => {
        pool.query('select * from items where itemID = ?', [itemID], function (error, results, fields) {
            if (error) { reject(null); }
            else {
                var item = { itemName: results[0].itemName, price: results[0].price, itemDescription: results[0].itemDescription, itemID: itemID };
                resolve(item);
            }
        });
    });
}

// TODO 
export async function getTransactions(user: User): Promise<Array<Transaction>> {
    return new Promise<Array<Transaction>>((resolve, reject) => {
        pool.query('select * from transactions  where userID = ?', [user.userID], async function (error, results, fields) {
            if (error) { reject([]); }
            else {
                let list = Array<Transaction>();
                let i = 0;
                var transaction: Transaction;
                var entries: Array<TransactionEntry>;
                await new Promise(async (resolve, reject) => {
                    for (i <= 0; i < results.length; i++) {
                        entries = await getTransactionEntries(results[i].transactionID);
                        transaction = { transactionID: results[i].transactionID, userID: user.userID, entries: entries, date: results[i].orderDate };
                        list.push(transaction);
                    }
                    resolve(list);
                });
                resolve(list);
            }
        });
    });

}

export async function getTransactionEntries(transactionID: number): Promise<Array<TransactionEntry>> {
    return new Promise((resolve, reject) => {
        pool.query('select * from transactionEntries,items  where transactionID = ? and transactionEntries.itemID = items.itemID', [transactionID], async function (error, results, fields) {
            var entries = Array<TransactionEntry>();
            if (error) return reject([]);
            var entry: TransactionEntry;
            var item: Item;
            var i;
            for (i = 0; i < results.length; i++) {
                item = { itemID: results[i].itemID, itemDescription: results[i].itemdesc, price: results[i].price, itemName: results[i].itemName };
                entry = { item: item, amount: results[i].amount, transactionID };
                entries.push(entry);
            }
            resolve(entries);
        });
    });
}

//TODO change method that it resolves like javaEE
export async function checkOutOld(user: User): Promise<boolean> {
    // set autocommit
    return new Promise((resolve, reject) => {
        getCartFromUser(user).then(cart => {
            if (cart != null && cart.entries.length > 0) {
                console.log(`userID ${user.userID} cartLength ${cart.entries.length}`);
                pool.query('insert into  transactions (userID, orderDate) values (?,?);', [user.userID, new Date()], async function (error, results, fields) {
                    if (error) return reject(false);
                    var id = results.insertId;
                    console.log("id", id);
                    cart.entries.forEach(function (entry) {
                        pool.query('INSERT INTO transactionEntries (transactionID,itemID,amount) VALUES (?,?,?)', [id, entry.item.itemID, entry.amount], async function (error, results, fields) {
                            if (error) return reject(false);
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
            } else {
                resolve(false);
            }
        }).catch(() => {
            reject(false);
        })
    });

}
async function insertTransactionEntry(id: any, entry: CartEntry): Promise<boolean> {
    return new Promise((resolve, reject) => {
        pool.query('INSERT INTO transactionEntries (transactionID,itemID,amount) VALUES (?,?,?)', [id, entry.item.itemID, entry.amount], async function (error, results, fields) {
            if (error) return resolve(false);
            resolve(true);
        });
    });
}

export async function checkout(user: User, cart: Cart): Promise<boolean> {
    return new Promise((resolve, reject) => {
        pool.query('insert into  transactions (userID, orderDate) values (?,?);', [user.userID, new Date()], async function (error, results, fields) {
            if (error) return reject(false);
            var id = results.insertId;
            console.log("id", id);
            var result = true;
            var i = 0;
            for (i = 0; i < cart.entries.length; i++) {
                if (!await insertTransactionEntry(id, cart.entries[i])) {
                    reject(false);
                }

            }
            resolve(result);
        });
    });
}

async function getCartEntries(cartID: number): Promise<Array<CartEntry>> {
    return new Promise((resolve, reject) => {
        pool.query('SELECT * FROM cartEntries,items where cartID = ? && cartEntries.itemID = items.itemID ', [cartID], async function (error, results, fields) {
            var entries = Array<CartEntry>();
            if (error) return reject([]);
            var entry: CartEntry;
            var item: Item;
            var i;
            for (i = 0; i < results.length; i++) {
                item = { itemID: results[i].itemID, itemDescription: results[i].itemdesc, price: results[i].price, itemName: results[i].itemName };
                entry = { item: item, amount: results[i].amount };
                entries.push(entry);
            }
            resolve(entries);
        });
    });
}

// TODO 
export async function getCartFromUser(user: User): Promise<Cart> {
    return new Promise<Cart>((resolve, reject) => {
        pool.query('SELECT * FROM carts where userID = ?', [user.userID], async function (error, results, fields) {
            if (error) { reject(null); } else {
                if (results[0] == undefined) {
                    createCart(user).then(cart => { resolve(cart) });
                } else {
                    getCartEntries(results[0].cartID).then(entries => {
                        var cart: Cart = { cartID: results[0].cartID, entries: entries };
                        resolve(cart);
                    }).catch(() => {
                        // TODO 
                        reject(null);
                    })
                }
            }
        });
    });
}

export async function modifyCartEntry(cartEntry: CartEntry, cart: Cart): Promise<boolean> {
    return new Promise((result, reject) => {
        pool.query('replace into cartEntries (cartID, itemID,amount) values (?,?,?)', [cart.cartID, cartEntry.item.itemID, cartEntry.amount], async function (error, results, fields) {
            if (error) {
                reject(false);
            } else {
                result(true);
            }
        });
    });
}

export async function deleteEntry(cart: Cart, cartEntry: CartEntry): Promise<boolean> {
    return new Promise((result, reject) => {
        pool.query('delete from cartEntries where cartID = ? and itemID =?', [cart.cartID, cartEntry.item.itemID], async function (error, results, fields) {
            if (error) { reject(false); }
            else {
                result(true);
            }
        });
    });
}

export async function createCart(user: User): Promise<Cart> {
    return new Promise((result, reject) => {
        pool.query('insert into carts (userID) values (?)', [user.userID], async function (error, results, fields) {
            if (error) { reject(false); }
            else {
                result(getCartFromUser(user));
            }
        });
    });
}

export async function deleteCart(cart: Cart): Promise<boolean> {
    return new Promise((result, reject) => {
        console.log(`begin delete cart ${cart.cartID}`);
        pool.query('delete from cartEntries where cartID = ?', [cart.cartID], async function (error, results, fields) {
            console.log(`error ${error}`);
            if (error) { reject(false); } else {
                console.log("delete cart");
                pool.query('delete from carts where cartID = ?', [cart.cartID], async function (error, results, fields) {
                    console.log("delete cart");
                    console.log(`${error}`);
                    if (error) { reject(false); } else {
                        result(true);
                    }
                });
            }
        });
    });
}

export async function updateCart(user: User, cart: Cart) {
    getCartFromUser(user).then(async (result) => {
        var dbcart = result;
        cart.entries.forEach(function (value) {
            modifyCartEntry(value, dbcart);
        });
    });
}

export async function retryWithOne<T>(call: (arg0: any) => Promise<T>, param: any) {
    return call(param)
        .catch(() => {
            return call(param);
        })
        .catch(() => {
            return call(param);
        }).catch(() => {
            return call(param);
        });
}
export async function retryWithTwo(call: any, param1: any, param2: any) {
    return call(param1, param2)
        .catch(() => {
            return call(param1, param2);
        })
        .catch(() => {
            return call(param1, param2);
        }).catch(() => {
            return call(param1, param2);
        });
}
export async function retryWith(call: any) {
    return call()
        .catch(() => {
            return call();
        })
        .catch(() => {
            return call();
        }).catch(() => {
            return call();
        });
}
