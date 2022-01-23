# Node.js WebShop

## Prerequesites
- npm and node js have been installed
- run `npm update` in the console 
#### Server
In [server.ts](./src/Server/server.ts#L13) on can change the port of the server.

In [sql-connector.ts](./src/Server/sql-connector.ts#L5) one has to set the credentials for the mysql database:
```
var mysql = require('mysql');
var pool = mysql.createPool({
    connectionLimit: 10,
    host: 'localhost',
    port: '55555',
    user: 'root',
    password: '',
    database: 'warehouse'
});
```
#### Client
In [client.ts](./src/Client/client.ts#L77) one has to set the url for the websocket connection

---

## Start the Program
### Server
After executing `npm update` run `npm run server` in the console 

### Client
This client currently only supports a connection to the Node.js Webshop
In [client.ts](./src/Client/client.ts) the following methods allows the client to communicate with the server:
 - `sendLogin` allows the client to send a login Request
 - `sendLogout` sends a logout request
 - `sendRegisterUser`sends a user registration request
 - `sendSearchItem` client sends a request to the server to search the database for the items with the given name
 - `sendGetTransactions` sends a request to retrieve all the previous performed purchases
 - `sendModifyCart` sends a cart modifiaction request (add or remove items from the cart)
 - `sendCheckOut` send a checkout request to initate the checkout 
---