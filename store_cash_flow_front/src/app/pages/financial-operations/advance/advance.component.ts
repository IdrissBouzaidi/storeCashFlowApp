import { Component } from '@angular/core';
import { Observable, tap } from 'rxjs';
import { AutocompleteComponent } from '../../../shared-components/autocomplete/autocomplete.component';
import { AutocompleteModel } from '../../../models/autocomplete-model';
import { RefTableService } from '../../../service/ref-table.service';
import { CommonModule } from '@angular/common';
import { DatePicker } from 'primeng/datepicker';
import { FloatLabel } from 'primeng/floatlabel';
import { FormBuilder, FormGroup, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { ButtonModule } from 'primeng/button';
import { DialogDetails } from '../../../models/dialog-details';
import { TransactionAddingFormComponent } from './components/transaction-adding-form/transaction-adding-form.component';
import { AdvanceArrayComponent } from './components/advance-array/advance-array.component';
import { AdvanceService } from '../../../service/advance.service';
import { Advance } from '../../../models/advance';
import { FinancialPeriodService } from '../../../service/financial-period.service';
import { FinancialPeriod } from '../../../models/financial-period';
import { FINANCIAL_PERIOD_STATES } from '../../../utils/consts/states-consts';

@Component({
    selector: 'app-advance',
    imports: [
        AutocompleteComponent,
        AdvanceArrayComponent,
        TransactionAddingFormComponent,
        CommonModule,
        ReactiveFormsModule,
        FloatLabel,
        DatePicker,
        ButtonModule
    ],
    templateUrl: './advance.component.html',
    styleUrl: './advance.component.scss'
})
export class AdvanceComponent {

    form!: FormGroup;

    transactionStatesAutocompleteDetails?: AutocompleteModel;
    periodsAutocompleteDetails?: AutocompleteModel;
    takerAutocompleteDetails?: AutocompleteModel;

    advanceList$?: Observable<Advance[]>;

    searchButtonIsLoading: boolean = false;

    addAdvanceDialogStatus: DialogDetails = new DialogDetails(undefined, false, false);

    constructor(
        private refTableService: RefTableService,
        private advanceService: AdvanceService,
        private fb: FormBuilder
    ) {
        debugger;
        this.setFormControls();
        this.getRefTables();
        this.getData();
    }
    
    setFormControls() {
        this.form = this.fb.group({
            advanceDateMin: [null],
            advanceDateMax: [null],
            transactionState: [null],
            period: [null],
            taker: ['']
        });
    }

    private getRefTables() {
        this.refTableService.getAdvanceTransactionStatesRefTable().subscribe(
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
                this.takerAutocompleteDetails = new AutocompleteModel("takerRefTable", "Taker", data, undefined, undefined);
            }
        );
    }

    public getData() {
        debugger;
        const advanceDateMin: Date | undefined = this.form.get('advanceDateMin')!.value;
        const advanceDateMax: Date | undefined = this.form.get('advanceDateMax')!.value;
        const stateId: number | undefined = this.form.get('transactionState')!.value?.id;
        const periodId: number | undefined = this.form.get('period')!.value?.id;
        const takerId: number | undefined = this.form.get('taker')!.value?.id;
        this.advanceList$ = this.advanceService.getAdvances(advanceDateMin, advanceDateMax, stateId, periodId, takerId);
    }

    onAddAdvanceClick() {
        debugger
        this.addAdvanceDialogStatus = new DialogDetails(undefined, true, false);
    }

    onDialogStatusChanged(newStatus: DialogDetails) {
        this.addAdvanceDialogStatus = newStatus;
        if(newStatus.dataSaved) {
            this.getData();
        }
        debugger;
    }
}
