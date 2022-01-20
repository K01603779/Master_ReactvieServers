interface User {
    userID: number;
    password: string;
    email: string;
    address: string;
    firstName: string;
    lastName: string;
    creditCard: string;
}

interface Item {
    itemID: number;
    itemName: string;
    itemDescription: string;
    price: number;
}

interface Transaction {
    transactionID: number;
    entries: Array<TransactionEntry>;
    userID: number;
    date: Date;
}

interface TransactionEntry {
    transactionID: number;
    item: Item;
    amount: number;
}

interface Cart {
    cartID: number;
    entries: Array<CartEntry>;
}
interface CartEntry {
    item: Item;
    amount: number;
}

export { User, Item, Transaction, TransactionEntry, Cart, CartEntry };