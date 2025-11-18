import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { ApplicationConfig, LOCALE_ID } from '@angular/core';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { provideRouter, withEnabledBlockingInitialNavigation, withInMemoryScrolling } from '@angular/router';
import { providePrimeNG } from 'primeng/config';
import { appRoutes } from './app.routes';
import { ConfirmationService, MessageService } from 'primeng/api';
import { authInterceptor } from './app/interceptors/auth-interceptor';
import { httpErrorsInterceptor } from './app/interceptors/errors-interceptor';
import { registerLocaleData } from '@angular/common';
import localeFr from '@angular/common/locales/fr';
import Aura from '@primeuix/themes/aura';
import { definePreset } from '@primeng/themes';
import { DatePipe } from '@angular/common';

registerLocaleData(localeFr);

const interceptors = [
    authInterceptor,
    httpErrorsInterceptor
];

const AuraProfessional = definePreset(Aura, {
  semantic: {
    primary: {
      50: '#f4f6f8',
      100: '#e5e9f0',
      200: '#d0d7e2',
      300: '#a9b7c9',
      400: '#7f94a8',
      500: '#4e6e8e',
      600: '#3e5871',
      700: '#2e4154',
      800: '#1f2b37',
      900: '#121a24',
      950: '#0b1015'
    }
  }
});

export const appConfig: ApplicationConfig = {
    providers: [
        MessageService,
        ConfirmationService,
        DatePipe,
        
        provideRouter(appRoutes, withInMemoryScrolling({ anchorScrolling: 'enabled', scrollPositionRestoration: 'enabled' }), withEnabledBlockingInitialNavigation()),
        provideHttpClient(withInterceptors(interceptors)),
        provideAnimationsAsync(),
        { provide: LOCALE_ID, useValue: 'fr-FR' },
        
        providePrimeNG({
            theme: {
                preset: AuraProfessional, options: { darkModeSelector: '.p-dark' }
            },
            translation: {
                accept: 'Oui',
                reject: 'Non',
                startsWith: 'Commence par',
                contains: 'Contient',
                notContains: 'Ne contient pas',
                endsWith: 'Se termine par',
                equals: 'Égal à',
                notEquals: 'Différent de',
                noFilter: 'Aucun filtre',
                lt: 'Inférieur à',
                lte: 'Inférieur ou égal à',
                gt: 'Supérieur à',
                gte: 'Supérieur ou égal à',
                is: 'Est',
                isNot: "N'est pas",
                before: 'Avant',
                after: 'Après',
                clear: 'Effacer',
                apply: 'Appliquer',
                matchAll: 'Correspond à tous',
                matchAny: 'Correspond à un',
                addRule: 'Ajouter une règle',
                removeRule: 'Supprimer la règle',
                dateIs: 'Date est',
                dateIsNot: 'Date n\'est pas',
                dateBefore: 'Date avant',
                dateAfter: 'Date après',

                today: "Aujourd'hui",
                weekHeader: 'Sem',
                firstDayOfWeek: 1,
                dayNames: ['dimanche', 'lundi', 'mardi', 'mercredi', 'jeudi', 'vendredi', 'samedi'],
                dayNamesShort: ['dim.', 'lun.', 'mar.', 'mer.', 'jeu.', 'ven.', 'sam.'],
                dayNamesMin: ['D', 'L', 'M', 'M', 'J', 'V', 'S'],
                monthNames: [
                    'janvier', 'février', 'mars', 'avril', 'mai', 'juin',
                    'juillet', 'août', 'septembre', 'octobre', 'novembre', 'décembre'
                ],
                monthNamesShort: [
                    'janv.', 'févr.', 'mars', 'avr.', 'mai', 'juin',
                    'juil.', 'août', 'sept.', 'oct.', 'nov.', 'déc.'
                ],
                dateFormat: 'dd/mm/yy',
                weak: 'Faible',
                medium: 'Moyen',
                strong: 'Fort',
                passwordPrompt: 'Saisissez un mot de passe',
                emptyMessage: 'Aucun résultat trouvé',
                emptyFilterMessage: 'Aucun résultat trouvé',
                noFileChosenMessage: 'Aucun fichier choisi'
            }
        }),
    ]
};