# Database

Download the [MySQL - Server](https://dev.mysql.com/downloads/mysql/) and extract the windows zip

## Set up the config.ini file
Set up the config ini file to contain the correct path to the server

## Setup the server
1. Modify the [initServer.bat](./initServer.bat) to have the correct path

2. Modify the [startServer.bat](./startServer.bat) to have the correct path

3. Modify the [stopServer.bat](./startServer.bat) to have the correct path


4. run  [initServer.bat](./initServer.bat)

5. run  [startServer.bat](./startServer.bat) to start the mysql server
 
6. Login via mysql as root and create a database e.g. warehouse `CREATE DATABASE warehouse;`
7. Execute the  [init_db](./init_db.sql) sql file 

    `mysql.exe -u root --port 55555 warehouse < ../init_db.sql`
8. Login via mysql to Modify the User Privilages to allow login via local host 
    `ALTER USER 'root'@'localhost' IDENTIFIED WITH mysql_native_password BY '';`

   `flush privileges;`

- use [shutdown.bat](./shutdown.bat) to stop the server



