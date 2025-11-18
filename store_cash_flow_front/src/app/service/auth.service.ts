import { Injectable, inject } from '@angular/core';
import { map, Observable, of, switchMap, tap } from 'rxjs';
import { TokenService } from './token.service';
import { UserService } from './user.service';
import { LoginService } from './login.service';

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private readonly loginService = inject(LoginService);
  private readonly tokenService = inject(TokenService);
  private readonly userService: UserService = inject(UserService);

  check() {
    return this.tokenService.valid()/* && this.userService.isDataSet()*/;
  }

  login(username: string, password: string, rememberMe = false): Observable<boolean> {
    let loginObservable: Observable<any> = this.loginService.login(username, password, rememberMe).pipe(
        tap(token => {
          this.tokenService.save(token)
        })
      );
    let getUserDetailsObservable: Observable<any> = this.userService.getUserDetailsFromBack();
    let getUserRolesObservable: Observable<any> = this.userService.getUserActionsFromBack();
    return loginObservable.pipe(
      switchMap(() => getUserDetailsObservable),
      switchMap(() => getUserRolesObservable),
      map(() => {
        return this.check();
      })
    );
  }

  refresh() {/*
    return this.loginService
      .refresh(filterObject({ refresh_token: this.tokenService.getRefreshToken() }))
      .pipe(
        catchError(() => of(undefined)),
        tap(token => this.tokenService.set(token)),
        map(() => this.check())
      );*/
  }

  logout() {
    this.tokenService.clear();
    this.userService.clear();
    return of(!this.check() || !this.userService.isDataSet());
  }
}