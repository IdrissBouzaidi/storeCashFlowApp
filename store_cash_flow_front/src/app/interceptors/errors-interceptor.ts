import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { HttpStatusCode } from '@angular/common/http';
import { catchError, tap } from 'rxjs/operators';
import { throwError } from 'rxjs';
import { MessageService } from 'primeng/api'; // si tu veux afficher des messages
import { LOGIN_URL } from '../utils/urls/external-urls';

export const httpErrorsInterceptor: HttpInterceptorFn = (req, next) => {
    const messageService = inject(MessageService);
    const router = inject(Router);
    const currentUrl = router.url;

    return next(req).pipe(
        tap(
            (data: any) => {
                if(data.status === HttpStatusCode.Ok) {
                    if(data.body?.status === 'ERROR') {
                        throw new Error(data);
                    }
                }
            }
        ),
        catchError(error => {
            if(error.status === HttpStatusCode.InternalServerError) {
                let errorDetailsFromService: string = error?.error?.errorDetails;
                let errorMessage: string = 'Un problème est survenu, réessayez à nouveau.';
                if(errorDetailsFromService && errorDetailsFromService.includes('lireImpayes') && errorDetailsFromService.includes('inexistant')) {
                    errorMessage = "La valeur insérée n'est pas correcte.";
                }
                messageService.add({severity: 'error', summary: 'Erreur serveur', detail: errorMessage});
            }
            else if(error.status === HttpStatusCode.BadRequest) {
                messageService.add({severity: 'error', summary: 'Erreur', detail: error.error.error});
            }
            else if(error.status === HttpStatusCode.Conflict) {
                messageService.add({severity: 'error', summary: 'Erreur', detail: error.error.error});
            }
            else if(error.status === HttpStatusCode.Unauthorized && !currentUrl.includes('/auth/login')) {
                messageService.add({severity: 'warn', summary: 'Déconnexion', detail: 'Veillez vous connecter à nouveau'});
            }
            else if (error.status === 0) {
                    messageService.add({
                    severity: 'error',
                    summary: 'Serveur injoignable',
                    detail: 'Veuillez vérifier votre connexion ou réessayer plus tard.',
                });
            }
            else if(!currentUrl.includes('/auth/login')) {
                messageService.add({
                    severity: 'error',
                    summary: 'Erreur serveur',
                    detail: 'Un problème est survenu, réessayez à nouveau.',
                });
            }
            return throwError(() => error);
        })
    );
};
