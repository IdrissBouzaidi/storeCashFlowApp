import { Component } from '@angular/core';
import { forkJoin, Observable, tap } from 'rxjs';
import { AutocompleteComponent } from '../../../shared-components/autocomplete/autocomplete.component';
import { AutocompleteModel } from '../../../models/autocomplete-model';
import { RefTableService } from '../../../service/ref-table.service';
import { CommonModule } from '@angular/common';
import { DatePicker } from 'primeng/datepicker';
import { FloatLabel } from 'primeng/floatlabel';
import { FormBuilder, FormGroup, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { ButtonModule } from 'primeng/button';
import { DialogDetails } from '../../../models/dialog-details';
import { ProductsArrayComponent } from './components/products-array/products-array.component';
import { ProductService } from '../../../service/product.service';
import { Product } from '../../../models/product';
import { ProductAddingFormComponent } from './components/product-adding-form/product-adding-form.component';

@Component({
    selector: 'app-products',
    imports: [
        AutocompleteComponent,
        ProductsArrayComponent,
        ProductAddingFormComponent,
        CommonModule,
        ReactiveFormsModule,
        FloatLabel,
        DatePicker,
        ButtonModule
    ],
    templateUrl: './products.component.html',
    styleUrl: './products.component.scss'
})
export class ProductsComponent {

    form!: FormGroup;


    productStatesAutocompleteDetails?: AutocompleteModel;
    creatorAutocompleteDetails?: AutocompleteModel;
    categoriesAutocompleteDetails?: AutocompleteModel;

    productsList$?: Observable<Product[]>;

    searchButtonIsLoading: boolean = false;

    addingDialogStatus: DialogDetails = new DialogDetails(undefined, false, false);

    constructor(
        private refTableService: RefTableService,
        private productService: ProductService,
        private fb: FormBuilder
    ) {
        this.setFormControls();
        this.getRefTables();
        this.getData();
    }
    
    setFormControls() {
        this.form = this.fb.group({
            addingDateMin: [null],
            addingDateMax: [null],
            productState: [null],
            creator: [null],
            category: [null]
        });
    }

    private getRefTables() {

        // 🧾 Chargement des tables de référence en parallèle

        const getProductStatesRefTable$ = this.refTableService.getProductStatesRefTable()
            .pipe(
                tap(
                    data => this.productStatesAutocompleteDetails = 
                        new AutocompleteModel("productStatesRefTable", "Product states", data, undefined, undefined)
                )
            );

        const getCategoriesRefTable$ = this.refTableService.getCategoriesRefTable()
            .pipe(
                tap(
                    data => this.categoriesAutocompleteDetails = 
                        new AutocompleteModel("categoriesRefTable", "Categories", data, undefined, undefined)
                )
            );

        const getUsersRefTable$ = this.refTableService.getUsersRefTable()
            .pipe(
                tap(
                    data => this.creatorAutocompleteDetails = 
                        new AutocompleteModel("createdByRefTable", "Created by", data, undefined, undefined)
                )
            );

        // 🔁 Exécution parallèle de toutes les requêtes
        forkJoin([
            getProductStatesRefTable$,
            getCategoriesRefTable$,
            getUsersRefTable$
        ]).subscribe(
            () => {
                // ✅ Action exécutée une fois que toutes les refTables sont chargées
                this.getData();
            }
        );

    }

    public getData() {

        const addingDateMin: Date | undefined = this.form.get('addingDateMin')!.value;
        const addingDateMax: Date | undefined = this.form.get('addingDateMax')!.value;
        const stateId: number | undefined = this.form.get('productState')!.value?.id;
        const creatorId: number | undefined = this.form.get('creator')!.value?.id;
        const categoryId: number | undefined = this.form.get('category')!.value?.id;
        this.productsList$ = this.productService.getProductsList(addingDateMin, addingDateMax, stateId, creatorId, categoryId);
    }

    onAddButtonClick() {
        debugger
        this.addingDialogStatus = new DialogDetails(undefined, true, false);
    }

    onDialogStatusChanged(newStatus: DialogDetails) {
        this.addingDialogStatus = newStatus;
        if(newStatus.dataSaved) {
            this.getData();
        }
        debugger;
    }
}
