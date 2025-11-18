import { Component, EventEmitter, Input, OnChanges, Output, SimpleChanges, ViewChild } from '@angular/core';
import { finalize, forkJoin, Observable, tap } from 'rxjs';
import { Table, TableModule } from 'primeng/table';
import { DialogModule } from 'primeng/dialog';
import { ButtonModule } from 'primeng/button';
import { CommonModule } from '@angular/common';
import { Menu } from 'primeng/menu';
import { MenuItem, MessageService, SortMeta } from 'primeng/api';
import { FieldDetails } from '../../../../../models/field-details';
import { convertNumberToFrNumber } from '../../../../../utils/functions/number-converter';
import { convertServiceDateToDateObject, convertServiceTimeToTimeObject } from '../../../../../utils/functions/date-converer';
import { RefTable } from '../../../../../models/ref-table';
import { RefTableService } from '../../../../../service/ref-table.service';
import { convertListToMapObject } from '../../../../../utils/functions/helpers';
import { MinIoService } from '../../../../../service/min-io.service';
import { ApiResponse } from '../../../../../models/api-response';
import { Product } from '../../../../../models/product';
import { PRODUCT_STATES } from '../../../../../utils/consts/states-consts';
import { ProductService } from '../../../../../service/product.service';

@Component({
    selector: 'app-products-array',
    imports: [
        Menu,
        DialogModule,
        ButtonModule,
        TableModule,
        CommonModule
    ],
    templateUrl: './products-array.component.html',
    styleUrl: './products-array.component.scss'
})
export class ProductsArrayComponent implements OnChanges {
    refTablesMap: { [code: string]: { [id: number]: RefTable } } = {};

    attributesCodes: string[] = [];
    attributsDetailsMap!: { [key: string]: FieldDetails };
    multiSortMeta: SortMeta[] = [];

    @Input() dataList$?: Observable<Product[]>;
    sourceData: Product[] = [];
    data: Product[] = [];
    dataTableIsShown: boolean = false;
    imagesMinIoUrlMaps: {[key: string]: string} = {};
    
    clickedMenuLine?: Product;
    selectedLine?: Product;
    
    dataIsLoading = true;
    
    @ViewChild('menu') menu!: Menu;
    items: MenuItem[] | undefined;

    cancelProductDialogIsVisible: boolean = false;
    cancelDialogIsLoading: boolean = false;

    @Output() onAddButtonClick: EventEmitter<void> = new EventEmitter();

    constructor(
        private refTableService: RefTableService,
        private minIoService: MinIoService,
        private messageService: MessageService,
        private productService: ProductService
    ) {
        this.initAttributsDetails();
        this.initMenuItems();
        this.getRefTables();
    }

    ngOnChanges(changes: SimpleChanges): void {
        if(changes['dataList$'] && !changes['dataList$'].firstChange) {
            this.getData();
        }
    }

    initAttributsDetails() {        
        this.attributsDetailsMap = {
            id: { libelle: 'Id', type: 'numeric' },
            label: { libelle: 'Label', type: 'text' },
            imageSrc: { libelle: 'Image', type: 'image' },
            stateId: { libelle: 'State', type: 'list', listCode: 'prudctStates' },
            addingDate: { libelle: 'Adding date', type: 'date' },
            addingTime: { libelle: 'Adding time', type: 'time' },
            details: { libelle: 'Details', type: 'text' },
            createdBy: { libelle: 'Created by', type: 'list', listCode: 'createdBy' }
        };

        this.attributesCodes = Object.keys(this.attributsDetailsMap);
        
        this.multiSortMeta = [
            { field: 'id', order: -1 }
        ];

    }

    getRefTables() {
        const getProductStatesRefTable$ = this.refTableService.getProductStatesRefTable()
            .pipe(
                tap(
                    data => this.refTablesMap['prudctStates'] = convertListToMapObject(data)
                )
            );

        const getUsersRefTable$ = this.refTableService.getUsersRefTable()
            .pipe(
                tap(
                    data => this.refTablesMap['createdBy'] = convertListToMapObject(data)
                )
            );

        // 🔁 Exécution parallèle de toutes les requêtes
        forkJoin([
            getProductStatesRefTable$,
            getUsersRefTable$
        ]).subscribe(
            () => {
                // ✅ Action exécutée une fois que toutes les refTables sont chargées
                this.getData();
            }
        );
    }

    getData() {
        
        this.dataIsLoading = true;

        this.dataList$?.subscribe(
            {
                next: (data: Product[]) => {
                    this.dataTableIsShown = true;
                    this.sourceData = data;
                    this.data = data;
                    this.data.forEach(
                        (item: any) => {
                            for(const key in this.attributsDetailsMap) {
                                if(this.attributsDetailsMap[key].isMontant) {
                                    item[key] = convertNumberToFrNumber(item[key])
                                }
                                else if(this.attributsDetailsMap[key].type === 'date' && item[key]) {
                                    item[key] = convertServiceDateToDateObject(item[key]);
                                }
                                else if(this.attributsDetailsMap[key].type === 'time' && item[key]) {
                                    item[key] = convertServiceTimeToTimeObject(item[key]);
                                }
                            }
                        }
                    );

                    this.getImagesUrls();
                    this.dataIsLoading = false;
                },
                error: (error) => {
                    this.dataTableIsShown = false;
                    this.dataIsLoading = false;
                }
            });
    }

    getImagesUrls() {
        const imagesSrcs: string[] = this.data.map(item => item.imageSrc)
                                                .filter(item => {
                                                    return item !== undefined
                                                    }
                                                ).sort();
        if(imagesSrcs.length === 0)
            return;
        this.minIoService.getImagesUrls(imagesSrcs).subscribe(
            (data: ApiResponse<RefTable[]>) => {
                data.data?.forEach(
                    (item: RefTable) => {
                        if(item.code && item.label)
                            this.imagesMinIoUrlMaps[item.code] = item.label;
                    }
                );
            }
        );
    }

    clear(table: Table) {
        table.clear();
        this.data = this.sourceData;
    }
    
    initMenuItems() {
        this.items = [
            {
                id: 'details',
                label: 'Details',
                command: () => this.onMenuItemClick('details')
            },
            {
                id: 'cancel',
                label: 'Cancel',
                command: () => this.onMenuItemClick('cancel')
            },
            {
                id: 'edit',
                label: 'Edit',
                command: () => this.onMenuItemClick('edit')
            }
        ];
    }

    onMenuItemClick(itemName: 'details' | 'cancel' | 'edit') {
        switch (itemName) {
            case 'details':
                break;
            case 'cancel':
                this.cancelProductDialogIsVisible = true;
                break;
            case 'edit':
                break;
        }
    }

    toggleMenu(item: Product, event: Event) {
        this.clickedMenuLine = item;
        const cancelMenuItem = this.items!.find(item => item.id === 'cancel')!;
        cancelMenuItem.disabled = !(item.stateId === PRODUCT_STATES.ACTIVE);
        this.menu.toggle(event);
    }

    
    
    onConfirmCancelProduct() {
        this.cancelDialogIsLoading = true;
        this.productService.cancelProduct(this.clickedMenuLine!.id!)
            .pipe(
                finalize(() => {
                    this.cancelProductDialogIsVisible = false;
                    this.cancelDialogIsLoading = false;
                })
            ).subscribe(
            data => {
                this.messageService.add({
                    severity: 'success',
                    summary: 'Product Cancellation',
                    detail: 'The selected product has been successfully canceled.'
                });
                this.getData();
            }
        );
    }

    isDate(date: any) {
        return date instanceof Date;
    }

}
