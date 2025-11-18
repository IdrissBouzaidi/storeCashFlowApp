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
import { Output as OutputModel } from '../../../../../models/output';
import { ApiResponse } from '../../../../../models/api-response';
import { MinIoService } from '../../../../../service/min-io.service';
import { OutputService } from '../../../../../service/output.service';
import { FinancialPeriodService } from '../../../../../service/financial-period.service';
import { FinancialPeriod } from '../../../../../models/financial-period';
import { FINANCIAL_PERIOD_STATES } from '../../../../../utils/consts/states-consts';

@Component({
    selector: 'app-output-array',
    imports: [
        Menu,
        DialogModule,
        ButtonModule,
        TableModule,
        CommonModule
    ],
    templateUrl: './output-array.component.html',
    styleUrl: './output-array.component.scss'
})
export class OutputArrayComponent implements OnChanges {
    refTablesMap: { [code: string]: { [id: number]: RefTable } } = {};

    attributesCodes: string[] = [];
    attributsDetailsMap!: { [key: string]: FieldDetails };
    multiSortMeta: SortMeta[] = [];

    @Input() dataList$?: Observable<OutputModel[]>;
    sourceData: OutputModel[] = [];
    data: OutputModel[] = [];
    dataTableIsShown: boolean = false;
    imagesMinIoUrlMaps: {[key: string]: string} = {};
    
    clickedMenuLine?: OutputModel;
    selectedLine?: OutputModel;
    
    dataIsLoading = true;
    
    @ViewChild('menu') menu!: Menu;
    items: MenuItem[] | undefined;

    @Output() onAddOutputClick: EventEmitter<void> = new EventEmitter();

    lastPeriodIsInProgress: boolean = false;

    cancelOutputDialogIsVisible: boolean = false;
    cancelDialogIsLoading: boolean = false;

    constructor(
        private refTableService: RefTableService,
        private minIoService: MinIoService,
        private outputService: OutputService,
        private messageService: MessageService,
        private financialPeriodService: FinancialPeriodService
    ) {
        this.initAttributsDetails();
        this.initMenuItems();
        this.getRefTables();
        this.isLastPeriodInProgress();
    }

    ngOnChanges(changes: SimpleChanges): void {
        if(changes['dataList$'] && !changes['dataList$'].firstChange) {
            this.getData();
        }
    }

    initAttributsDetails() {        
        this.attributsDetailsMap = {
            
            id: {libelle: 'Id', type: 'numeric'},
            label: {libelle: 'Label', type: 'text'},
            productLabel: {libelle: 'Product', type: 'text'},

            quantity: {libelle: 'Quantity', type: 'numeric'},
            unitCost:   { libelle: 'Unit cost',   type: 'numeric', isMontant: true },
            totalCost:  { libelle: 'Total cost',  type: 'numeric', isMontant: true },
            unitPrice:  { libelle: 'Unit price',  type: 'numeric', isMontant: true },
            totalPrice: { libelle: 'Total price', type: 'numeric', isMontant: true },
            unitProfit: { libelle: 'Unit profit', type: 'numeric', isMontant: true },
            totalProfit:{ libelle: 'Total profit',type: 'numeric', isMontant: true },

            addingDate: {libelle: 'Adding date', type: 'date'},
            addingTime: {libelle: 'Adding time', type: 'time'},
            transactionDate: {libelle: 'Transaction date', type: 'date'},
            transactionTime: {libelle: 'Transaction time', type: 'time'},

            imageSrc: {libelle: 'Image', type: 'image'},
            idTransactionType: {libelle: 'Transaction type', type: 'list', listCode: 'transType'},
            idState: {libelle: 'State', type: 'list', listCode: 'transState'},
            idPeriod: {libelle: 'Period', type: 'list', listCode: 'period'},
            soldBy: {libelle: 'Sold by', type: 'list', listCode: 'user'},
            executedBy: {libelle: 'Executed by', type: 'list', listCode: 'user'}
        }

        this.attributesCodes = Object.keys(this.attributsDetailsMap);
        
        this.multiSortMeta = [
            { field: 'id', order: -1 }
        ];

    }

    getRefTables() {
        const getProductsTransactionStatesRefTable$ = this.refTableService.getProductsTransactionStatesRefTable()
            .pipe(
                tap(
                    data => this.refTablesMap['transState'] = convertListToMapObject(data)
                )
            );

        const getTransactionTypesRefTable$ = this.refTableService.getTransactionTypesRefTable()
            .pipe(
                tap(
                    data => this.refTablesMap['transType'] = convertListToMapObject(data)
                )
            );

        const getPeriodsRefTable$ = this.refTableService.getPeriodsRefTable()
            .pipe(
                tap(
                    data => this.refTablesMap['period'] = convertListToMapObject(data)
                )
            );

        const getUsersRefTable$ = this.refTableService.getUsersRefTable()
            .pipe(
                tap(
                    data => this.refTablesMap['user'] = convertListToMapObject(data)
                )
            );

        // 🔁 Exécution parallèle de toutes les requêtes
        forkJoin([
            getProductsTransactionStatesRefTable$,
            getTransactionTypesRefTable$,
            getPeriodsRefTable$,
            getUsersRefTable$
        ]).subscribe(
            () => {
                // ✅ Action exécutée après que toutes les refTables soient chargées
                this.getData();
            }
        );
    }
        
    isLastPeriodInProgress() {
        this.financialPeriodService.getLastPeriod().subscribe(
            (data: FinancialPeriod | undefined) => {
                this.lastPeriodIsInProgress = data?.stateId === FINANCIAL_PERIOD_STATES.IN_PROG;
            }
        );
    }

    getData() {
        
        this.dataIsLoading = true;

        this.dataList$?.subscribe(
            {
                next: (data: OutputModel[]) => {
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
                this.cancelOutputDialogIsVisible = true;
                // this.outputService.cancelOutput(this.clickedMenuLine!.id!).subscribe(
                //     data => {
                //         this.getData();
                //         this.messageService.add({
                //             severity: 'success',
                //             summary: 'Transaction cancellation',
                //             detail: 'The output transaction has been cancelled.'
                //         });
                //     }
                // );
                break;
            case 'edit':
                break;
        }
    }

    toggleMenu(item: OutputModel, event: Event) {
        this.clickedMenuLine = item;
        this.menu.toggle(event);
    }
    onConfirmCancelOutput() {
        this.cancelDialogIsLoading = true;
        this.outputService.cancelOutput(this.clickedMenuLine!.id!)
            .pipe(
                finalize(() => {
                    this.cancelOutputDialogIsVisible = false;
                    this.cancelDialogIsLoading = false;
                })
            ).subscribe(
            data => {
                this.messageService.add({
                    severity: 'success',
                    summary: 'Output Cancellation',
                    detail: 'The selected output has been successfully canceled.'
                });
                this.getData();
            }
        );
    }

    isDate(date: any) {
        return date instanceof Date;
    }

}
