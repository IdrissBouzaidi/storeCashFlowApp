import { Component } from '@angular/core';
import { ChargeArrayComponent } from './components/charge-array/charge-array.component';
import { Observable } from 'rxjs';
import { ConsInput } from '../../../models/cons-input';
import { ConsInputService } from '../../../service/cons-input.service';
import { AutocompleteComponent } from '../../../shared-components/autocomplete/autocomplete.component';
import { AutocompleteModel } from '../../../models/autocomplete-model';
import { RefTableService } from '../../../service/ref-table.service';
import { CommonModule } from '@angular/common';
import { DatePicker } from 'primeng/datepicker';
import { FloatLabel } from 'primeng/floatlabel';
import { FormBuilder, FormGroup, FormsModule } from '@angular/forms';
import { ButtonModule } from 'primeng/button';
import { DialogDetails } from '../../../models/dialog-details';
import { TransactionAddingFormComponent } from './components/transaction-adding-form/transaction-adding-form.component';
import { ChargeService } from '../../../service/charge.service';
import { Charge } from '../../../models/charge';

@Component({
    selector: 'app-charge',
    imports: [
        AutocompleteComponent,
        ChargeArrayComponent,
        TransactionAddingFormComponent,
        CommonModule,
        FormsModule,
        FloatLabel,
        DatePicker,
        ButtonModule
    ],
    templateUrl: './charge.component.html',
    styleUrl: './charge.component.scss'
})
export class ChargeComponent {

    form!: FormGroup;
    
    chargeTypesAutocompleteDetails?: AutocompleteModel;
    selectedTransactionDateMin?: Date;
    selectedTransactionDateMax?: Date;
    transactionTypesAutocompleteDetails?: AutocompleteModel;
    chargesTransactionStatesAutocompleteDetails?: AutocompleteModel;
    periodsAutocompleteDetails?: AutocompleteModel;
    consumedByAutocompleteDetails?: AutocompleteModel;

    chargesList$?: Observable<Charge[]>;

    searchButtonIsLoading: boolean = false;

    addChargeDialogStatus: DialogDetails = new DialogDetails(undefined, false, false);

    constructor(
        private refTableService: RefTableService,
        private chargeService: ChargeService,
        private fb: FormBuilder
    ) {
        this.setFormControls();
        this.getRefTables();
        this.getData();
    }
    
    setFormControls() {
        this.form = this.fb.group({
            chargeType: [null],
            transactionType: [null],
            chargesTransactionStates: [null],
            period: [null],
            consumedBy: ['']
        });
    }

    private getRefTables() {
        this.refTableService.getChargeTypesRefTable().subscribe(
            data => {
                this.chargeTypesAutocompleteDetails = new AutocompleteModel("chargeTypesRefTable", "charge Types", data, undefined, undefined);
            }
        );
        this.refTableService.getTransactionTypesRefTable().subscribe(
            data => {
                this.transactionTypesAutocompleteDetails = new AutocompleteModel("transactionTypesRefTable", "Transaction types", data, undefined, undefined);
            }
        );
        this.refTableService.getChargesTransactionStatesRefTable().subscribe(
            data => {
                this.chargesTransactionStatesAutocompleteDetails = new AutocompleteModel("statesRefTable", "States", data, undefined, undefined);
            }
        );
        this.refTableService.getPeriodsRefTable().subscribe(
            data => {
                this.periodsAutocompleteDetails = new AutocompleteModel("periodsRefTable", "Periods", data, undefined, undefined);
            }
        );
        this.refTableService.getUsersRefTable().subscribe(
            data => {
                this.consumedByAutocompleteDetails = new AutocompleteModel("consumedByRefTable", "Consumed by", data, undefined, undefined);
            }
        );
    }

    public getData() {

        this.chargesList$ = this.chargeService.getChargesList(this.form.get('chargeType')!.value?.id, this.selectedTransactionDateMin, this.selectedTransactionDateMax, this.form.get('chargesTransactionStates')!.value?.id, this.form.get('period')!.value?.id, this.form.get('consumedBy')!.value?.id);
    }

    onAddChargeClick() {
        debugger
        this.addChargeDialogStatus = new DialogDetails(undefined, true, false);
    }

    onDialogStatusChanged(newStatus: DialogDetails) {
        this.addChargeDialogStatus = newStatus;
        if(newStatus.dataSaved) {
            this.getData();
        }
        debugger;
    }
}
