import { Component } from '@angular/core';
import { StyleClassModule } from 'primeng/styleclass';
import { Router, RouterModule } from '@angular/router';
import { RippleModule } from 'primeng/ripple';
import { ButtonModule } from 'primeng/button';
import { UserService } from '../../../service/user.service';
import { TokenService } from '../../../service/token.service';
import { User } from '../../../models/user';

@Component({
    selector: 'topbar-widget',
    imports: [RouterModule, StyleClassModule, ButtonModule, RippleModule],
    styles: `
        .img-container {
            display: flex;
            justify-content: center;

            img {
                width: 100px;
            }
        }
    `,
    templateUrl: 'topbarwidget.component.html'
})
export class TopbarWidget {
    isLogedIn: boolean = false;

    constructor(
        public router: Router,
    ) {        
    }
}
