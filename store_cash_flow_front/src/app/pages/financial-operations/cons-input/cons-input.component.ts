import { ChangeDetectorRef, Component } from '@angular/core';
import { ConsInputArrayComponent } from './components/cons-input-array/cons-input-array.component';
import { forkJoin, Observable, of, switchMap, tap } from 'rxjs';
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
import { TransactionAddingFormComponent } from './components/transaction-adding-form/transaction-adding-form.component';
import { DialogDetails } from '../../../models/dialog-details';
import { PRODUCT_TRANSACTION_STATES } from '../../../utils/consts/states-consts';
import { DIALOG_ADDING_IS_MONO, DIALOG_ADDING_IS_MULTIPLE } from '../../../utils/consts/dialog-consts';
import { TransactionMultipleAddingFormComponent } from './components/transaction-multiple-adding-form/transaction-multiple-adding-form.component';

@Component({
    selector: 'app-cons-input',
    imports: [
        AutocompleteComponent,
        ConsInputArrayComponent,
        TransactionAddingFormComponent,
        TransactionMultipleAddingFormComponent,

        CommonModule,
        FormsModule,
        FloatLabel,
        DatePicker,
        ButtonModule
    ],
    templateUrl: './cons-input.component.html',
    styleUrl: './cons-input.component.scss'
})
export class ConsInputComponent {

    form!: FormGroup;
    
    productsAutocompleteDetails?: AutocompleteModel;
    selectedTransactionDateMin?: Date;
    selectedTransactionDateMax?: Date;
    transactionTypesAutocompleteDetails?: AutocompleteModel;
    productsTransactionStatesAutocompleteDetails?: AutocompleteModel;
    periodsAutocompleteDetails?: AutocompleteModel;
    executedByAutocompleteDetails?: AutocompleteModel;

    consInputsList$?: Observable<ConsInput[]>;

    searchButtonIsLoading: boolean = false;

    addConsInputDialogStatus: DialogDetails = new DialogDetails(undefined, false, false);

    constructor(
        private refTableService: RefTableService,
        private consInputService: ConsInputService,
        private fb: FormBuilder
    ) {
        this.setFormControls();
        this.getRefTables();
    }
    
    setFormControls() {
        this.form = this.fb.group({
            product: [null],
            transactionType: [null],
            productsTransactionStates: [null],
            period: [null],
            executedBy: ['']
        });
    }

    private getRefTables() {
        const getProductsRefTable$ = this.refTableService.getProductsRefTable().pipe(
            tap(
                data => {
                    this.productsAutocompleteDetails = new AutocompleteModel("productsRefTable", "Products", data, undefined, undefined);
                }
            )
        );
        const getTransactionTypesRefTable$ = this.refTableService.getTransactionTypesRefTable().pipe(
            tap(
                data => {
                    this.transactionTypesAutocompleteDetails = new AutocompleteModel("transactionTypesRefTable", "Transaction types", data, undefined, undefined);
                }
            )
        );
        const getProductsTransactionStatesRefTable$ = this.refTableService.getProductsTransactionStatesRefTable().pipe(
            tap(
                data => {
                    const availableState = data.find(item => item.id === PRODUCT_TRANSACTION_STATES.AVAILABLE);
                    this.form.get('productsTransactionStates')!.setValue(availableState);
                    this.productsTransactionStatesAutocompleteDetails = new AutocompleteModel("statesRefTable", "States", data, undefined, undefined);
                }
            )
        );
        const getPeriodsRefTable$ = this.refTableService.getPeriodsRefTable().pipe(
            tap(
                data => {
                    this.periodsAutocompleteDetails = new AutocompleteModel("periodsRefTable", "Periods", data, undefined, undefined);
                }
            )
        );
        const getUsersRefTable$ = this.refTableService.getUsersRefTable().pipe(
            tap(
                data => {
                    this.executedByAutocompleteDetails = new AutocompleteModel("executedByRefTable", "Executed by", data, undefined, undefined);
                }
            )
        );

        forkJoin({
            getProductsRefTable: getProductsRefTable$,
            getTransactionTypesRefTable: getTransactionTypesRefTable$,
            getProductsTransactionStatesRefTable: getProductsTransactionStatesRefTable$,
            getPeriodsRefTable: getPeriodsRefTable$,
            getUsersRefTable: getUsersRefTable$
        }).pipe(
            switchMap(
                () => {
                    this.getData();
                    if(this.consInputsList$)
                        return this.consInputsList$;
                    else
                        return of(undefined);
                    }
            )
        ).subscribe();
    }

    public getData() {
        this.consInputsList$ = this.consInputService.getConsInputsList(this.form.get('product')!.value?.id, this.form.get('productsTransactionStates')!.value?.id, this.form.get('period')!.value?.id, this.form.get('executedBy')!.value?.id, this.selectedTransactionDateMin, this.selectedTransactionDateMax);
    }

    onAddConsInputClick(isMultiple: boolean) {
        debugger
        const dialogId: number = isMultiple? DIALOG_ADDING_IS_MULTIPLE: DIALOG_ADDING_IS_MONO;
        this.addConsInputDialogStatus = new DialogDetails(dialogId, true, false);
    }

    onDialogStatusChanged(newStatus: DialogDetails) {
        this.addConsInputDialogStatus = newStatus;
        if(newStatus.dataSaved) {
            this.getData();
        }
        debugger;
    }
}
