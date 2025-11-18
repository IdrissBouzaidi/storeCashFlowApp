export class Token {
    [prop: string]: any;
  
    access_token?: string;
    expires_in?: number;
    refresh_expires_in?: number;
    refresh_token?: string;
    token_type?: string;
    exp?: number;

    constructor(token: Partial<Token>) {
        Object.assign(this, token);
    }
}