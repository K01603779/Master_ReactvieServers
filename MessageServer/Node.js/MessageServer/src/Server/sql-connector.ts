import { Group, User, ContactType, Contact } from "../Entities/entities";
import { Message, MsgType, RemoveGroup, AcceptRequest, CreateGroup, DeclineRequest, InviteToGroup, LeaveGroup, PrivateMessage } from "../Messages/messages";

var mysql = require('mysql');
var pool = mysql.createPool({
    connectionLimit: 10,
    host: 'localhost',
    port: '55555',
    user: 'root',
    password: '',
    database: 'warehouse'
});

export async function getUser(username: string, pwd: string): Promise<User> {
    return new Promise<User>((resolve, reject) => {
        pool.query(`select * from respondent where respondentID =? and content =? and type = ${ContactType.User}`, [username, pwd], function (error, results, fields) {
            if (error) {
                reject(null);
            }
            else {
                console.log(results[0]);
                if (results[0] == undefined) { reject(null) } else {
                    var user: User = new User(results[0].respondentID, results[0].content);
                    resolve(user);
                }
            }
        });
    });
}
export async function getUserFromDB(username: string): Promise<User> {
    return new Promise<User>((resolve, reject) => {
        pool.query(`select * from respondent where respondentID =? and type = ${ContactType.User}`, [username], function (error, results, fields) {
            if (error) { reject(null); }
            else {
                console.log(results[0]);
                if (results[0] == undefined) { reject(null) } else {
                    var user: User = new User(results[0].respondentID, results[0].content);
                    resolve(user);
                }
            }
        });
    });
}
export async function getContact(id: string): Promise<Contact> {
    return new Promise<Contact>((resolve, reject) => {
        pool.query(`select * from respondent where respondentID =?`, [id], function (error, results, fields) {
            if (error) { reject(null); }
            else {
                console.log(results[0]);
                if (results[0] == undefined) { reject(null) } else {
                    if (results[0].type == ContactType.User) {
                        resolve(new User(results[0].respondentID, results[0].content));
                    } else {
                        var group: Group = new Group(results[0].respondentID, results[0].content);
                        getUserEntrysFromGroup(id).then(array => {
                            group.accepted = array.accepted;
                            group.requested = array.request;
                            console.log(`Received Group ${group}`);
                            resolve(group);
                        }).catch(error => {
                            resolve(group);
                        })
                    }
                }
            }
        });
    });
}

export async function createUser(username: string, password: string): Promise<User> {
    return new Promise<User>((resolve, reject) => {
        pool.query('INSERT INTO respondent (respondentID, content, type) VALUES (?,?,?)', [username, password, ContactType.User], function (error, results, fields) {
            if (error) {
                console.log(error);
                reject(null);
            } else {
                console.log(results);
                var regUser: User = new User(username, password);
                resolve(regUser);
            }
        });
    });
}

export async function getGroup(groupname: string): Promise<Group> {
    return new Promise<Group>((resolve, reject) => {
        pool.query(`select * from respondent where respondentID =? and type = ${ContactType.Group}`, [groupname], function (error, results, fields) {
            if (error) { reject(null); }
            else {
                console.log(results[0]);
                if (results[0] == undefined) { resolve(null) } else {
                    var group: Group = new Group(results[0].respondentID, results[0].content);
                    getUserEntrysFromGroup(groupname).then(array => {
                        group.accepted = array.accepted;
                        group.requested = array.request;
                        console.log(`Received Group ${group}`);
                        resolve(group);
                    }).catch(error => {
                        resolve(group);
                    })

                }
            }
        });
    });
}

export async function createGroup(groupname: string, user: string): Promise<Group> {
    return new Promise<Group>((resolve, reject) => {
        pool.query('INSERT INTO respondent (respondentID, content, type) VALUES (?,?,?)', [groupname, user, ContactType.Group], function (error, results, fields) {
            if (error) {
                console.log(error);
                reject(null);
            } else {
                console.log(results);
                var group: Group = new Group(groupname, user);
                resolve(group);
            }
        });
    });
}

export async function removeGroupEntity(groupname: string, username: string) {
    pool.query('delete from groupEntry where userID = ? and groupID = ?', [username, groupname], async function (error, results, fields) {
        if (error) { console.log("GroupEntity error"); }
    });
}

export async function deleteEntity(name: string) {
    pool.query('delete from respondent where respondentID = ?', [name], async function (error, results, fields) {
        if (error) { console.log("GroupEntity error"); }
    });
}

export async function updateGroupEntiry(groupname: string, username: string, accepted: boolean) {
    return new Promise((result, reject) => {
        pool.query('replace  into groupEntry (groupID, userID, accepted) values (?,?,?)', [groupname, username, accepted], async function (error, results, fields) {
            if (error) {
                console.log(error);
                reject(false);
            } else {
                result(true);
            }
        });
    });
}

export async function getUserEntrysFromGroup(groupname: string): Promise<{ accepted: string[], request: string[] }> {
    return new Promise<{ accepted: string[], request: string[] }>((result, reject) => {
        pool.query('select * from groupEntry where groupID = ?', [groupname], async function (error, results, fields) {
            if (error) {
                reject(null);
            } else {
                var accepted = [];
                var request = [];
                results.forEach(element => {
                    if (element.accepted) {
                        accepted.push(element.userID);
                    } else {
                        request.push(element.userID);
                    }
                });
                result({ accepted, request });
            }
        });
    });
}

export async function storeUserMsg(message: Message) {
    return new Promise<Message>((resolve, reject) => {
        pool.query('INSERT INTO message (senderID, receiverID, content,type) VALUES (?,?,?,?)', [message.senderID, message.receiverID, message.content, message.type], function (error, results, fields) {
            if (error) {
                console.log(error);
                reject(null);
            } else {
                resolve(message);
            }
        });
    });
}

export async function getMessageOfUser(username: string) {
    return new Promise<Array<Message>>((resolve, reject) => {
        pool.query('select * from message where receiverID = ?', [username], function (error, results, fields) {
            if (error) {
                console.log(error);
                reject(null);
            } else {
                var messages = []
                results.forEach(element => {
                    console.log("SQL-Messages " + element);
                    pool.query('delete from message where messageID = ?', [element.messageID], async function (error, results, fields) {
                        if (error) { reject([]); }
                    });
                    messages.push(createMessage(element.senderID, element.receiverID, element.content, element.type));
                });
                resolve(messages);
            }
        });
    });
}

function createMessage(senderID: string, receiverID: string, content: string, type: number): Message {
    switch (type) {
        case MsgType.InviteToGroup:
            return new InviteToGroup(senderID, receiverID, content);
        case MsgType.CreateGroup:
            return new CreateGroup(senderID, content);
        case MsgType.AcceptRequest:
            return new AcceptRequest(senderID, content);
        case MsgType.DeclineRequest:
            return new DeclineRequest(senderID, content);
        case MsgType.LeaveGroup:
            return new LeaveGroup(senderID, content);
        case MsgType.Private:
            return new PrivateMessage(senderID, receiverID, content);
        case MsgType.RemoveGroup:
            return new RemoveGroup(senderID, content);
    }
}

export async function retryWithOne(call: any, param: any) {
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
export async function retryWithThree(call: any, param1: any, param2: any, param3: any) {
    return call(param1, param2, param3)
        .catch(() => {
            return call(param1, param2, param3);
        })
        .catch(() => {
            return call(param1, param2, param3);
        }).catch(() => {
            return call(param1, param2, param3);
        });
}

function tryPromise(ret: boolean){
    var promise = new Promise<String>((resolve, reject) => {;
        if (ret) {
                resolve("success");
            }
            else {
                reject(null);
            }
        });
    promise.then(value => {
        console.log(`Returned Value ${value}`)
    }).catch(error => {
        console.log("Promise rejected");
        
    });
}