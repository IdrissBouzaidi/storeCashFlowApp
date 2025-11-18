import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { ButtonModule } from 'primeng/button';
import { CheckboxModule } from 'primeng/checkbox';
import { InputTextModule } from 'primeng/inputtext';
import { PasswordModule } from 'primeng/password';
import {  RippleModule } from 'primeng/ripple';
import { AppFloatingConfigurator } from '../../layout/component/app.floatingconfigurator';
import { AuthService } from '../../service/auth.service';
import { HttpErrorResponse, HttpStatusCode } from '@angular/common/http';
import { MessageService } from 'primeng/api';
import { Toast } from 'primeng/toast';
import { CommonModule } from '@angular/common';
import { HOME_INTERNAL_URL } from '../../utils/urls/internal-urls';

@Component({
    selector: 'app-login',
    standalone: true,
    imports: [
        CommonModule,
        ButtonModule,
        CheckboxModule,
        InputTextModule,
        PasswordModule,
        FormsModule,
        RouterModule,
        RippleModule,
        AppFloatingConfigurator,
        Toast
    ],
    templateUrl: 'login.component.html',
    styleUrl: 'login.component.scss'
})
export class LoginComponent implements OnInit {
    login: string = '';
    loginCaseIsValid: boolean = true;

    password: string = '';
    passwordCaseIsValid: boolean = true;

    checked: boolean = false;

    isSubmitting: boolean = false;

    constructor(
        private authService: AuthService,
        private router: Router,
        private messageService: MessageService
    ) {
    }

    ngOnInit(): void {
        this.authService.logout().subscribe();
    }

    logIn() {

        if(!this.login) {
            this.loginCaseIsValid = false;
            return;
        }

        else if(!this.password) {
            this.passwordCaseIsValid = false;
            return;
        }

        else {
            this.isSubmitting = true;
            this.authService
            .login(this.login, this.password, this.checked)
            //.pipe(filter(authenticated => authenticated))
            .subscribe({
                next: () => {
                    this.messageService.add({severity: 'success', summary: 'Connexion', detail: 'Authentification avec succès'});
                    this.isSubmitting = false;
                    this.router.navigateByUrl(HOME_INTERNAL_URL);
                },
                error: (errorRes: HttpErrorResponse) => {
                    if (errorRes.status === HttpStatusCode.Unauthorized) {
                        this.messageService.add({severity: 'error', summary: 'Connexion', detail: "Le login ou le mot de passe est incorrect"});
                    }
                    this.isSubmitting = false;
                },
            });
        }
    }

    onLoginChange() {
        this.loginCaseIsValid = true;
    }

    onPasswordChange() {
        this.passwordCaseIsValid = true;
    }
}
