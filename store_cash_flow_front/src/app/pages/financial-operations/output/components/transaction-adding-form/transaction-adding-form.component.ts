import { Component, EventEmitter, Input, Output } from '@angular/core';
import { RefTableService } from '../../../../../service/ref-table.service';
import { AutocompleteModel } from '../../../../../models/autocomplete-model';
import { AutocompleteComponent } from '../../../../../shared-components/autocomplete/autocomplete.component';
import { FloatLabel } from 'primeng/floatlabel';
import { DatePicker } from 'primeng/datepicker';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { InputTextModule } from 'primeng/inputtext';
import { InputNumber, InputNumberModule } from 'primeng/inputnumber';
import { FileUpload } from 'primeng/fileupload';
import { FileDetails } from '../../../../../models/file-details';
import { RefTable } from '../../../../../models/ref-table';
import { AutoCompleteSelectEvent } from 'primeng/autocomplete';
import { TextareaModule } from 'primeng/textarea';
import { ConfirmDialogModule } from 'primeng/confirmdialog';
import { DialogModule } from 'primeng/dialog';
import { ButtonModule } from 'primeng/button';
import { getFieldErrorFromFormBuilder } from '../../../../../utils/functions/helpers';
import { ConsInputService } from '../../../../../service/cons-input.service';
import { OutputService } from '../../../../../service/output.service';
import { MessageService } from 'primeng/api';
import { DialogDetails } from '../../../../../models/dialog-details';
import { PRODUCT_TRANSACTION_STATES } from '../../../../../utils/consts/states-consts';

@Component({
    selector: 'app-transaction-adding-form',
    imports: [
        CommonModule,
        FormsModule,
        ReactiveFormsModule,
        AutocompleteComponent,
        FloatLabel,
        DatePicker,
        InputTextModule,
        InputNumber,
        InputNumberModule,
        FileUpload,
        TextareaModule,
        ConfirmDialogModule,
        DialogModule,
        ButtonModule
    ],
    templateUrl: './transaction-adding-form.component.html',
    styleUrl: './transaction-adding-form.component.scss'
})
export class TransactionAddingFormComponent {
    
    @Input() dialogStatus!: DialogDetails;
    @Output() dialogStatusChange =  new EventEmitter<DialogDetails>();

    form!: FormGroup;
    productsAutocompleteDetails?: AutocompleteModel;
    soldByAutocompleteDetails?: AutocompleteModel;
    productsTransactionStatesAutocompleteDetails?: AutocompleteModel;
    consInputsLabelsAutocompleteDetails?: AutocompleteModel;
    consInputsCostsAutocompleteDetails?: AutocompleteModel;

    selectedRemainingInputQuantity?: number;

    constructor(
        private refTableService: RefTableService,
        private fb: FormBuilder,
        private consInputService: ConsInputService,
        private outputService: OutputService,
        private messageService: MessageService
    ) {
        debugger;
        this.setFormControls();
        this.getRefTableData();
    }

    setFormControls() {
        this.form = this.fb.group({
            product: [null, Validators.required],
            consInputLabel: [null, [Validators.required]],
            consInputCost: [null, [Validators.required]],
            price: [null, [Validators.required]],
            quantity: [1, [Validators.required, Validators.min(1)]],
            soldBy: [''],
            state: [null, Validators.required],
            transactionDate: [new Date(), Validators.required],
            transactionTime: [new Date(), Validators.required],
            description: ['']
        });
    }

    getRefTableData() {
        this.refTableService.getProductsRefTable().subscribe(
            data => {
                this.productsAutocompleteDetails = new AutocompleteModel("productsRefTable", "Product *", data, undefined, true);
            }
        );
        this.refTableService.getUsersRefTable().subscribe(
            data => {
                this.soldByAutocompleteDetails = new AutocompleteModel("soldByRefTable", "Sold by", data, undefined, undefined);
            }
        );

        this.refTableService.getProductsTransactionStatesRefTable().subscribe(
            data => {
                data = data.filter(item => item.id === PRODUCT_TRANSACTION_STATES.RESERVED || item.id === PRODUCT_TRANSACTION_STATES.SOLD);
                const selectedValue = data.find(item => item.id === PRODUCT_TRANSACTION_STATES.SOLD);
                this.form.get('state')!.setValue(selectedValue);
                this.productsTransactionStatesAutocompleteDetails = new AutocompleteModel("tranState", "State *", data, undefined, undefined);
            }
        );
        this.searchConsInputs(undefined);
    }

    searchConsInputs(productLabel: string | undefined) {
        this.consInputService.searchConsInputs(productLabel).subscribe(
            (data: any) => {
                const selectedPriceExistsInList: boolean = data.some((item: any) => item.id === this.form.get('consInputLabel')!.value?.id);
                const selectedValue: RefTable | undefined = selectedPriceExistsInList? this.form.get('consInputLabel')!.value: undefined;
                this.form.get('consInputLabel')!.setValue(selectedValue);
                this.form.get('consInputCost')!.setValue(selectedValue);
                this.consInputsLabelsAutocompleteDetails = new AutocompleteModel("consInputsRefTable", "Input *", data, undefined, undefined);
                this.consInputsCostsAutocompleteDetails = new AutocompleteModel("consInputsRefTable", "Input cost *", data, 'cost', undefined);
            }
        );
    }

    selectProduct(value: RefTable | undefined) {
        this.searchConsInputs(value?.label);
        debugger;
    }

    selectConsInput(value: any) {
        debugger;
        const consInputLabelFormControl = this.form.get('consInputLabel');
        const consInputCostFormControl = this.form.get('consInputCost');
        this.selectedRemainingInputQuantity = value?.remainingQuantity;
        const oldQuantityControl = this.form.get('quantity');

        //On traite le cas où la nouvelle quantité restante dans les inputs est plus petite par rapport à ce qui est saisi dans le champ de la quantité. 
        if(this.selectedRemainingInputQuantity && this.selectedRemainingInputQuantity<oldQuantityControl!.value)
            oldQuantityControl!.setValue(undefined);

        if(consInputLabelFormControl) {
            console.log("consInputLabelFormControl: ", consInputCostFormControl!.value);

            //J'ai ajouté ces deux lignes parce que si on modifie price, il faut que cons input prenne la valeur, même chose pour l'inverse.
            consInputLabelFormControl.setValue(value);
            consInputCostFormControl!.setValue(value);
            if(value && this.consInputsLabelsAutocompleteDetails) {
                debugger;
                const selectedProduct = this.productsAutocompleteDetails?.values.find(item => item.id === value.idProduct);
                if(selectedProduct && this.productsAutocompleteDetails && this.form.get('product')!.value !== selectedProduct) {
                    this.form.get('product')?.setValue(selectedProduct);
                    const consInputBySelectedProduct: RefTable[] = this.consInputsLabelsAutocompleteDetails.values.filter((item: any) => item.idProduct === selectedProduct.id);
                    this.consInputsLabelsAutocompleteDetails.values = consInputBySelectedProduct;
                    this.consInputsCostsAutocompleteDetails!.values = consInputBySelectedProduct;
                }
            }
        }
    }

    cancelOutputAdding() {
        this.dialogStatus = new DialogDetails(undefined, false, false);
        this.dialogStatusChange.emit(this.dialogStatus);
    }
    
    onSubmit() {
        debugger;
        if (this.form.invalid) {
            this.form.markAllAsTouched(); // force Angular à afficher les erreurs
            return;
        }
        else {
            let productId: number = this.form.get('product')!.value?.id;
            let consInputId: number = this.form.get('consInputLabel')!.value?.id;
            let consInputLabel: string | undefined = this.form.get('consInputLabel')!.value?.label;
            let price: number = this.form.get('price')!.value;
            let quantity: number = this.form.get('quantity')!.value;
            let soldById: number | undefined = this.form.get('soldBy')!.value?.id;
            let stateId: number = this.form.get('state')!.value?.id;
            let transactionDate: Date = this.form.get('transactionDate')!.value;
            let transactionTime: Date = this.form.get('transactionTime')!.value;
            let description: string | undefined = this.form.get('description')!.value;

            this.outputService.addOutput(productId, consInputId, consInputLabel, price, quantity, soldById, stateId, transactionDate, transactionTime, description)?.subscribe(
                {
                    next: (data) => {
                        this.dialogStatus = new DialogDetails(undefined, false, true);
                        this.dialogStatusChange.emit(this.dialogStatus);
                        this.messageService.add({ severity: 'success', summary: 'Transaction adding', detail: 'The output transaction has been recorded.' });
                    },
                    error: () => {

                    }
                }
            );
        }
    }

    getFieldError(fieldName: string) {
        const fieldControl = this.form.get(fieldName);
        return getFieldErrorFromFormBuilder(fieldControl);
    }
}
