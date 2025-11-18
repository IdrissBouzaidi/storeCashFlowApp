import { inject, Injectable } from "@angular/core";
import { HttpClient, HttpHeaders } from "@angular/common/http";
import { map, Observable, of, tap } from "rxjs";
import { User } from "../models/user";
import { Role } from "../models/role";
import { Action } from "../models/action";
import { GET_USER_ACTIONS_URL, GET_USER_INFORMATIONS_URL } from "../utils/urls/external-urls";

@Injectable({providedIn: 'root'})
export class UserService {
    private http: HttpClient = inject(HttpClient);

    private readonly USER_DATA_KEY: string = 'user-data';
    private readonly ROLES_DATA_KEY: string = 'roles-data';

    private user?: User;
    private roles: Role[] = [];
    private actions: Action[] = [];

    constructor() {
        this.getUserDetailsFromLocalStorage();
        this.getUserRolesFromLocalStorage();
    }

    getUserDetailsFromBack(): Observable<any> {
        let httpObservable: Observable<any> = this.http.get<any>(GET_USER_INFORMATIONS_URL);
        return httpObservable.pipe(
            tap(
                (user: User) => {
                    localStorage.setItem(this.USER_DATA_KEY, JSON.stringify(user));
                    this.getUserDetailsFromLocalStorage();
                }
            )
        );
    }

    getUserActionsFromBack(): Observable<any> {
        let httpObservable: Observable<any> = this.http.get<any>(GET_USER_ACTIONS_URL);
        return httpObservable.pipe(
            tap(
                (roles: Role[]) => {
                    localStorage.setItem(this.ROLES_DATA_KEY, JSON.stringify(roles));
                    this.getUserRolesFromLocalStorage();
                }
            )
        );
    }

    getUser(): User | undefined {
        return this.user;
    }

    getRoles(): Role[] | undefined {
        return this.roles;
    }

    getActions(): Action[] | undefined {
        return this.actions;
    }

    getUserDetailsFromLocalStorage() {
        let userDetailsStringified: string | null = localStorage.getItem(this.USER_DATA_KEY);
        if(userDetailsStringified) {
            this.user = JSON.parse(userDetailsStringified);
        }
    }

    getUserRolesFromLocalStorage() {
        let userRolesStringified: string | null = localStorage.getItem(this.ROLES_DATA_KEY);
        if(userRolesStringified) {
            this.roles = JSON.parse(userRolesStringified);
            if(this.roles) {
                let actionsObj: any = {};
                for(let role of this.roles) {
                    for(let action of role?.actions) {
                        if(action.id)
                            actionsObj[action.id] = action;
                    }
                }
                this.actions = Object.values(actionsObj);
            }
        }
    }

    isDataSet(): boolean {
        return this.user != undefined && this.roles.length>0;
    }

    clear() {
        localStorage.removeItem(this.USER_DATA_KEY);
        this.getUserDetailsFromLocalStorage();
        this.getUserRolesFromLocalStorage();
    }
}