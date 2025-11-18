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
import { getFieldErrorFromFormBuilder, getUploadInfo } from '../../../../../utils/functions/helpers';
import { ConsInputService } from '../../../../../service/cons-input.service';
import { MessageService } from 'primeng/api';
import { DialogDetails } from '../../../../../models/dialog-details';
import { MinIoService } from '../../../../../service/min-io.service';
import { catchError, finalize, forkJoin, of, switchMap, tap } from 'rxjs';
import { SelectButtonModule } from 'primeng/selectbutton';

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
        ButtonModule,
        SelectButtonModule
    ],
    templateUrl: './transaction-adding-form.component.html',
    styleUrl: './transaction-adding-form.component.scss'
})
export class TransactionAddingFormComponent {
    
    @Input() dialogStatus!: DialogDetails;
    @Output() dialogStatusChange =  new EventEmitter<DialogDetails>();

    form!: FormGroup;
    productsAutocompleteDetails?: AutocompleteModel;
    periodsAutocompleteDetails?: AutocompleteModel;

    selectedAddingModeValue: string = 'Idriss';
    addingModes!: RefTable[];

    submitIsLoading: boolean = false;
    
    imagesDetailsMap: { [key: string]: FileDetails } = {};

    constructor(
        private refTableService: RefTableService,
        private fb: FormBuilder,
        private consInputService: ConsInputService,
        private messageService: MessageService,
        private minIoService: MinIoService
    ) {
        this.addingModes = [
            { id: 0, label: 'Idriss' },
            {  id: 1, label: 'Bouzaidi' }
        ];
        this.setFormControls();
        this.getRefTableData();
    }

    setFormControls() {
        this.form = this.fb.group({
            product: [null, Validators.required],
            cost: [null, [Validators.required, Validators.min(0)]],
            shortDesc: ['', Validators.required],
            transactionDate: [new Date(), Validators.required],
            transactionTime: [new Date(), Validators.required],
            quantity: [1, [Validators.required, Validators.min(1)]],
            period: [null, Validators.required],
            description: [''],
            imageInput: [null],
            imageReceipt: [null]
        });
    }

    getRefTableData() {
        this.refTableService.getProductsRefTable().subscribe(
            data => {
                this.productsAutocompleteDetails = new AutocompleteModel("productsRefTable", "Product *", data, undefined, undefined);
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

    selectImage(event: any, code: string) {
        const file: File = event.files[0];
        const fileName: string = code + '_' + new Date().getTime() + '.' + file.name.split('.').pop()?.toLowerCase();
        
        const newImageDetails = new FileDetails(file, fileName);
        const newKey: number = Object.keys(this.imagesDetailsMap).length;
        this.imagesDetailsMap[newKey] = newImageDetails;
        this.form.get(code)!.setValue({ imageDetailsKey: newKey });

        if(newImageDetails.file) {
            const reader = new FileReader();
            reader.onload = (e) => {
                if(newImageDetails) {
                    newImageDetails.fileInternalUrl = e.target?.result;
                }
            }
            reader.readAsDataURL(newImageDetails.file);
        }
    }

    selectProduct(value: RefTable | undefined) {
        if(!this.form.get('shortDesc')?.value && value)
            this.form.get('shortDesc')?.setValue( (value as RefTable).label );
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
            
            const productId: number = this.form.get('product')?.value?.id;
            const cost: number = this.form.get('cost')?.value;
            const shortDescr: string = this.form.get('shortDesc')?.value;
            const transactionDate: Date = this.form.get('transactionDate')?.value;
            const transactionTime: Date = this.form.get('transactionTime')?.value;
            const quantity: number = this.form.get('quantity')?.value;
            const periodId: number = this.form.get('period')?.value?.id;
            const description: string = this.form.get('description')?.value;
            const inputImageKey: number = this.form.get('imageInput')!.value?.imageDetailsKey;
            const inputImageSrc: string | undefined = (inputImageKey !== undefined)? this.imagesDetailsMap[inputImageKey].fileName: undefined;

            const receiptImageKey: number = this.form.get('imageReceipt')!.value?.imageDetailsKey;
            const receiptImageSrc: string | undefined = (receiptImageKey !== undefined)? this.imagesDetailsMap[receiptImageKey].fileName: undefined;
            const addConsInput$ = this.consInputService.addConsInput(productId, cost, shortDescr, transactionDate, transactionTime, quantity, periodId, description, inputImageSrc, receiptImageSrc)?.pipe(
                tap(
                    (data => {
                        this.dialogStatus = new DialogDetails(undefined, false, true);
                        this.dialogStatusChange.emit(this.dialogStatus);
                        this.messageService.add({ severity: 'success', summary: 'Transaction adding', detail: 'The consumable input transaction has been recorded.' });
                    })
                ),
                finalize(() => {
                    this.submitIsLoading = false;
                })
            );

        this.submitIsLoading = true;
        let allImagesHasBeenUploaded = true;


            
    
        //Créer les observables pour les images à charger.
        const updateImages$ = [];
        for(const key of Object.keys(this.imagesDetailsMap)) {
            const imageDetails = this.imagesDetailsMap[key];

            //Pour les images qui ont été déjà importés, on fait le skip.
            if(imageDetails.uploadInfo?.severity === 'success')
                continue;
            imageDetails.uploadInfo = getUploadInfo('uploading');
            const updateImage$ = this.minIoService.updateImage(imageDetails)
                                    ?.pipe(
                                        tap(() => {
                                            debugger
                                            imageDetails.uploadInfo = getUploadInfo('success');
                                        }),
                                        catchError((error) => {
                                            imageDetails.uploadInfo = getUploadInfo('error');
                                            return of({ error: true, image: imageDetails });
                                        })
                                    );
            updateImages$.push(updateImage$);
        }
        //Le cas où aucune image n'a été sélectionnée.
        if(Object.keys(this.imagesDetailsMap).length === 0) {
            addConsInput$?.subscribe();
        }
        else
            forkJoin(updateImages$).subscribe(
                data => {
                    //Donner l'information dans le cas où l'upload d'une certaine image a été échoué
                    for(const key of Object.keys(this.imagesDetailsMap)) {
                        if(this.imagesDetailsMap[key].uploadInfo.severity === 'danger') {
                            allImagesHasBeenUploaded = false;
                            break;
                        }
                    }
                    if(allImagesHasBeenUploaded) {
                        addConsInput$?.subscribe();
                    }
                    else {
                        this.messageService.add({
                            severity: 'warn',
                            summary: 'Upload warning',
                            detail: 'Some images were not successfully uploaded.'
                        });
                        this.submitIsLoading = false;
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
