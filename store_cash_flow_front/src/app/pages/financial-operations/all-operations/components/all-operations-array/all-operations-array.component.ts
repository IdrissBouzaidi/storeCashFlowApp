import { Component, EventEmitter, Input, OnChanges, Output, SimpleChanges, ViewChild } from '@angular/core';
import { forkJoin, Observable, tap } from 'rxjs';
import { Table, TableModule } from 'primeng/table';
import { DialogModule } from 'primeng/dialog';
import { ButtonModule } from 'primeng/button';
import { CommonModule } from '@angular/common';
import { Menu } from 'primeng/menu';
import { MenuItem, SortMeta } from 'primeng/api';
import { FieldDetails } from '../../../../../models/field-details';
import { convertNumberToFrNumber } from '../../../../../utils/functions/number-converter';
import { convertServiceDateToDateObject, convertServiceTimeToTimeObject } from '../../../../../utils/functions/date-converer';
import { RefTable } from '../../../../../models/ref-table';
import { RefTableService } from '../../../../../service/ref-table.service';
import { convertListToMapObject } from '../../../../../utils/functions/helpers';
import { Transaction } from '../../../../../models/transaction';
import { ApiResponse } from '../../../../../models/api-response';
import { MinIoService } from '../../../../../service/min-io.service';

@Component({
    selector: 'app-all-operations-array',
    imports: [
        Menu,
        DialogModule,
        ButtonModule,
        TableModule,
        CommonModule
    ],
    templateUrl: './all-operations-array.component.html',
    styleUrl: './all-operations-array.component.scss'
})
export class AllOperationsArrayComponent implements OnChanges {
    refTablesMap: { [code: string]: { [id: number]: RefTable } } = {};

    attributesCodes: string[] = [];
    attributsDetailsMap!: { [key: string]: FieldDetails };
    multiSortMeta: SortMeta[] = [];

    @Input() dataList$?: Observable<Transaction[]>;
    sourceData: Transaction[] = [];
    data: Transaction[] = [];
    dataTableIsShown: boolean = false;
    imagesMinIoUrlMaps: {[key: string]: string} = {};
    
    clickedMenuLine?: Transaction;
    selectedLine?: Transaction;
    
    dataIsLoading = true;
    
    @ViewChild('menu') menu!: Menu;
    items: MenuItem[] | undefined;

    constructor(
        private refTableService: RefTableService,
        private minIoService: MinIoService
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
            amount: { libelle: 'Amount', type: 'numeric', isMontant: true },

            addingDate: { libelle: 'Adding date', type: 'date' },
            addingTime: { libelle: 'Adding time', type: 'time' },

            transactionDate: { libelle: 'Transaction date', type: 'date' },
            transactionTime: { libelle: 'Transaction time', type: 'time' },

            details: { libelle: 'Details', type: 'text' },
            imageSrc: { libelle: 'Image', type: 'image' },

            idTransactionType: { libelle: 'Transaction type', type: 'list', listCode: 'transType' },
            idPeriod: { libelle: 'Period', type: 'list', listCode: 'period' },
            executedBy: { libelle: 'Executed by', type: 'list', listCode: 'executedBy' },

            // --- Colonnes des montants courants ---
            currentCapital: { libelle: 'Current capital', type: 'numeric', isMontant: true },
            currentProfitGross: { libelle: 'Gross profit', type: 'numeric', isMontant: true },
            currentProfitNet: { libelle: 'Net profit', type: 'numeric', isMontant: true },
            totalExpenses: { libelle: 'Total expenses', type: 'numeric', isMontant: true },
            totalCustomerCredit: { libelle: 'Customer credit', type: 'numeric', isMontant: true },
            totalExternalLoan: { libelle: 'External loan', type: 'numeric', isMontant: true },
            totalAdvance: { libelle: 'Advance', type: 'numeric', isMontant: true },
            totalConsumableInputs: { libelle: 'Consumable inputs', type: 'numeric', isMontant: true },
            totalNonConsumableInputs: { libelle: 'Non-consumable inputs', type: 'numeric', isMontant: true },
            cashRegisterBalance: { libelle: 'Cash register balance', type: 'numeric', isMontant: true },
            totalOutOfPocketExpenses: { libelle: 'Out of pocket expenses', type: 'numeric', isMontant: true }
        }

        this.attributesCodes = Object.keys(this.attributsDetailsMap);
        
        this.multiSortMeta = [
            { field: 'id', order: -1 }
        ];

    }

    getRefTables() {
        const getTransactionTypesRefTable$ = this.refTableService.getTransactionTypesRefTable()
            .pipe(
                tap(data => this.refTablesMap['transType'] = convertListToMapObject(data))
            );

        const getPeriodsRefTable$ = this.refTableService.getPeriodsRefTable()
            .pipe(
                tap(data => this.refTablesMap['period'] = convertListToMapObject(data))
            );

        const getUsersRefTable$ = this.refTableService.getUsersRefTable()
            .pipe(
                tap(data => this.refTablesMap['executedBy'] = convertListToMapObject(data))
            );

        // 🔁 Exécuter les trois requêtes en parallèle
        forkJoin([
            getTransactionTypesRefTable$,
            getPeriodsRefTable$,
            getUsersRefTable$
        ]).subscribe(
            data => {
                // ✅ Action une fois tout chargé
                this.getData();
            }
        );
    }

    getData() {
        
        this.dataIsLoading = true;

        this.dataList$?.subscribe(
            {
                next: (data: Transaction[]) => {
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
                label: 'Détails',
                command: () => this.onMenuItemClick('details')
            }
        ]

    }

    onMenuItemClick(itemName: 'details') {
        switch(itemName) {
            case 'details':
                break;
        }
    }

    toggleMenu(item: Transaction, event: Event) {
        this.clickedMenuLine = item;
        this.menu.toggle(event);
    }

    isDate(date: any) {
        return date instanceof Date;
    }

}
