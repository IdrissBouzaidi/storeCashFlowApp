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
import { CapitalContributionService } from '../../../../../service/capital-contribution.service';

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
    contributorAutocompleteDetails?: AutocompleteModel;

    constructor(
        private refTableService: RefTableService,
        private capitalContributionService: CapitalContributionService,
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
                contributor: [null, [Validators.required]],
                description: [null],
                shortDesc: [null],
                contributionDate: [new Date(), [Validators.required]],
                contributionTime: [new Date(), [Validators.required]]
            }
        );
    }

    getRefTableData() {
        this.refTableService.getUsersRefTable().subscribe(
            data => {
                this.contributorAutocompleteDetails = new AutocompleteModel("contributorRefTable", "Contributor *", data, undefined, undefined);
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
            const contributorId: number | undefined = this.form.get('contributor')!.value?.id;
            const description: string | undefined = this.form.get('description')!.value;
            const shortDesc: string | undefined = this.form.get('shortDesc')!.value;
            const contributionDate: Date = this.form.get('contributionDate')!.value;
            const contributionTime: Date = this.form.get('contributionTime')!.value;

            this.capitalContributionService.addCapitalContribution(amount, contributorId, description, shortDesc, contributionDate, contributionTime).subscribe({
                next: (data) => {
                    this.dialogStatus = new DialogDetails(undefined, false, true);
                    this.dialogStatusChange.emit(this.dialogStatus);
                    this.messageService.add({ severity: 'success', summary: 'Transaction adding', detail: 'The capital contribution transaction has been recorded.' });
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
