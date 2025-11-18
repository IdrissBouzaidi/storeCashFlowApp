import { Role } from "./role";

export class User {
    
    id?: number;
    username?: string;
    phone?: string;
    firstName?: string;
    lastName?: string;

    constructor(user: Partial<User>) {
        Object.assign(this, user);
    }
}