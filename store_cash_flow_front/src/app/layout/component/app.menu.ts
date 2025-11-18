import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { MenuItem } from 'primeng/api';
import { AppMenuitem } from './app.menuitem';
import { UserService } from '../../service/user.service';
import { VIEW_ADVANCE_ACTION, VIEW_ALL_OPERATIONS_ACTION, VIEW_CAPITAL_CONTRIBUTION_ACTION, VIEW_CHARGE_ACTION, VIEW_CONS_INPUTS_ACTION, VIEW_CUSTOMER_CREDIT_ACTION, VIEW_EXTERNAL_LOAN_ACTION, VIEW_NOT_CONS_INPUT_ACTION, VIEW_OUT_OF_POCKET_EXPENSE_ACTION, VIEW_OUTPUT_ACTION } from '../../utils/consts/actions-consts';
import { CATEGORIES_INTERNAL_URL, HOME_INTERNAL_URL, OPERATIONS_INTERNAL_URL, PERIODS_INTERNAL_URL, PRODUCTS_INTERNAL_URL, REFERENCES_INTERNAL_URL, VIEW_ADVANCE_INTERNAL_URL, VIEW_ALL_OPERATIONS_INTERNAL_URL, VIEW_CAPITAL_CONTRIBUTION_INTERNAL_URL, VIEW_CHARGE_INTERNAL_URL, VIEW_CONS_INPUTS_INTERNAL_URL, VIEW_CUSTOMER_CREDIT_INTERNAL_URL, VIEW_EXTERNAL_LOAN_INTERNAL_URL, VIEW_NOT_CONS_INPUT_INTERNAL_URL, VIEW_OUT_OF_POCKET_EXPENSE_INTERNAL_URL, VIEW_OUTPUT_INTERNAL_URL } from '../../utils/urls/internal-urls';
import { Action } from '../../models/action';

@Component({
    selector: 'app-menu',
    standalone: true,
    imports: [CommonModule, AppMenuitem, RouterModule],
    template: `<ul class="layout-menu">
        <ng-container *ngFor="let item of model; let i = index">
            <li app-menuitem *ngIf="!item.separator" [item]="item" [index]="i" [root]="true"></li>
            <li *ngIf="item.separator" class="menu-separator"></li>
        </ng-container>
    </ul> `
})
export class AppMenu {
    model: MenuItem[] = [];

    hasViewAllOperationsAction: boolean = false;
    hasViewConsInputsAction: boolean = false;
    hasViewNotConsInputAction: boolean = false;
    hasViewOutputAction: boolean = false;
    hasViewChargeAction: boolean = false;
    hasViewCapitalContributionAction: boolean = false;
    hasViewAdvanceAction: boolean = false;
    hasViewOutOfPocketExpenseAction: boolean = false;
    hasViewCustomerCreditAction: boolean = false;
    hasViewExternalLoanAction: boolean = false;

    constructor(
        private userService: UserService
    ) {
        this.getUserActions();
    }

    getUserActions() {
        const userActions: Action[] | undefined = this.userService.getActions();
        this.hasViewAllOperationsAction = userActions?.some(action => action.id === VIEW_ALL_OPERATIONS_ACTION)?? false;
        this.hasViewConsInputsAction = userActions?.some(action => action.id === VIEW_CONS_INPUTS_ACTION)?? false;
        this.hasViewNotConsInputAction = userActions?.some(action => action.id === VIEW_NOT_CONS_INPUT_ACTION)?? false;
        this.hasViewOutputAction = userActions?.some(action => action.id === VIEW_OUTPUT_ACTION)?? false;
        this.hasViewChargeAction = userActions?.some(action => action.id === VIEW_CHARGE_ACTION)?? false;
        this.hasViewCapitalContributionAction = userActions?.some(action => action.id === VIEW_CAPITAL_CONTRIBUTION_ACTION)?? false;
        this.hasViewAdvanceAction = userActions?.some(action => action.id === VIEW_ADVANCE_ACTION)?? false;
        this.hasViewOutOfPocketExpenseAction = userActions?.some(action => action.id === VIEW_OUT_OF_POCKET_EXPENSE_ACTION)?? false;
        this.hasViewCustomerCreditAction = userActions?.some(action => action.id === VIEW_CUSTOMER_CREDIT_ACTION)?? false;
        this.hasViewExternalLoanAction = userActions?.some(action => action.id === VIEW_EXTERNAL_LOAN_ACTION)?? false;
    }

    ngOnInit() {
        this.model = [
            {
                id: 'home',
                label: 'Home',
                items: [
                    { label: 'Dashboard', icon: 'pi pi-chart-bar', routerLink: [HOME_INTERNAL_URL] },
                    { label: 'Reports', icon: 'pi pi-chart-line', routerLink: [HOME_INTERNAL_URL] }
                ]
            },
            {
                id: 'operations',
                label: 'Operations',
                items: []
            },
            {
                id: 'periods',
                label: 'Periods',
                items: [
                    { label: 'Periods', icon: 'pi pi-clock', routerLink: [PERIODS_INTERNAL_URL] },
                ]
            },
            {
                id: 'references',
                label: 'References',
                items: [
                    { label: 'Products', icon: 'pi pi-box', routerLink: [REFERENCES_INTERNAL_URL, PRODUCTS_INTERNAL_URL] },
                    { label: 'Categories', icon: 'pi pi-tags', routerLink: [REFERENCES_INTERNAL_URL, CATEGORIES_INTERNAL_URL] }
                ]
            }
        ];

        const operationsItem: MenuItem = this.model.find(item => item.id === 'operations')!;
        if(this.hasViewAllOperationsAction) {
            const viewAllOperationsSubAction: MenuItem = { label: 'All operations', icon: 'pi pi-', routerLink: [OPERATIONS_INTERNAL_URL, VIEW_ALL_OPERATIONS_INTERNAL_URL] };
            operationsItem.items!.push(viewAllOperationsSubAction);
        }
        if(this.hasViewConsInputsAction) {
            const viewConsInputSubAction: MenuItem = { label: 'Consumable inputs', icon: 'pi pi-', routerLink: [OPERATIONS_INTERNAL_URL, VIEW_CONS_INPUTS_INTERNAL_URL] };
            operationsItem.items!.push(viewConsInputSubAction);
        }
        if(this.hasViewNotConsInputAction) {
            const viewNotConsInputSubAction: MenuItem = { label: 'Not consumable inputs', icon: 'pi pi-', routerLink: [OPERATIONS_INTERNAL_URL, VIEW_NOT_CONS_INPUT_INTERNAL_URL] };
            operationsItem.items!.push(viewNotConsInputSubAction);
        }
        if(this.hasViewOutputAction) {
            const viewOutputSubAction: MenuItem = { label: 'Outputs', icon: 'pi pi-', routerLink: [OPERATIONS_INTERNAL_URL, VIEW_OUTPUT_INTERNAL_URL] };
            operationsItem.items!.push(viewOutputSubAction);
        }
        if(this.hasViewChargeAction) {
            const viewChargeSubAction: MenuItem = { label: 'Charge', icon: 'pi pi-', routerLink: [OPERATIONS_INTERNAL_URL, VIEW_CHARGE_INTERNAL_URL] };
            operationsItem.items!.push(viewChargeSubAction);
        }
        if(this.hasViewCapitalContributionAction) {
            const viewCapitalContributionSubAction: MenuItem = { label: 'Capital contribution', icon: 'pi pi-', routerLink: [OPERATIONS_INTERNAL_URL, VIEW_CAPITAL_CONTRIBUTION_INTERNAL_URL] };
            operationsItem.items!.push(viewCapitalContributionSubAction);
        }
        if(this.hasViewAdvanceAction) {
            const viewAdvanceSubAction: MenuItem = { label: 'Advance', icon: 'pi pi-', routerLink: [OPERATIONS_INTERNAL_URL, VIEW_ADVANCE_INTERNAL_URL] };
            operationsItem.items!.push(viewAdvanceSubAction);
        }
        if(this.hasViewOutOfPocketExpenseAction) {
            const viewOutOfPocketSubAction: MenuItem = { label: 'Out of pocket expense', icon: 'pi pi-', routerLink: [OPERATIONS_INTERNAL_URL, VIEW_OUT_OF_POCKET_EXPENSE_INTERNAL_URL] };
            operationsItem.items!.push(viewOutOfPocketSubAction);
        }
        if(this.hasViewCustomerCreditAction) {
            const viewCostumerCreditAction: MenuItem = { label: 'Customer credit', icon: 'pi pi-', routerLink: [OPERATIONS_INTERNAL_URL, VIEW_CUSTOMER_CREDIT_INTERNAL_URL] };
            operationsItem.items!.push(viewCostumerCreditAction);
        }
        if(this.hasViewExternalLoanAction) {
            const viewExternalLoadAction: MenuItem = { label: 'External loan', icon: 'pi pi-', routerLink: [OPERATIONS_INTERNAL_URL, VIEW_EXTERNAL_LOAN_INTERNAL_URL] };
            operationsItem.items!.push(viewExternalLoadAction);
        }

        if(operationsItem.items?.length === 0) {
            this.model = this.model.filter(item => item.id !== 'operations');
        }
    }
}
