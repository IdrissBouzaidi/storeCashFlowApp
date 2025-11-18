import { Component } from '@angular/core';
import { Observable } from 'rxjs';
import { AutocompleteComponent } from '../../../shared-components/autocomplete/autocomplete.component';
import { AutocompleteModel } from '../../../models/autocomplete-model';
import { RefTableService } from '../../../service/ref-table.service';
import { CommonModule } from '@angular/common';
import { DatePicker } from 'primeng/datepicker';
import { FloatLabel } from 'primeng/floatlabel';
import { FormBuilder, FormGroup, FormsModule, Validators } from '@angular/forms';
import { ButtonModule } from 'primeng/button';
import { TransactionAddingFormComponent } from './components/transaction-adding-form/transaction-adding-form.component';
import { OutputArrayComponent } from './components/output-array/output-array.component';
import { Output } from '../../../models/output';
import { OutputService } from '../../../service/output.service';
import { DialogDetails } from '../../../models/dialog-details';
import { PRODUCT_TRANSACTION_STATES } from '../../../utils/consts/states-consts';

@Component({
    selector: 'app-output',
    imports: [
        AutocompleteComponent,
        OutputArrayComponent,
        TransactionAddingFormComponent,

        CommonModule,
        FormsModule,
        FloatLabel,
        DatePicker,
        ButtonModule
    ],
    templateUrl: './output.component.html',
    styleUrl: './output.component.scss'
})
export class OutputComponent {

    form!: FormGroup;

    productsAutocompleteDetails?: AutocompleteModel;
    selectedTransactionDateMin?: Date;
    selectedTransactionDateMax?: Date;
    productsTransactionStatesAutocompleteDetails?: AutocompleteModel;
    periodsAutocompleteDetails?: AutocompleteModel;
    soldByAutocompleteDetails?: AutocompleteModel;

    outputsList$?: Observable<Output[]>;

    searchButtonIsLoading: boolean = false;

    addOutputDialogStatus: DialogDetails = new DialogDetails(undefined, false, false);

    constructor(
        private refTableService: RefTableService,
        private outputService: OutputService,
        private fb: FormBuilder
    ) {
        this.getRefTables();
        this.setFormControls();
        this.getData();
    }
    
    setFormControls() {
        this.form = this.fb.group({
            product: [null],
            productsTransactionStates: [null],
            period: [null],
            soldBy: ['']
        });
    }

    private getRefTables() {
        this.refTableService.getProductsRefTable().subscribe(
            data => {
                this.productsAutocompleteDetails = new AutocompleteModel("productsRefTable", "Products", data, undefined, undefined);
            }
        );
        this.refTableService.getProductsTransactionStatesRefTable().subscribe(
            data => {
                data = data.filter(item => item.id !== PRODUCT_TRANSACTION_STATES.AVAILABLE);
                this.productsTransactionStatesAutocompleteDetails = new AutocompleteModel("statesRefTable", "States", data, undefined, undefined);
            }
        );
        this.refTableService.getPeriodsRefTable().subscribe(
            data => {
                this.periodsAutocompleteDetails = new AutocompleteModel("periodsRefTable", "Periods", data, undefined, undefined);
            }
        );
        this.refTableService.getUsersRefTable().subscribe(
            data => {
                this.soldByAutocompleteDetails = new AutocompleteModel("soldByRefTable", "Sold by", data, undefined, undefined);
            }
        );
    }

    public getData() {
        this.outputsList$ = this.outputService.getOutputsList(this.form.get('product')!.value?.id, this.form.get('productsTransactionStates')!.value?.id, this.form.get('period')!.value?.id, this.form.get('soldBy')!.value?.id, this.selectedTransactionDateMin, this.selectedTransactionDateMax);
    }

    onAddOutputClick() {
        debugger
        this.addOutputDialogStatus = new DialogDetails(undefined, true, false);
    }
    
    onDialogStatusChanged(newStatus: DialogDetails) {
        this.addOutputDialogStatus = newStatus;
        if(newStatus.dataSaved) {
            this.getData();
        }
        debugger;
    }
}
