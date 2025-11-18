export class Action {

    id?: number;
    label?: string;
    code?: string;
  
    constructor(action: Partial<Action>) {
        Object.assign(this, action);
    }
}