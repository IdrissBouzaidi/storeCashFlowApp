import { Routes } from '@angular/router';
import { LandingComponent } from './app/pages/landing/landing.component';
import { LoginComponent } from './app/pages/login/login.component';
import { authGuard, viewAdvanceGuard, viewAllOperationsGuard, viewCapitalContributionGuard, viewChargeGuard, viewConsInputsGuard, viewCustomerCreditGuard, viewExternalLoanGuard, viewNotConsInputGuard, viewOutOfPocketExpenseGuard, viewOutputGuard } from './app/utils/guards';
import { AppLayout } from './app/layout/component/app.layout';
import { CATEGORIES_INTERNAL_URL, HOME_INTERNAL_URL, OPERATIONS_INTERNAL_URL, PERIODS_INTERNAL_URL, PRODUCTS_INTERNAL_URL, REFERENCES_INTERNAL_URL, VIEW_ADVANCE_INTERNAL_URL, VIEW_ALL_OPERATIONS_INTERNAL_URL, VIEW_CAPITAL_CONTRIBUTION_INTERNAL_URL, VIEW_CHARGE_INTERNAL_URL, VIEW_CONS_INPUTS_INTERNAL_URL, VIEW_CUSTOMER_CREDIT_INTERNAL_URL, VIEW_EXTERNAL_LOAN_INTERNAL_URL, VIEW_NOT_CONS_INPUT_INTERNAL_URL, VIEW_OUT_OF_POCKET_EXPENSE_INTERNAL_URL, VIEW_OUTPUT_INTERNAL_URL } from './app/utils/urls/internal-urls';
import { HomeComponent } from './app/pages/home/home.component';
import { ConsInputComponent } from './app/pages/financial-operations/cons-input/cons-input.component';
import { OutputComponent } from './app/pages/financial-operations/output/output.component';
import { ChargeComponent } from './app/pages/financial-operations/charge/charge.component';
import { CapitalContributionComponent } from './app/pages/financial-operations/capital-contribution/capital-contribution.component';
import { NotConsInputComponent } from './app/pages/financial-operations/not-cons-input/not-cons-input.component';
import { AdvanceComponent } from './app/pages/financial-operations/advance/advance.component';
import { OutOfPocketExpenseComponent } from './app/pages/financial-operations/out-of-pocket-expense/out-of-pocket-expense.component';
import { CustomerCreditComponent } from './app/pages/financial-operations/customer-credit/customer-credit.component';
import { ExternalLoanComponent } from './app/pages/financial-operations/external-loan/external-loan.component';
import { AllOperationsComponent } from './app/pages/financial-operations/all-operations/all-operations.component';
import { PeriodsComponent } from './app/pages/periods/periods.component';
import { ProductsComponent } from './app/pages/references/products/products.component';

export const appRoutes: Routes = [
    { path: '', component: LandingComponent },
    { path: 'auth/login', component: LoginComponent },
    {
        path: '',
        component: AppLayout,
        canActivate: [authGuard],
        canActivateChild: [authGuard],
        children: [
            { path: HOME_INTERNAL_URL, data: { breadcrumb: 'Charts' }, component: HomeComponent, canActivate: [authGuard] },
            {
                path: OPERATIONS_INTERNAL_URL,
                canActivate: [authGuard],
                canActivateChild: [authGuard],
                children: [
                    { path: VIEW_ALL_OPERATIONS_INTERNAL_URL, component: AllOperationsComponent, canActivate: [viewAllOperationsGuard] },
                    { path: VIEW_CONS_INPUTS_INTERNAL_URL, component: ConsInputComponent, canActivate: [viewConsInputsGuard] },
                    { path: VIEW_NOT_CONS_INPUT_INTERNAL_URL, component: NotConsInputComponent, canActivate: [viewNotConsInputGuard] },
                    { path: VIEW_OUTPUT_INTERNAL_URL, component: OutputComponent, canActivate: [viewOutputGuard] },
                    { path: VIEW_CHARGE_INTERNAL_URL, component: ChargeComponent, canActivate: [viewChargeGuard] },
                    { path: VIEW_CAPITAL_CONTRIBUTION_INTERNAL_URL, component: CapitalContributionComponent, canActivate: [viewCapitalContributionGuard] },
                    { path: VIEW_ADVANCE_INTERNAL_URL, component: AdvanceComponent, canActivate: [viewAdvanceGuard] },
                    { path: VIEW_OUT_OF_POCKET_EXPENSE_INTERNAL_URL, component: OutOfPocketExpenseComponent, canActivate: [viewOutOfPocketExpenseGuard] },
                    { path: VIEW_CUSTOMER_CREDIT_INTERNAL_URL, component: CustomerCreditComponent, canActivate: [viewCustomerCreditGuard] },
                    { path: VIEW_EXTERNAL_LOAN_INTERNAL_URL, component: ExternalLoanComponent, canActivate: [viewExternalLoanGuard] }
                ]
            },
            { path: PERIODS_INTERNAL_URL, component: PeriodsComponent, canActivate: [] },
            {
                path: REFERENCES_INTERNAL_URL,
                canActivate: [authGuard],
                canActivateChild: [authGuard],
                children: [
                    { path: PRODUCTS_INTERNAL_URL, component: ProductsComponent, canActivate: [] },
                    { path: CATEGORIES_INTERNAL_URL, component: HomeComponent, canActivate: [] }

                ]
            }
        ]
    },
    { path: '**', redirectTo: HOME_INTERNAL_URL }
];