import { Component, EventEmitter, Input, Output } from '@angular/core';
import { FloatLabel } from 'primeng/floatlabel';
import { DatePicker } from 'primeng/datepicker';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { InputTextModule } from 'primeng/inputtext';
import { InputNumber, InputNumberModule } from 'primeng/inputnumber';
import { FileUpload } from 'primeng/fileupload';
import { TextareaModule } from 'primeng/textarea';
import { ConfirmDialogModule } from 'primeng/confirmdialog';
import { DialogModule } from 'primeng/dialog';
import { ButtonModule } from 'primeng/button';
import { MessageService } from 'primeng/api';
import { AutocompleteComponent } from '../../../../shared-components/autocomplete/autocomplete.component';
import { DialogDetails } from '../../../../models/dialog-details';
import { AutocompleteModel } from '../../../../models/autocomplete-model';
import { getFieldErrorFromFormBuilder } from '../../../../utils/functions/helpers';
import { FinancialPeriodService } from '../../../../service/financial-period.service';

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
    takerAutocompleteDetails?: AutocompleteModel;
    periodsAutocompleteDetails?: AutocompleteModel;

    constructor(
        private financialPeriodService: FinancialPeriodService,
        private fb: FormBuilder,
        private messageService: MessageService
    ) {
        this.setFormControls();
    }

    setFormControls() {
        this.form = this.fb.group(
            {
                label: [null, [Validators.required]],
                description: [null],
                startDate: [new Date()],
                startTime: [new Date()]
            }
        );
    }

    cancelInputAdding() {
        this.dialogStatus = new DialogDetails(undefined, false, false);
        this.dialogStatusChange.emit(this.dialogStatus);
    }

    
    onSubmit() {
        if (this.form.invalid) {
            this.form.markAllAsTouched(); // force Angular à afficher les erreurs
            return;
        }
        else {

            const label: string = this.form.get('label')!.value;
            const startDate: Date | undefined = this.form.get('startDate')!.value;
            const startTime: Date | undefined = this.form.get('startTime')!.value;
            const description: string | undefined = this.form.get('description')!.value;


            this.financialPeriodService.addFinancialPeriod(label, startDate, startTime, description).subscribe({
                next: (data) => {
                    this.dialogStatus = new DialogDetails(undefined, false, true);
                    this.dialogStatusChange.emit(this.dialogStatus);
                    this.messageService.add({ 
                        severity: 'success', 
                        summary: 'Financial Period Creation', 
                        detail: 'The financial period has been successfully created.' 
                    });
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
