import { Component } from '@angular/core';
import { Observable } from 'rxjs';
import { ConsInput } from '../../../models/cons-input';
import { ConsInputService } from '../../../service/cons-input.service';
import { AutocompleteComponent } from '../../../shared-components/autocomplete/autocomplete.component';
import { AutocompleteModel } from '../../../models/autocomplete-model';
import { RefTableService } from '../../../service/ref-table.service';
import { CommonModule } from '@angular/common';
import { DatePicker } from 'primeng/datepicker';
import { FloatLabel } from 'primeng/floatlabel';
import { FormBuilder, FormGroup, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { ButtonModule } from 'primeng/button';
import { DialogDetails } from '../../../models/dialog-details';
import { AllOperationsArrayComponent } from './components/all-operations-array/all-operations-array.component';
import { TransactionService } from '../../../service/transaction.service';
import { Transaction } from '../../../models/transaction';

@Component({
    selector: 'app-all-operations',
    imports: [
        AutocompleteComponent,
        AllOperationsArrayComponent,

        CommonModule,
        ReactiveFormsModule,
        FloatLabel,
        DatePicker,
        ButtonModule
    ],
    templateUrl: './all-operations.component.html',
    styleUrl: './all-operations.component.scss'
})
export class AllOperationsComponent {

    form!: FormGroup;
    
    transactionTypesAutocompleteDetails?: AutocompleteModel;
    periodsAutocompleteDetails?: AutocompleteModel;
    executedByAutocompleteDetails?: AutocompleteModel;

    transactionList$?: Observable<Transaction[]>;

    searchButtonIsLoading: boolean = false;

    constructor(
        private refTableService: RefTableService,
        private transactionService: TransactionService,
        private consInputService: ConsInputService,
        private fb: FormBuilder
    ) {
        this.setFormControls();
        this.getRefTables();
        this.getData();
    }
    
    setFormControls() {
        this.form = this.fb.group({
            transactionDateMin: [null],
            transactionDateMax: [null],
            transactionType: [null],
            period: [null],
            executedBy: ['']
        });
    }

    private getRefTables() {
        this.refTableService.getTransactionTypesRefTable().subscribe(
            data => {
                this.transactionTypesAutocompleteDetails = new AutocompleteModel("transactionTypesRefTable", "Transaction types", data, undefined, undefined);
            }
        );
        this.refTableService.getPeriodsRefTable().subscribe(
            data => {
                this.periodsAutocompleteDetails = new AutocompleteModel("periodsRefTable", "Periods", data, undefined, undefined);
            }
        );
        this.refTableService.getUsersRefTable().subscribe(
            data => {
                this.executedByAutocompleteDetails = new AutocompleteModel("executedByRefTable", "Executed by", data, undefined, undefined);
            }
        );
    }

    public getData() {
        const transactionDateMin: Date | undefined = this.form.get('transactionDateMin')!.value;
        const transactionDateMax: Date | undefined = this.form.get('transactionDateMax')!.value;
        const transactionTypeId: number | undefined = this.form.get('transactionType')!.value?.id;
        const periodId: number | undefined = this.form.get('period')!.value?.id;
        const executedBy: number | undefined = this.form.get('executedBy')!.value?.id;
        this.transactionList$ = this.transactionService.getTransactions(transactionTypeId, periodId, executedBy, transactionDateMin, transactionDateMax);
    }
}
