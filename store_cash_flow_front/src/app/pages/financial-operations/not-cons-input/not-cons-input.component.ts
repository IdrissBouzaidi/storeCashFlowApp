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
import { TransactionAddingFormComponent } from './components/transaction-adding-form/transaction-adding-form.component';
import { DialogDetails } from '../../../models/dialog-details';
import { NotConsInputArrayComponent } from './components/not-cons-input-array/not-cons-input-array.component';
import { NotConsInputService } from '../../../service/not-cons-input.service';
import { NotConsInput } from '../../../models/not-cons-input';

@Component({
    selector: 'app-not-cons-input',
    imports: [
        AutocompleteComponent,
        NotConsInputArrayComponent,
        TransactionAddingFormComponent,

        CommonModule,
        ReactiveFormsModule,
        FloatLabel,
        DatePicker,
        ButtonModule
    ],
    templateUrl: './not-cons-input.component.html',
    styleUrl: './not-cons-input.component.scss'
})
export class NotConsInputComponent {

    form!: FormGroup;
    
    periodsAutocompleteDetails?: AutocompleteModel;
    executedByAutocompleteDetails?: AutocompleteModel;
    notConsInputStatesAutocompleteDetails?: AutocompleteModel;

    notConsInputsList$?: Observable<NotConsInput[]>;

    searchButtonIsLoading: boolean = false;

    addNotConsInputDialogStatus: DialogDetails = new DialogDetails(undefined, false, false);

    constructor(
        private refTableService: RefTableService,
        private notConsInputService: NotConsInputService,
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
            period: [null],
            executedBy: [null],
            notConsInputState: [null]
        });
    }

    private getRefTables() {
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
        this.refTableService.getNotConsInputStatesRefTable().subscribe(
            data => {
                this.notConsInputStatesAutocompleteDetails = new AutocompleteModel("notConsInputStatesRefTable", "Transaction state", data, undefined, undefined);
            }
        );
    }

    public getData() {
        const transactionDateMin: Date | undefined = this.form.get('transactionDateMin')!.value;
        const transactionDateMax: Date | undefined = this.form.get('transactionDateMax')!.value;
        const periodId: number | undefined = this.form.get('period')!.value?.id;
        const executedById: number | undefined = this.form.get('executedBy')!.value?.id;
        const notConsInputStateId: number | undefined = this.form.get('notConsInputState')!.value?.id;
        this.notConsInputsList$ = this.notConsInputService.getNotConsInputs(transactionDateMin, transactionDateMax, periodId, executedById, notConsInputStateId);
    }

    onAddNotConsInputClick() {
        debugger
        this.addNotConsInputDialogStatus = new DialogDetails(undefined, true, false);
    }

    onDialogStatusChanged(newStatus: DialogDetails) {
        this.addNotConsInputDialogStatus = newStatus;
        if(newStatus.dataSaved) {
            this.getData();
        }
        debugger;
    }
}
