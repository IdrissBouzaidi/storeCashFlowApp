import { AfterViewInit, Component, Renderer2 } from '@angular/core';
import { RouterModule } from '@angular/router';
import { Toast } from 'primeng/toast';

@Component({
    selector: 'app-root',
    standalone: true,
    imports: [
        RouterModule,
        Toast
    ],
    template:
            `
                <router-outlet></router-outlet>
                <p-toast />
            `
})
export class AppComponent {

    constructor() {}
}
