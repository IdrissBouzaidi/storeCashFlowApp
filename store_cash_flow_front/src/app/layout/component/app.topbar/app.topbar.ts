import { Component } from '@angular/core';
import { MenuItem } from 'primeng/api';
import { Router, RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';
import { StyleClassModule } from 'primeng/styleclass';
import { AppConfigurator } from '../app.configurator';
import { LayoutService } from '../../service/layout.service';
import { AuthService } from '../../../service/auth.service';
import { User } from '../../../models/user';
import { UserService } from '../../../service/user.service';
import { Role } from '../../../models/role';
import { APPLICATION_NAME } from '../../../utils/consts/consts';

@Component({
    selector: 'app-topbar',
    standalone: true,
    imports: [RouterModule, CommonModule, StyleClassModule, AppConfigurator],
    templateUrl: `app.topbar.html`,
    styleUrl: 'app.topbar.scss'
})
export class AppTopbar {
    items!: MenuItem[];
    userDetails?: User;
    userRoles: Role[] = [];
    applicationName: string = APPLICATION_NAME;

    constructor(
        public layoutService: LayoutService,
        private authServive: AuthService,
        private userService: UserService,
        private router: Router
    ) {
        this.userDetails = this.userService.getUser();
        this.userRoles = this.userService.getRoles()?? [];
    }

    toggleDarkMode() {
        this.layoutService.layoutConfig.update((state) => ({ ...state, darkTheme: !state.darkTheme }));
    }

    logOut() {
        this.authServive.logout();
        this.router.navigateByUrl('/auth/login');
    }
}
