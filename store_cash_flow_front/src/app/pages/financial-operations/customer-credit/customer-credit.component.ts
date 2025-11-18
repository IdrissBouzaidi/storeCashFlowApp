import { Component } from '@angular/core';
import { Observable } from 'rxjs';
import { AutocompleteComponent } from '../../../shared-components/autocomplete/autocomplete.component';
import { AutocompleteModel } from '../../../models/autocomplete-model';
import { RefTableService } from '../../../service/ref-table.service';
import { CommonModule } from '@angular/common';
import { DatePicker } from 'primeng/datepicker';
import { FloatLabel } from 'primeng/floatlabel';
import { FormBuilder, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { ButtonModule } from 'primeng/button';
import { DialogDetails } from '../../../models/dialog-details';
import { TransactionAddingFormComponent } from './components/transaction-adding-form/transaction-adding-form.component';
import { AdvanceService } from '../../../service/advance.service';
import { CustomerCreditArrayComponent } from './components/customer-credit-array/customer-credit-array.component';
import { CustomerCredit } from '../../../models/customer-credit';
import { CustomerCreditService } from '../../../service/customer-credit.service';

@Component({
    selector: 'app-customer-credit',
    imports: [
        AutocompleteComponent,
        CustomerCreditArrayComponent,
        TransactionAddingFormComponent,
        CommonModule,
        ReactiveFormsModule,
        FloatLabel,
        DatePicker,
        ButtonModule
    ],
    templateUrl: './customer-credit.component.html',
    styleUrl: './customer-credit.component.scss'
})
export class CustomerCreditComponent {

    form!: FormGroup;

    transactionStatesAutocompleteDetails?: AutocompleteModel;
    periodsAutocompleteDetails?: AutocompleteModel;
    customerAutocompleteDetails?: AutocompleteModel;

    customerCreditList$?: Observable<CustomerCredit[]>;

    searchButtonIsLoading: boolean = false;

    addCustomerCreditDialogStatus: DialogDetails = new DialogDetails(undefined, false, false);

    constructor(
        private refTableService: RefTableService,
        private customerCreditService: CustomerCreditService,
        private fb: FormBuilder
    ) {
        debugger;
        this.setFormControls();
        this.getRefTables();
        this.getData();
    }
    
    setFormControls() {
        this.form = this.fb.group({
            creditDateMin: [null],
            creditDateMax: [null],
            transactionState: [null],
            period: [null],
            customer: ['']
        });
    }

    private getRefTables() {
        this.refTableService.getCustomerCreditStatesRefTable().subscribe(
            data => {
                this.transactionStatesAutocompleteDetails = new AutocompleteModel("statesRefTable", "States", data, undefined, undefined);
            }
        );
        this.refTableService.getPeriodsRefTable().subscribe(
            data => {
                this.periodsAutocompleteDetails = new AutocompleteModel("periodsRefTable", "Periods", data, undefined, undefined);
            }
        );
        this.refTableService.getCustomersRefTable().subscribe(
            data => {
                this.customerAutocompleteDetails = new AutocompleteModel("customerRefTable", "Customer", data, undefined, undefined);
            }
        );
    }

    public getData() {
        debugger;
        const creditDateMin: Date | undefined = this.form.get('creditDateMin')!.value;
        const creditDateMax: Date | undefined = this.form.get('creditDateMax')!.value;
        const stateId: number | undefined = this.form.get('transactionState')!.value?.id;
        const periodId: number | undefined = this.form.get('period')!.value?.id;
        const customerId: number | undefined = this.form.get('customer')!.value?.id;
        this.customerCreditList$ = this.customerCreditService.getCustomerCredits(creditDateMin, creditDateMax, stateId, periodId, customerId);
    }

    onAddCustomerCreditClick() {
        debugger
        this.addCustomerCreditDialogStatus = new DialogDetails(undefined, true, false);
    }

    onDialogStatusChanged(newStatus: DialogDetails) {
        this.addCustomerCreditDialogStatus = newStatus;
        if(newStatus.dataSaved) {
            this.getData();
        }
        debugger;
    }
}
