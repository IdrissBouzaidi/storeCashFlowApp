import { Action } from "./action";

export class Role {
    
    id?: number;
    label?: string;
    code?: string;
    actions: Action[] = [];

    constructor(role: Partial<Role>) {
        Object.assign(this, role);
    }
}