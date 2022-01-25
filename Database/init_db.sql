# Steps :
# 1.) Download mysql server
# 2.) setup config and .bat-files correctly 
# 3.) run init.bat
# 4.) run start.bat
# 5.) Login via mysql as root and create a database 
# 6.) Execute this sql file 
# 7.)   Login via mysql:
#    ALTER USER 'root'@'localhost' IDENTIFIED WITH mysql_native_password BY '';
#    flush privileges;
# 8.) 
# mysql.exe -u root --port 55555 -p
# CREATE DATABASE warehouse;
# mysql.exe -u root --port 55555 warehouse < ../init_db.sql
# 

# Creates the Webshop Database

# drop table items;

CREATE TABLE items ( ItemID int PRIMARY KEY AUTO_INCREMENT, itemName varchar(255), itemdesc TEXT(5000), price DOUBLE(10, 3));
INSERT INTO items (itemName,itemdesc , price) VALUES ("Apple", "A box of apples ...", 3.99);
INSERT INTO items (itemName,itemdesc , price) VALUES ("Oranges", "Can be compared with apples ", 4.99);
INSERT INTO items (itemName,itemdesc , price) VALUES ("Bananas", "Box of Bananas ", 10.53);
INSERT INTO items (itemName,itemdesc , price) VALUES ("Pinapples", "Box of Pinapples ", 2.99);
INSERT INTO items (itemName,itemdesc , price) VALUES ("Raspberries", "Box of Raspberries ", 2.43);

# drop table users;
CREATE TABLE users ( userID int PRIMARY KEY AUTO_INCREMENT, firstName varchar(50), lastName varchar(50), password varchar(50), email varchar(50) UNIQUE, address varchar(50), creditCard varchar(50));
INSERT INTO users (firstName, lastName, password, email, address,creditCard) VALUES ( "Max" , "Mustermann", "12345","max.mustermann@jku.at","Musterstraße 14 ","32344-53434-53443");

# drop table carts;
create table carts (cartID int PRIMARY KEY AUTO_INCREMENT, userID int);
# drop table cartEntries;
create table cartEntries (cartID int NOT NULL, itemID int NOT NULL, amount int, CONSTRAINT PK_CartEntries PRIMARY KEY(cartID,itemID));
# drop table transactions;
create table transactions (transactionID int PRIMARY KEY AUTO_INCREMENT, userID int, orderDate date);
# drop table transactionEntries;


# Creates the MessageServer Database

# drop table message;
create table message (messageID int PRIMARY KEY AUTO_INCREMENT, senderID  varchar(200), receiverID varchar(200), content varchar(500),type int);
# drop table respondent;
create table respondent (respondentID varchar(200) PRIMARY KEY, content varchar(200), type int);
# drop table groupEntry;
create table groupEntry (userID varchar(200), groupID varchar(200), accepted bool, CONSTRAINT PK_GroupEntry PRIMARY KEY(userID,groupID));
insert into respondent (respondentID,content,type) VALUES ('TestUser','12345',0); 

