import { Injectable } from '@angular/core';
import { Token } from '../models/token';

@Injectable({
    providedIn: 'root',
})
export class TokenService {
    private readonly key = 'token-data';

    private token?: Token;
    
    constructor() {
        this.getInfosFromLocalStorage();
    }

    getInfosFromLocalStorage() {
        let tokenStringified: string | null = localStorage.getItem(this.key);
        let token: Token | undefined;
        if(tokenStringified) {
            try {
                token = JSON.parse(tokenStringified);
                this.save(token);
            }
            catch(e) {

            }
        }
    }

    getBearerToken() {
        return this.token?.access_token;
    }

    clear() {
        localStorage.removeItem(this.key);
        this.token = {};
    }

    save(token?: Token) {
        this.token = token;
        localStorage.setItem(this.key, JSON.stringify(token));
    }

    valid(): boolean {
        return this.token?.access_token? true: false;
    }

}
