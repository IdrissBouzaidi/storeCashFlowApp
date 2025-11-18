import { HttpInterceptorFn, HttpStatusCode } from '@angular/common/http';
import { inject } from '@angular/core';
import { TokenService } from '../service/token.service';
import { LOGIN_URL } from '../utils/urls/external-urls';
import { catchError, throwError } from 'rxjs';
import { Router } from '@angular/router';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
    const tokenService = inject(TokenService);
    const accessToken = tokenService.getBearerToken();
    const router: Router = inject(Router);

    const unsecuredUrls = [LOGIN_URL];
    const isUnsecured = unsecuredUrls.some(url => req.url.includes(url));
    if (accessToken && !isUnsecured) {

        const headers = req.headers.set('Authorization', 'Bearer ' + accessToken);
        const clonedReq = req.clone({ headers });
        return next(clonedReq).pipe(
            catchError(
                error => {
                    if(error.status === HttpStatusCode.Unauthorized) {
                        router.navigateByUrl('/auth/login');
                    }
                    return throwError(() => error);
                }
            )
        );
    }

    return next(req);
};