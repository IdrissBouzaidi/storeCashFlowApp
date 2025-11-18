import { Component } from '@angular/core';
import { Observable } from 'rxjs';
import { CommonModule } from '@angular/common';
import { DatePicker } from 'primeng/datepicker';
import { FloatLabel } from 'primeng/floatlabel';
import { FormBuilder, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { ButtonModule } from 'primeng/button';
import { TransactionAddingFormComponent } from './components/transaction-adding-form/transaction-adding-form.component';
import { PeriodsArrayComponent } from './components/periods-array/periods-array.component';
import { AutocompleteComponent } from '../../shared-components/autocomplete/autocomplete.component';
import { AutocompleteModel } from '../../models/autocomplete-model';
import { Advance } from '../../models/advance';
import { DialogDetails } from '../../models/dialog-details';
import { RefTableService } from '../../service/ref-table.service';
import { AdvanceService } from '../../service/advance.service';
import { FinancialPeriodService } from '../../service/financial-period.service';
import { FinancialPeriod } from '../../models/financial-period';

@Component({
    selector: 'app-periods',
    imports: [
        AutocompleteComponent,
        PeriodsArrayComponent,
        TransactionAddingFormComponent,
        CommonModule,
        ReactiveFormsModule,
        FloatLabel,
        DatePicker,
        ButtonModule
    ],
    templateUrl: './periods.component.html',
    styleUrl: './periods.component.scss'
})
export class PeriodsComponent {

    form!: FormGroup;

    periodStatesAutocompleteDetails?: AutocompleteModel;

    periodsList$?: Observable<FinancialPeriod[]>;

    searchButtonIsLoading: boolean = false;

    addingDialogStatus: DialogDetails = new DialogDetails(undefined, false, false);

    constructor(
        private refTableService: RefTableService,
        private financialPeriodService: FinancialPeriodService,
        private fb: FormBuilder
    ) {
        debugger;
        this.setFormControls();
        this.getRefTables();
        this.getData();
    }
    
    setFormControls() {
        this.form = this.fb.group({
            startDateMin: [null],
            startDateMax: [null],
            endDateMin: [null],
            endDateMax: [null],
            stateId: [null]
        });
    }

    private getRefTables() {
        this.refTableService.getPeriodsStatesRefTable().subscribe(
            data => {
                this.periodStatesAutocompleteDetails = new AutocompleteModel("periodStatesRefTable", "States", data, undefined, undefined);
            }
        );
    }

    public getData() {
        debugger;
        const startDateMin: Date | undefined = this.form.get('startDateMin')!.value;
        const startDateMax: Date | undefined = this.form.get('startDateMax')!.value;
        const endDateMin: Date | undefined = this.form.get('endDateMin')!.value;
        const endDateMax: Date | undefined = this.form.get('endDateMax')!.value;
        const stateId: number | undefined = this.form.get('stateId')!.value?.id;
        this.periodsList$ = this.financialPeriodService.getFinancialPeriods(startDateMin, startDateMax, endDateMin, endDateMax, stateId);
    }

    onAddButtonClick() {
        debugger
        this.addingDialogStatus = new DialogDetails(undefined, true, false);
    }

    onDialogStatusChanged(newStatus: DialogDetails) {
        this.addingDialogStatus = newStatus;
        if(newStatus.dataSaved) {
            this.getData();
        }
        debugger;
    }
}
