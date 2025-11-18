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
import { getFieldErrorFromFormBuilder } from '../../../../../utils/functions/helpers';
import { DialogDetails } from '../../../../../models/dialog-details';
import { MessageService } from 'primeng/api';
import { AdvanceService } from '../../../../../service/advance.service';

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

    constructor(
        private refTableService: RefTableService,
        private advanceService: AdvanceService,
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
                taker: [null, [Validators.required]],
                description: [null],
                shortDesc: [null],
                advanceDate: [new Date(), [Validators.required]],
                advanceTime: [new Date(), [Validators.required]]
            }
        );
    }

    getRefTableData() {
        this.refTableService.getUsersRefTable().subscribe(
            data => {
                this.takerAutocompleteDetails = new AutocompleteModel("takerRefTable", "Taker *", data, undefined, undefined);
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
        
            const amount: number | undefined = this.form.get('amount')!.value;
            const takerId: number = this.form.get('taker')!.value!.id;
            const description: string | undefined = this.form.get('description')!.value;
            const shortDesc: string | undefined = this.form.get('shortDesc')!.value;
            const advanceDate: Date = this.form.get('advanceDate')!.value;
            const advanceTime: Date = this.form.get('advanceTime')!.value;

            this.advanceService.addAdvance(amount, takerId, description, shortDesc, advanceDate, advanceTime).subscribe({
                next: (data) => {
                    this.dialogStatus = new DialogDetails(undefined, false, true);
                    this.dialogStatusChange.emit(this.dialogStatus);
                    this.messageService.add({ severity: 'success', summary: 'Transaction adding', detail: 'The advance transaction has been recorded.' });
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
