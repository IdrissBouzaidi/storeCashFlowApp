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
import { TextareaModule } from 'primeng/textarea';
import { ConfirmDialogModule } from 'primeng/confirmdialog';
import { DialogModule } from 'primeng/dialog';
import { ButtonModule } from 'primeng/button';
import { atLeastOneRequiredValidator, getFieldErrorFromFormBuilder } from '../../../../../utils/functions/helpers';
import { DialogDetails } from '../../../../../models/dialog-details';
import { ChargeService } from '../../../../../service/charge.service';
import { MessageService } from 'primeng/api';
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
    chargeTypesAutocompleteDetails?: AutocompleteModel;
    consumedByAutocompleteDetails?: AutocompleteModel;
    selectedImageDetails?: FileDetails;
    periodsAutocompleteDetails?: AutocompleteModel;

    constructor(
        private refTableService: RefTableService,
        private chargeService: ChargeService,
        private fb: FormBuilder,
        private messageService: MessageService,
        private minIoService: MinIoService
    ) {
        this.setFormControls();
        this.getRefTableData();
    }

    setFormControls() {
        this.form = this.fb.group(
            {
                chargeType: [null],
                cost: [null, [Validators.required, Validators.min(0)]],
                shortDesc: [''],
                consumedBy: [null],
                transactionDate: [new Date(), Validators.required],
                transactionTime: [new Date(), Validators.required],
                quantity: [1, [Validators.required, Validators.min(1)]],
                period: [null, Validators.required],
                description: ['']
            },
            {
                validators: atLeastOneRequiredValidator(['chargeType', 'shortDesc'])
            }
        );
    }

    getRefTableData() {
        this.refTableService.getChargeTypesRefTable().subscribe(
            data => {
                this.chargeTypesAutocompleteDetails = new AutocompleteModel("chargeTypesRefTable", "Charge type *", data, undefined, undefined);
            }
        );
        this.refTableService.getUsersRefTable().subscribe(
            data => {
                this.consumedByAutocompleteDetails = new AutocompleteModel("consumedByRefTable", "Consumed by", data, undefined, undefined);
            }
        );
        this.refTableService.getPeriodsRefTable().subscribe(
            data => {
                const MAX_PERIOD_ID = Math.max(...data.map(item => item.id));
                const defaultPeriod: RefTable = data.find(item => item.id === MAX_PERIOD_ID)!;
                this.form.get('period')!.setValue(defaultPeriod);
                this.periodsAutocompleteDetails = new AutocompleteModel("periodsRefTable", "Period *", data, undefined, undefined);
            }
        );
    }

    selectChargeType(value: RefTable | undefined) {
        debugger;
        const val = this.form.get('chargeType');
        if(!this.form.get('shortDesc')?.value && value)
            this.form.get('shortDesc')?.setValue( (value as RefTable).label );
        debugger;
    }

    selectImage(event: any) {
        const file: File = event.files[0];
        const fileName: string = 'charge_' + new Date().getTime() + '.' + file.name.split('.').pop()?.toLowerCase();
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
            const chargeTypeId: number = this.form.get('chargeType')?.value?.id;
            const cost: number = this.form.get('cost')?.value;
            const shortDesc: string = this.form.get('shortDesc')?.value;
            const consumedBy: number | undefined = this.form.get('consumedBy')?.value?.id;
            const transactionDate: Date = this.form.get('transactionDate')?.value;
            const transactionTime: Date = this.form.get('transactionTime')?.value;
            const quantity: number = this.form.get('quantity')?.value;
            const periodId: number = this.form.get('period')?.value?.id;
            const description: string = this.form.get('description')?.value;
            const imageSrc: string | undefined = this.selectedImageDetails?.fileName;
            const addCharge$ = this.chargeService.addCharge(chargeTypeId, cost, shortDesc, consumedBy, transactionDate, transactionTime, quantity, periodId, description, imageSrc)?.pipe(
                    tap(
                        (data) => {
                            this.dialogStatus = new DialogDetails(undefined, false, true);
                            this.dialogStatusChange.emit(this.dialogStatus);
                            this.messageService.add({ severity: 'success', summary: 'Transaction adding', detail: 'The charge transaction has been recorded.' })
                        }
                    )
                );
                
            if(addCharge$) {

                if(this.selectedImageDetails) {
                    const updateImage$ = this.minIoService.updateImage(this.selectedImageDetails);
                    updateImage$?.pipe(
                        switchMap(() => addCharge$)
                    ).subscribe();
                }
                else {
                    addCharge$.subscribe();
                }
            }
        }
    }

    getFieldError(fieldName: string) {
        const fieldControl = this.form.get(fieldName);
        const fieldError: string = getFieldErrorFromFormBuilder(fieldControl);
        return fieldError;
    }
}
