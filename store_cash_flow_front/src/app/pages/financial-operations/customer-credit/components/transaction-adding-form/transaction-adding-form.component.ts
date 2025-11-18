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
import { RefTable } from '../../../../../models/ref-table';
import { TextareaModule } from 'primeng/textarea';
import { ConfirmDialogModule } from 'primeng/confirmdialog';
import { DialogModule } from 'primeng/dialog';
import { ButtonModule } from 'primeng/button';
import { atLeastOneRequiredValidator, getFieldErrorFromFormBuilder } from '../../../../../utils/functions/helpers';
import { DialogDetails } from '../../../../../models/dialog-details';
import { MessageService } from 'primeng/api';
import { AdvanceService } from '../../../../../service/advance.service';
import { CustomerCreditService } from '../../../../../service/customer-credit.service';

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
    customerAutocompleteDetails?: AutocompleteModel;

    constructor(
        private refTableService: RefTableService,
        private customerCreditService: CustomerCreditService,
        private fb: FormBuilder,
        private messageService: MessageService
    ) {
        this.setFormControls();
        this.getRefTableData();
    }

    setFormControls() {
        this.form = this.fb.group(
            {
                amount: [null, [Validators.required]],
                customer: [null],
                description: [null],
                shortDesc: [null],
                creditDate: [new Date(), [Validators.required]],
                creditTime: [new Date(), [Validators.required]]
            },
            {
                validators: atLeastOneRequiredValidator(['customer', 'shortDesc'])
            }
        );
    }

    getRefTableData() {
        this.refTableService.getCustomersRefTable().subscribe(
            data => {
                this.customerAutocompleteDetails = new AutocompleteModel("customerRefTable", "Customer", data, undefined, undefined);
            }
        );
    }

    cancelInputAdding() {
        debugger
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
        
            const amount: number | undefined = this.form.get('amount')!.value;
            const customerId: number = this.form.get('customer')!.value?.id;
            const description: string | undefined = this.form.get('description')!.value;
            const shortDesc: string | undefined = this.form.get('shortDesc')!.value;
            const creditDate: Date = this.form.get('creditDate')!.value;
            const creditTime: Date = this.form.get('creditTime')!.value;

            this.customerCreditService.addCustomerCredit(amount, customerId, description, shortDesc, creditDate, creditTime).subscribe({
                next: (data) => {
                    this.dialogStatus = new DialogDetails(undefined, false, true);
                    this.dialogStatusChange.emit(this.dialogStatus);
                    this.messageService.add({ severity: 'success', summary: 'Transaction adding', detail: 'The customer credit transaction has been recorded.' });
                }
            });
        }
    }

    getFieldError(fieldName: string) {
        const fieldControl = this.form.get(fieldName);
        const fieldError: string = getFieldErrorFromFormBuilder(fieldControl);
        return fieldError;
    }
}
