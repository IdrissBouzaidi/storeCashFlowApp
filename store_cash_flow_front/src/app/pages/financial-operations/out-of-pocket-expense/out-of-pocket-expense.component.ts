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
import { OutOfPocketExpenseArrayComponent } from './components/out-of-pocket-expense-array/out-of-pocket-expense-array.component';
import { OutOfPocketService } from '../../../service/out-of-pocket.service';
import { OutOfPocket } from '../../../models/out-of-pocket';

@Component({
    selector: 'app-out-of-pocket-expense',
    imports: [
        AutocompleteComponent,
        OutOfPocketExpenseComponent,
        TransactionAddingFormComponent,
        OutOfPocketExpenseArrayComponent,

        CommonModule,
        ReactiveFormsModule,
        FloatLabel,
        DatePicker,
        ButtonModule,
    ],
    templateUrl: './out-of-pocket-expense.component.html',
    styleUrl: './out-of-pocket-expense.component.scss'
})
export class OutOfPocketExpenseComponent {

    form!: FormGroup;

    selectedTransactionDateMin?: Date;
    selectedTransactionDateMax?: Date;
    statesAutocompleteDetails?: AutocompleteModel;
    periodsAutocompleteDetails?: AutocompleteModel;
    borrowersAutocompleteDetails?: AutocompleteModel;

    outOfPocketsList$?: Observable<OutOfPocket[]>;

    searchButtonIsLoading: boolean = false;

    addOutOfPocketDialogStatus: DialogDetails = new DialogDetails(undefined, false, false);

    constructor(
        private refTableService: RefTableService,
        private outOfPocketService: OutOfPocketService,
        private fb: FormBuilder
    ) {
        this.getRefTables();
        this.setFormControls();
        this.getData();
    }
    
    setFormControls() {
        this.form = this.fb.group({
            transactionDateMin: [null],
            transactionDateMax: [null],
            state: [null],
            period: [null],
            borrower: ['']
        });
    }

    private getRefTables() {
        this.refTableService.getOutOfPocketStatesRefTable().subscribe(
            data => {
                this.statesAutocompleteDetails = new AutocompleteModel("statesRefTable", "States", data, undefined, undefined);
            }
        );
        this.refTableService.getPeriodsRefTable().subscribe(
            data => {
                this.periodsAutocompleteDetails = new AutocompleteModel("periodsRefTable", "Periods", data, undefined, undefined);
            }
        );
        this.refTableService.getUsersRefTable().subscribe(
            data => {
                this.borrowersAutocompleteDetails = new AutocompleteModel("borrowerRefTable", "Borrower", data, undefined, undefined);
            }
        );
    }

    public getData() {
        const transDateMin: Date | undefined = this.form.get('transactionDateMin')!.value;
        const transDateMax: Date | undefined = this.form.get('transactionDateMax')!.value;
        const stateId: number | undefined = this.form.get('state')!.value?.id;
        const periodId: number | undefined = this.form.get('period')!.value?.id;
        const borrowerId: number | undefined = this.form.get('borrower')!.value?.id;
        this.outOfPocketsList$ = this.outOfPocketService.getOutOfPockets(transDateMin, transDateMax, stateId, periodId, borrowerId);
    }

    onAddOutOfPocketClick() {
        debugger
        this.addOutOfPocketDialogStatus = new DialogDetails(undefined, true, false);
    }
    
    onDialogStatusChanged(newStatus: DialogDetails) {
        this.addOutOfPocketDialogStatus = newStatus;
        if(newStatus.dataSaved) {
            this.getData();
        }
        debugger;
    }
}
