import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { ButtonModule } from 'primeng/button';
import { RippleModule } from 'primeng/ripple';

@Component({
    selector: 'hero-widget',
    imports: [ButtonModule, RippleModule],
    template: `
        <div
            id="hero"
            class="flex flex-row pt-6 px-6 lg:px-20 overflow-hidden"
            style="padding-bottom: 100px; background: linear-gradient(0deg, rgba(255, 255, 255, 0.2), rgba(255, 255, 255, 0.2)), radial-gradient(77.36% 256.97% at 77.36% 57.52%, rgb(238, 239, 175) 0%, rgb(195, 227, 250) 100%); clip-path: ellipse(150% 87% at 93% 13%)"
        >
            <div class="w-1/2 mx-6 md:mx-20 mt-0 md:mt-6">
                <h1 class="text-5xl font-bold text-gray-900 leading-tight">
                    <span class="font-light block" style="font-size: 20px;">Smart Solution for Your Clothing Business</span>
                    Manage your boutique from A to Z with GestionBoutik
                </h1>
                <p class="font-normal text-2xl leading-normal md:mt-4 text-gray-700">
                    Our all-in-one management app lets you track your sales, cash inflows and outflows, stock, loans, and capital with ease.
                    Automate your calculations, take control of your cash flow, anticipate your needs, and make smart decisions to grow your business.
                </p>
                <button pButton pRipple [rounded]="true" type="button" label="Get Started Now" class="!text-xl mt-8 !px-4" (click)="getStarted()"></button>
            </div>
            <div class="flex justify-center md:justify-end" style=>
                <img src="https://primefaces.org/cdn/templates/sakai/landing/screen-1.png" alt="Hero Image" class="w-9/12 md:w-auto" />
            </div>
        </div>
    `
})
export class HeroWidget {
    constructor(
        private router: Router
    ) {}
    getStarted() {
        this.router.navigateByUrl('/accueil');
    }
}
