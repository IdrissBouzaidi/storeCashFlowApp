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
import { TransactionAddingFormComponent } from './components/transaction-adding-form/transaction-adding-form.component';
import { CapitalContributionArrayComponent } from './components/capital-contribution-array/capital-contribution-array.component';
import { CapitalContributionService } from '../../../service/capital-contribution.service';
import { CapitalContribution } from '../../../models/capital-contribution';

@Component({
    selector: 'app-capital-contribution',
    imports: [
        AutocompleteComponent,
        CapitalContributionArrayComponent,
        TransactionAddingFormComponent,
        CommonModule,
        ReactiveFormsModule,
        FloatLabel,
        DatePicker,
        ButtonModule
    ],
    templateUrl: './capital-contribution.component.html',
    styleUrl: './capital-contribution.component.scss'
})
export class CapitalContributionComponent {

    form!: FormGroup;
    
    transactionStatesAutocompleteDetails?: AutocompleteModel;
    periodsAutocompleteDetails?: AutocompleteModel;
    contributorAutocompleteDetails?: AutocompleteModel;

    contrCapitalList$?: Observable<CapitalContribution[]>;

    searchButtonIsLoading: boolean = false;

    addContrCapitalDialogStatus: DialogDetails = new DialogDetails(undefined, false, false);

    constructor(
        private refTableService: RefTableService,
        private capitalContributionService: CapitalContributionService,
        private fb: FormBuilder
    ) {
        debugger;
        this.setFormControls();
        this.getRefTables();
        this.getData();
    }
    
    setFormControls() {
        this.form = this.fb.group({
            contrDateMin: [null],
            contrDateMax: [null],
            transactionState: [null],
            period: [null],
            contributor: ['']
        });
    }

    private getRefTables() {
        this.refTableService.getCapitalContributionTransactionStatesRefTable().subscribe(
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
                this.contributorAutocompleteDetails = new AutocompleteModel("contributorRefTable", "Contributor", data, undefined, undefined);
            }
        );
    }

    public getData() {
        let test = this.form.get('contrDateMin')!.value;
        debugger;
        this.contrCapitalList$ = this.capitalContributionService.getCapitalContributionList(this.form.get('contrDateMin')!.value, this.form.get('contrDateMax')!.value, this.form.get('transactionState')!.value?.id, this.form.get('period')!.value?.id, this.form.get('contributor')!.value?.id);
    }

    onAddCapitalContrClick() {
        debugger
        this.addContrCapitalDialogStatus = new DialogDetails(undefined, true, false);
    }

    onDialogStatusChanged(newStatus: DialogDetails) {
        this.addContrCapitalDialogStatus = newStatus;
        if(newStatus.dataSaved) {
            this.getData();
        }
        debugger;
    }
}
