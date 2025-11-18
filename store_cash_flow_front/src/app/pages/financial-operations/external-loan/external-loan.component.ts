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
import { ExternalLoan } from '../../../models/external-loan';
import { ExternalLoanService } from '../../../service/external-loan.service';
import { ExternalLoanArrayComponent } from './components/external-loan-array/external-loan-array.component';

@Component({
    selector: 'app-external-loan',
    imports: [
        AutocompleteComponent,
        ExternalLoanArrayComponent,
        TransactionAddingFormComponent,
        CommonModule,
        ReactiveFormsModule,
        FloatLabel,
        DatePicker,
        ButtonModule
    ],
    templateUrl: './external-loan.component.html',
    styleUrl: './external-loan.component.scss'
})
export class ExternalLoanComponent {

    form!: FormGroup;

    transactionStatesAutocompleteDetails?: AutocompleteModel;
    periodsAutocompleteDetails?: AutocompleteModel;
    creditorAutocompleteDetails?: AutocompleteModel;

    externalLoanList$?: Observable<ExternalLoan[]>;

    searchButtonIsLoading: boolean = false;

    addExternalLoanDialogStatus: DialogDetails = new DialogDetails(undefined, false, false);

    constructor(
        private refTableService: RefTableService,
        private externalLoanService: ExternalLoanService,
        private fb: FormBuilder
    ) {
        debugger;
        this.setFormControls();
        this.getRefTables();
        this.getData();
    }
    
    setFormControls() {
        this.form = this.fb.group({
            loanDateMin: [null],
            loanDateMax: [null],
            transactionState: [null],
            period: [null],
            creditor: ['']
        });
    }

    private getRefTables() {
        this.refTableService.getExternalLoanStatesRefTable().subscribe(
            data => {
                this.transactionStatesAutocompleteDetails = new AutocompleteModel("statesRefTable", "States", data, undefined, undefined);
            }
        );
        this.refTableService.getPeriodsRefTable().subscribe(
            data => {
                this.periodsAutocompleteDetails = new AutocompleteModel("periodsRefTable", "Periods", data, undefined, undefined);
            }
        );
        this.refTableService.getUsersRefTable().subscribe(
            data => {
                this.creditorAutocompleteDetails = new AutocompleteModel("creditorRefTable", "Creditor", data, undefined, undefined);
            }
        );
    }

    public getData() {
        debugger;
        const loanDateMin: Date | undefined = this.form.get('loanDateMin')!.value;
        const loanDateMax: Date | undefined = this.form.get('loanDateMax')!.value;
        const stateId: number | undefined = this.form.get('transactionState')!.value?.id;
        const periodId: number | undefined = this.form.get('period')!.value?.id;
        const creditorId: number | undefined = this.form.get('creditor')!.value?.id;
        this.externalLoanList$ = this.externalLoanService.getExternalLoans(loanDateMin, loanDateMax, stateId, periodId, creditorId);
    }

    onAddExternalLoanClick() {
        debugger
        this.addExternalLoanDialogStatus = new DialogDetails(undefined, true, false);
    }

    onDialogStatusChanged(newStatus: DialogDetails) {
        this.addExternalLoanDialogStatus = newStatus;
        if(newStatus.dataSaved) {
            this.getData();
        }
        debugger;
    }
}
