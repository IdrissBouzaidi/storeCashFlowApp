import { Component, EventEmitter, Input, Output } from '@angular/core';
import { AutocompleteComponent } from '../../../../../shared-components/autocomplete/autocomplete.component';
import { FloatLabel } from 'primeng/floatlabel';
import { DatePicker } from 'primeng/datepicker';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { InputTextModule } from 'primeng/inputtext';
import { InputNumber, InputNumberModule } from 'primeng/inputnumber';
import { FileUpload } from 'primeng/fileupload';
import { FileDetails } from '../../../../../models/file-details';
import { TextareaModule } from 'primeng/textarea';
import { ConfirmDialogModule } from 'primeng/confirmdialog';
import { DialogModule } from 'primeng/dialog';
import { ButtonModule } from 'primeng/button';
import { getFieldErrorFromFormBuilder, getUploadInfo } from '../../../../../utils/functions/helpers';
import { DialogDetails } from '../../../../../models/dialog-details';
import { MessageService } from 'primeng/api';
import { catchError, finalize, forkJoin, of, tap } from 'rxjs';
import { MinIoService } from '../../../../../service/min-io.service';
import { ProductService } from '../../../../../service/product.service';
import { RefTableService } from '../../../../../service/ref-table.service';
import { AutocompleteModel } from '../../../../../models/autocomplete-model';

@Component({
    selector: 'app-product-adding-form',
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
    templateUrl: './product-adding-form.component.html',
    styleUrl: './product-adding-form.component.scss'
})
export class ProductAddingFormComponent {
    
    @Input() dialogStatus!: DialogDetails;
    @Output() dialogStatusChange =  new EventEmitter<DialogDetails>();

    categoriesAutocompleteDetails!: AutocompleteModel;

    form!: FormGroup;

    submitIsLoading: boolean = false;

    imagesDetailsMap: { [key: string]: FileDetails } = {};

    someCategoryIsSelected: boolean = false;

    constructor(
        private refTableService: RefTableService,
        private productService: ProductService,
        private fb: FormBuilder,
        private messageService: MessageService,
        private minIoService: MinIoService
    ) {
        this.setFormControls();
        this.getRefTables();
    }

    setFormControls() {
        this.form = this.fb.group(
            {
                shortDesc: ['', [Validators.required]],
                description: [''],
                category: [''],
                image: [null]
            }
        );
    }

    
    private getRefTables() {

        // 🧾 Chargement des tables de référence en parallèle

        const getCategoriesRefTable$ = this.refTableService.getCategoriesRefTable()
            .pipe(
                tap(
                    data => {
                        this.categoriesAutocompleteDetails = new AutocompleteModel("categoriesRefTable", "Categories", data, undefined, undefined)
                    }
                )
            );

        // 🔁 Exécution parallèle de toutes les requêtes
        forkJoin([
            getCategoriesRefTable$
        ]).subscribe(
            data => {
                this.categoriesAutocompleteDetails
                debugger
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

    cancelAdding() {
        debugger
        this.dialogStatus = new DialogDetails(undefined, false, false);
        this.dialogStatusChange.emit(this.dialogStatus);
    }

    addCategory() {
        debugger
        const selectedCategory = this.form.get('category')!.value;
        if(selectedCategory) {
            selectedCategory.selected = true;
            this.form.get('category')!.setValue(undefined);
            this.someCategoryIsSelected = true;
            this.categoriesAutocompleteDetails.values = this.categoriesAutocompleteDetails.initialValues.filter((item: any) => !item.selected);
        }
    }

    removeCategory(category: any) {
        category.selected = false;
        this.categoriesAutocompleteDetails.values = this.categoriesAutocompleteDetails.initialValues.filter((item: any) => !item.selected);
    }

    isCategorySelecteed(category: any) {
        return category?.selected;
    }

    
    onSubmit() {
        debugger;
        if (this.form.invalid) {
            this.form.markAllAsTouched(); // force Angular à afficher les erreurs
            return;
        }
        else {

            const shortDesc: string = this.form.get('shortDesc')!.value;
            const description: string = this.form.get('description')!.value;
            const imageKey: number = this.form.get('image')!.value?.imageDetailsKey;
            const imageSrc: string | undefined = (imageKey !== undefined)? this.imagesDetailsMap[imageKey].fileName: undefined;
            const categoryIdList: number[] | undefined = this.categoriesAutocompleteDetails.initialValues?.filter((item: any) => item.selected).map(item => item.id);
            const addProduct$ = this.productService.addProduct(shortDesc, description, imageSrc, categoryIdList)?.pipe(
                    tap(
                        (data) => {
                            this.dialogStatus = new DialogDetails(undefined, false, true);
                            this.dialogStatusChange.emit(this.dialogStatus);
                            this.messageService.add({ severity: 'success', summary: 'Product adding adding', detail: 'The product has been recorded.' })
                        }
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
                addProduct$?.subscribe();
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
                            addProduct$?.subscribe();
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
        const fieldError: string = getFieldErrorFromFormBuilder(fieldControl);
        return fieldError;
    }
}
