# Node.js Message Server

## Prerequesites
-- npm and node js have been installed
-- in [/src](./src) run `npm update` in the console 
#### Server
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
In [client.ts](./src/Client/client.ts#L117) one has to set the url for the websocket connection and how the client will connecto to the message server :
```
// Default location of the Node.js server
const url = 'ws://localhost:8080/'; 
// Default location of the Java EE sync server
//const url = 'ws://localhost:8080/JSP-Message/message'; 
// Default location of the Java EE async server
//const url = 'ws://localhost:8080/JSP-Message/messageasync'; 
// Default location of the Akka server
//const url = 'ws://localhost:8080/greeter'; 
// Default location of the RxKotlin server
const url = 'ws://localhost:8080/chat';


// Node.js connect
client.connect(url, 'echo-protocol', null, { username: username, password: password, create: create });
// Java EE connect
client.connect(url,null,null,{ username: username, password: password, create: create }); 
// Akka, RxKotlin connect
client.connect(url,null,null,{ username: username, password: password, create: create }); 
```
---
## Start the Program
### Server
After executing `npm update` run `npm run server` in the console 
### Client
---
After the specified server is running (Akka,Node.js,RxKotlin,JavaEE ) and setting up the correct connection to the server in [client.ts](./src/Client/client.ts#L117) one can start the server by calling :

``npm run client username password [created]``

- `username` is the username of the user 
- `password` the password of the user
- `created` (optional) if true a new user is registerd database containing the specified credentials else the credentials are used to log the user in 

- e.g. ``npm run client TestClient 12345 true`` would try to register a user "TestClient" with password 12345 in the databaes and log the client in

### Commands
After the user has been sucessfully logged in one can use the flowing textcommands to send Messages to the server:

- Send a message to another user or another group

  Using the command  ``send;[Message];[User|Group]``

  would send a message to a group or an user  

  e.g. ``send;Hello!;User1``
- Create a group

  Using the command  ``create;[Gropname]`` 

  would try to create a group with the name (if the group doesn't already exist)

  e.g. ``create;Testgroup1``
- delete a group

  Using the command  ``remove;[Gropname]``

  would try to delete a group with the name (if the user was the creator of the specifed group)

  e.g. ``remove;Testgroup1``
- add a user to a group

  Using the command  ``add;[User];[Gropname]``

  would sent a group request to the specifed user

  e.g. ``add;TestUser1;Testgroup1``
- accept a group request

  Using the command  ``accept;[Gropname]``

  would accept a received group request from the given group

  e.g. ``accept;Testgroup1``

- decline a group request

  Using the command  ``decline;[Gropname]``

  would decline a received group request from the given group

  e.g. ``decline;Testgroup1`` 

- leave a group

  Using the command  ``leave;[Gropname]``

  would let the user leave a group

  e.g. ``leave;Testgroup1`` 

All of these commands can be used with each of the provided message serves

---
