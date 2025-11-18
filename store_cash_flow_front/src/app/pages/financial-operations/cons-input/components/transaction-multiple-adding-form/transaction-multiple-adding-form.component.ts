import { ChangeDetectorRef, Component, EventEmitter, Input, Output } from '@angular/core';
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
import { atLeastOneRequiredValidator, getFieldErrorFromFormBuilder, getUploadInfo } from '../../../../../utils/functions/helpers';
import { ConsInputService } from '../../../../../service/cons-input.service';
import { MessageService, SortMeta } from 'primeng/api';
import { DialogDetails } from '../../../../../models/dialog-details';
import { MinIoService } from '../../../../../service/min-io.service';
import { catchError, finalize, forkJoin, of, tap } from 'rxjs';
import { SelectButtonModule } from 'primeng/selectbutton';
import { Table, TableModule } from 'primeng/table';
import { SelectModule } from 'primeng/select';
import { ConsInput } from '../../../../../models/cons-input';
import { FieldDetails } from '../../../../../models/field-details';
import { TagModule } from 'primeng/tag';
import { convertDateAndTimeToLisibleString } from '../../../../../utils/functions/date-converer';

@Component({
    selector: 'app-transaction-multiple-adding-form',
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
        SelectButtonModule,
        
        TableModule,
        SelectModule,
        TagModule
    ],
    templateUrl: './transaction-multiple-adding-form.component.html',
    styleUrl: './transaction-multiple-adding-form.component.scss'
})
export class TransactionMultipleAddingFormComponent {
    
    @Input() dialogStatus!: DialogDetails;
    @Output() dialogStatusChange =  new EventEmitter<DialogDetails>();

    form!: FormGroup;
    headerForm!: FormGroup;
    initialFormValues!: ConsInput;

    multiSortMeta: SortMeta[] = [];
    attributesCodes: string[] = [];
    selectedData?: ConsInput[];
    attributsDetailsMap!: { [key: string]: FieldDetails };
    
    sourceData: ConsInput[] = [];
    data!: ConsInput[];
    headerDataLine: any;

    imagesDetailsMap: { [key: string]: FileDetails } = {};
    tableIsLoading = false;
    submitIsLoading = false;

    saveWithoutCheckingImagesDialogIsVisible = false;

    constructor(
        private refTableService: RefTableService,
        private fb: FormBuilder,
        private minIoService: MinIoService,
        private messageService: MessageService,
        private consInputService: ConsInputService
    ) {
        this.headerDataLine = new ConsInput();
        this.initAttributsDetails();
        this.getRefTableData();
        this.data = this.sourceData;
    }
    

    initAttributsDetails() {
        this.attributsDetailsMap = {
            idProduct: { libelle: 'Product', type: 'autoComplete', listCode: 'idProduct', formControl: [null] },
            cost: { libelle: 'Cost', type: 'numeric', isMontant:true, formControl: [null, [Validators.required, Validators.min(0)]], required: true, isDecimal: true },
            label: { libelle: 'Short description', type: 'text', formControl: [''] },
            transactionDate: { libelle: 'Transaction date', type: 'date', formControl: [new Date(), Validators.required], required: true },
            transactionTime: { libelle: 'Transaction time', type: 'time', formControl: [new Date(), Validators.required], required: true },
            quantity: { libelle: 'Quantity', type: 'numeric', formControl: [1, [Validators.required, Validators.min(1)]], required: true },
            description: { libelle: 'Description', type: 'text', formControl: [''] },
            image: { libelle: 'Image', type: 'image' },
            receipt: { libelle: 'Receipt', type: 'image' }
        }

        this.attributesCodes = Object.keys(this.attributsDetailsMap);
        
        this.multiSortMeta = [
            { field: 'id', order: -1 }
        ];

        let controlsMap: any = {};
        let headerControlsMap: any = {};
        this.attributesCodes.forEach(code => {
            controlsMap[code] = this.attributsDetailsMap[code]?.formControl;
            headerControlsMap[code]=[null];
        });

        
        this.form = this.fb.group(controlsMap,
            {
                updateOn: 'submit',
                validators: atLeastOneRequiredValidator(['idProduct', 'shortDesc'])
            }
        );
        this.headerForm = this.fb.group(headerControlsMap);
        this.initialFormValues = this.form.getRawValue();

    }

    getRefTableData() {
        this.refTableService.getProductsRefTable().subscribe(
            data => {
                this.attributsDetailsMap['idProduct'].autoCompleteDetails = new AutocompleteModel("productsRefTable", "Product *", data, undefined, undefined);
            }
        );
    }

    selectProduct(value: RefTable | undefined) {
        if(!this.form.get('shortDesc')?.value && value)
            this.form.get('shortDesc')?.setValue( (value as RefTable).label );
    }

    cancelInputAdding() {
        this.dialogStatus = new DialogDetails(undefined, false, false);
        this.dialogStatusChange.emit(this.dialogStatus);
    }

    addEmptyLine() {
        debugger
        this.data;
        const newLineId: number = this.sourceData.length;
        const newConsInput: any = new ConsInput(newLineId);
        this.attributesCodes.forEach(code => {
            debugger;
            newConsInput[code] = (this.initialFormValues as any) [code];
        });
        this.sourceData.push(newConsInput);
        this.data = [...this.sourceData];
    }

    deleteLine(line: ConsInput) {
        const lineIndex: number = this.sourceData.indexOf(line);
        this.sourceData.splice(lineIndex, 1);
        this.data = [...this.sourceData];
    }

    onInputChange(value: any, item: any, code: string) {
        debugger
        this.selectedData;
        item[code] = value;
        this.form.markAsUntouched();
    }

    onRowClick(item: any) {
        debugger
        for(let code of this.attributesCodes) {
            let itemValue;
            if(this.attributsDetailsMap[code].type === 'autoComplete')
                itemValue = this.attributsDetailsMap[code].autoCompleteDetails?.valuesMap[item[code]];
            else
                itemValue = item[code];
            this.form.get(code)!.setValue(itemValue);
        }
        debugger
        this.data.forEach((item: any) => item.edition = false);
        item.edition = true;
    }

    onPageClick(event: any) {
        debugger
        event.stopPropagation();
        const path = event.composedPath() as HTMLElement[];
        const isClickedInsideRow = path.some(el => (el as HTMLElement).tagName === 'TR');

        const isClickedInsideCheckbox = path.some(el => 
            (el as HTMLElement).tagName === 'P-CHECKBOX' || 
            (el as HTMLElement).classList?.contains('p-checkbox')
        );
        if(isClickedInsideCheckbox || !isClickedInsideRow)
            this.data.forEach((item: any) => item.edition = false);
    }

    onImageSelected(event: any, item: any, code: string) {
        const file: File = event.files[0];
        const fileName: string = code + '_' + convertDateAndTimeToLisibleString(new Date())+ '.' + file.name.split('.').pop()?.toLowerCase();

        const newImageDetails = new FileDetails(file, fileName);
        const newKey: number = Object.keys(this.imagesDetailsMap).length;
        this.imagesDetailsMap[newKey] = newImageDetails;
        item[code] = { imageDetailsKey: newKey };

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

    cancelImage(item: any, code: string) {
        debugger
        delete this.imagesDetailsMap[item[code].imageDetailsKey];
        item[code] = undefined;
    }

    clear(table: Table) {
        table.clear();
        this.data = [...this.sourceData];
    }

    isDate(date: any) {
        return date instanceof Date;
    }

    onHeaderInputSubmit(code: string) {
        debugger;
        this.headerDataLine;
        this.selectedData?.forEach((item: any) => {
            item[code] = this.headerDataLine[code];
        });
    }
    
    onSubmit(checkImages: boolean) {

        this.submitIsLoading = true;
        let allImagesHasBeenUploaded = true;
        //Vérification s'il y a une cellule qui n'est pas valide.
        for(let item of this.data) {
            this.onRowClick(item);
            (item as any).edition = false;
            if (this.form.invalid) {
                (item as any).edition = true;
                this.form.markAllAsTouched(); // force Angular à afficher les erreurs
                this.submitIsLoading = false;
                return;
            }
        }

        const addConsInputList$ = this.consInputService.addConsInputList(this.sourceData, this.imagesDetailsMap)?.pipe(
            tap(
                (data => {
                    this.submitIsLoading = false;
                    this.dialogStatus = new DialogDetails(undefined, false, true);
                    this.dialogStatusChange.emit(this.dialogStatus);
                    this.messageService.add({ severity: 'success', summary: 'Transactions adding', detail: 'The consumable input transactions have been recorded.' });
                })
            ),
            finalize(() => {
                this.submitIsLoading = false;
            })
        );
        
        if(checkImages) {

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
            debugger
            //Le cas où aucune image n'a été sélectionnée.
            if(Object.keys(this.imagesDetailsMap).length === 0) {
                addConsInputList$.subscribe();
            }
            else
                forkJoin(updateImages$).subscribe(
                    data => {

                        debugger
                        //Donner l'information dans le cas où l'upload d'une certaine image a été échoué
                        for(const key of Object.keys(this.imagesDetailsMap)) {
                            if(this.imagesDetailsMap[key].uploadInfo.severity === 'danger') {
                                allImagesHasBeenUploaded = false;
                                break;
                            }
                        }
                        if(allImagesHasBeenUploaded) {
                            addConsInputList$.subscribe();
                        }
                        else {
                            this.messageService.add({
                                severity: 'warn',
                                summary: 'Upload warning',
                                detail: 'Some images were not successfully uploaded.'
                            });
                            this.submitIsLoading = false;
                            this.saveWithoutCheckingImagesDialogIsVisible = true;
                        }
                    }
                );

        }
        else {
            this.saveWithoutCheckingImagesDialogIsVisible = false;
            addConsInputList$.subscribe();
        }
    }

    getFieldError(fieldName: string) {
        const fieldControl = this.form.get(fieldName);
        return getFieldErrorFromFormBuilder(fieldControl);
    }
}
