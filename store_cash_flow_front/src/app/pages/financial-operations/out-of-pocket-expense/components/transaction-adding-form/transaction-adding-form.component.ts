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
import { OutOfPocketService } from '../../../../../service/out-of-pocket.service';

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
    borrowerAutocompleteDetails?: AutocompleteModel;

    constructor(
        private refTableService: RefTableService,
        private outOfPocketService: OutOfPocketService,
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
                borrower: [null, [Validators.required]],
                description: [null],
                shortDesc: [null],
                borrowingDate: [new Date(), [Validators.required]],
                borrowingTime: [new Date(), [Validators.required]]
            }
        );
    }

    getRefTableData() {
        this.refTableService.getUsersRefTable().subscribe(
            data => {
                this.borrowerAutocompleteDetails = new AutocompleteModel("borrowerRefTable", "Borrower *", data, undefined, undefined);
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
            const borrowerId: number = this.form.get('borrower')!.value!.id;
            const description: string | undefined = this.form.get('description')!.value;
            const shortDesc: string | undefined = this.form.get('shortDesc')!.value;
            const borrowingDate: Date = this.form.get('borrowingDate')!.value;
            const borrowingTime: Date = this.form.get('borrowingTime')!.value;

            this.outOfPocketService.addOutOfPocket(amount, borrowerId, description, shortDesc, borrowingDate, borrowingTime).subscribe(
                (data) => {
                    this.dialogStatus = new DialogDetails(undefined, false, true);
                    this.dialogStatusChange.emit(this.dialogStatus);
                    this.messageService.add({ severity: 'success', summary: 'Transaction adding', detail: 'The out of pocket transaction has been recorded.' });
                }
            );
        }
    }

    getFieldError(fieldName: string) {
        const fieldControl = this.form.get(fieldName);
        const fieldError: string = getFieldErrorFromFormBuilder(fieldControl);
        return fieldError;
    }
}
