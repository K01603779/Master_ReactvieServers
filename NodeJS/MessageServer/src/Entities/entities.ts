export enum ContactType {
    User = 0,
    Group
};

export class Contact {
    id: string;
    type: ContactType;
    constructor(id: string) {
        this.id = id;
    }
}

export class User extends Contact {
    password: string;
    type = ContactType.User;
    constructor(id: string, password: string) {
        super(id);
        this.password = password;
    }
}
export class Group extends Contact {
    type = ContactType.Group;
    manager:string
    accepted: string [];
    requested: string [];
    constructor(id: string,manager:string) {
        super(id);
        this.manager=manager;
    }
}

export class GroupEntry {
    groupID: string;
    userID: string;
    accept: boolean;
}