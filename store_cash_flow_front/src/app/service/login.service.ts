import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { map, Observable, of } from 'rxjs';
import { Token } from '../models/token';
import { LOGIN_URL } from '../utils/urls/external-urls';
import { User } from '../models/user';

@Injectable({
  providedIn: 'root',
})
export class LoginService {
  protected readonly http = inject(HttpClient);

  login(login: string, password: string, rememberMe = false): Observable<Token> {
    let httpObservable: Observable<any> = this.http.post<any>(LOGIN_URL, { login, password });
    return httpObservable;
  }

  refresh(params: Record<string, any>) {
    return this.http.post<Token>('/auth/refresh', params);
  }

  user() {
    const user: User = new User({})
    return of(user);
  }
}
