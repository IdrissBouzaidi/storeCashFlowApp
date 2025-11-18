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
import { atLeastOneRequiredValidator, getFieldErrorFromFormBuilder } from '../../../../../utils/functions/helpers';
import { ConsInputService } from '../../../../../service/cons-input.service';
import { MessageService } from 'primeng/api';
import { DialogDetails } from '../../../../../models/dialog-details';
import { NotConsInputService } from '../../../../../service/not-cons-input.service';
import { switchMap, tap } from 'rxjs';
import { MinIoService } from '../../../../../service/min-io.service';

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

    reusableInputsAutocompleteDetails?: AutocompleteModel;
    contributorAucompleteDetails?: AutocompleteModel;
    selectedImageDetails?: FileDetails;

    constructor(
        private refTableService: RefTableService,
        private fb: FormBuilder,
        private notConsInputService: NotConsInputService,
        private messageService: MessageService,
        private minIoService: MinIoService
    ) {
        this.setFormControls();
        this.getRefTableData();
    }

    setFormControls() {
        this.form = this.fb.group({
            reusableInput: [null],
            cost: [null, [Validators.required, Validators.min(0)]],
            quantity: [1, [Validators.required, Validators.min(1)]],
            contributor: [null, [Validators.required]],
            shortDesc: ['', ],
            transactionDate: [new Date(), Validators.required],
            transactionTime: [new Date(), Validators.required],
            description: ['']
        },
        {
            validators: atLeastOneRequiredValidator(['reusableInput', 'shortDesc'])
        }
        );
    }

    getRefTableData() {
        this.refTableService.getReusableInputsRefTable().subscribe(
            data => {
                this.reusableInputsAutocompleteDetails = new AutocompleteModel("reusableInputsRefTable", "Reusable input *", data, undefined, undefined);
            }
        );
        this.refTableService.getUsersRefTable().subscribe(
            data => {
                this.contributorAucompleteDetails = new AutocompleteModel("contributorRefTable", "Contributor *", data, undefined, undefined);
            }
        );
    }

    selectReusableInput(value: RefTable | undefined) {
        if(!this.form.get('shortDesc')!.value) {
            const reusableInputValue: string | undefined = this.form.get('reusableInput')!.value?.label;
            if(reusableInputValue) {
                this.form.get('shortDesc')!.setValue(reusableInputValue);
            }
        }
    }

    selectImage(event: any) {
        const file: File = event.files[0];
        const fileName: string = 'not_cons_input_' + new Date().getTime() + '.' + file.name.split('.').pop()?.toLowerCase();
        this.selectedImageDetails = new FileDetails(file, fileName);
        if(this.selectedImageDetails.file) {
            const reader = new FileReader();
            reader.onload = (e) => {
                if(this.selectedImageDetails) {
                    this.selectedImageDetails.fileInternalUrl = e.target?.result;
                }
            }
            reader.readAsDataURL(this.selectedImageDetails.file);
        }
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
            const reusblaInputId: number | undefined = this.form.get('reusableInput')!.value?.id;
            const cost: number = this.form.get('cost')!.value;
            const quantity: number = this.form.get('quantity')!.value;
            const contributorId: number | undefined = this.form.get('contributor')!.value?.id;
            const shortDescr: string | undefined = this.form.get('shortDesc')!.value;
            const transactionDate: Date = this.form.get('transactionDate')!.value;
            const transactionTime: Date = this.form.get('transactionTime')!.value;
            const description: string | undefined = this.form.get('description')!.value;
            const srcImage: string | undefined = this.selectedImageDetails?.fileName;
            const addNotConsInput$ = this.notConsInputService.addNotConsInput(reusblaInputId, cost, quantity, contributorId, shortDescr, transactionDate, transactionTime, description, srcImage).pipe(
                tap(data => {
                    this.dialogStatus = new DialogDetails(undefined, false, true);
                    this.dialogStatusChange.emit(this.dialogStatus);
                    this.messageService.add({ severity: 'success', summary: 'Transaction adding', detail: 'The not consumable input transaction has been recorded.' });
                })
            );

            if(addNotConsInput$) {

                if(this.selectedImageDetails) {
                    const updateImage$ = this.minIoService.updateImage(this.selectedImageDetails);
                    updateImage$?.pipe(
                        switchMap(() => addNotConsInput$)
                    ).subscribe();
                }
                else {
                    addNotConsInput$.subscribe();
                }
            }
        }
    }

    getFieldError(fieldName: string) {
        const fieldControl = this.form.get(fieldName);
        return getFieldErrorFromFormBuilder(fieldControl);
    }
}
