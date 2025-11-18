import { Component, EventEmitter, Input, OnChanges, Output, SimpleChanges, ViewChild } from '@angular/core';
import { forkJoin, Observable, tap } from 'rxjs';
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
import { Charge } from '../../../../../models/charge';
import { MinIoService } from '../../../../../service/min-io.service';
import { ApiResponse } from '../../../../../models/api-response';
import { ChargeService } from '../../../../../service/charge.service';

@Component({
    selector: 'app-charge-array',
    imports: [
        Menu,
        DialogModule,
        ButtonModule,
        TableModule,
        CommonModule
    ],
    templateUrl: './charge-array.component.html',
    styleUrl: './charge-array.component.scss'
})
export class ChargeArrayComponent implements OnChanges {
    refTablesMap: { [code: string]: { [id: number]: RefTable } } = {};

    attributesCodes: string[] = [];
    attributsDetailsMap!: { [key: string]: FieldDetails };
    multiSortMeta: SortMeta[] = [];

    @Input() dataList$?: Observable<Charge[]>;
    sourceData: Charge[] = [];
    data: Charge[] = [];
    dataTableIsShown: boolean = false;
    imagesMinIoUrlMaps: {[key: string]: string} = {};
    
    clickedMenuLine?: Charge;
    selectedLine?: Charge;
    
    dataIsLoading = true;
    
    @ViewChild('menu') menu!: Menu;
    items: MenuItem[] | undefined;

    @Output() onAddChargeClick: EventEmitter<void> = new EventEmitter();

    constructor(
        private refTableService: RefTableService,
        private minIoService: MinIoService,
        private messageService: MessageService,
        private chargeService: ChargeService
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
            
            id: {libelle: 'Id', type: 'numeric'},
            label: {libelle: 'Label', type: 'text'},
            idChargeType: {libelle: 'Charge type', type: 'list', listCode: 'idChargeType'},

            cost: {libelle: 'Cost', type: 'numeric', isMontant: true},
            quantity: {libelle: 'Quantity', type: 'numeric'},
            total: {libelle: 'Total cost', type: 'numeric', isMontant: true},

            addingDate: {libelle: 'Adding date', type: 'date'},
            addingTime: {libelle: 'Adding time', type: 'time'},
            transactionDate: {libelle: 'Transaction date', type: 'date'},
            transactionTime: {libelle: 'Transaction time', type: 'time'},

            imageSrc: {libelle: 'Image', type: 'image'},
            idTransactionType: {libelle: 'Transaction type', type: 'list', listCode: 'transType'},
            idState: {libelle: 'State', type: 'list', listCode: 'transState'},
            consumedBy: {libelle: 'Consumed by', type: 'list', listCode: 'consumedBy'},
            idPeriod: {libelle: 'Period', type: 'list', listCode: 'period'},
            executedBy: {libelle: 'Executed by', type: 'list', listCode: 'executedBy'}
        }

        this.attributesCodes = Object.keys(this.attributsDetailsMap);
        
        this.multiSortMeta = [
            { field: 'id', order: -1 }
        ];

    }

    getRefTables() {
        const getChargesTransactionStatesRefTable$ = this.refTableService.getChargesTransactionStatesRefTable()
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

        const getExecutedByUsersRefTable$ = this.refTableService.getUsersRefTable()
            .pipe(
                tap(
                    data => this.refTablesMap['executedBy'] = convertListToMapObject(data)
                )
            );

        const getConsumedByUsersRefTable$ = this.refTableService.getUsersRefTable()
            .pipe(
                tap(
                    data => this.refTablesMap['consumedBy'] = convertListToMapObject(data)
                )
            );

        const getChargeTypesRefTable$ = this.refTableService.getChargeTypesRefTable()
            .pipe(
                tap(
                    data => this.refTablesMap['idChargeType'] = convertListToMapObject(data)
                )
            );

        // 🔁 Exécution parallèle de toutes les requêtes
        forkJoin([
            getChargesTransactionStatesRefTable$,
            getTransactionTypesRefTable$,
            getPeriodsRefTable$,
            getExecutedByUsersRefTable$,
            getConsumedByUsersRefTable$,
            getChargeTypesRefTable$
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
                next: (data: Charge[]) => {
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
                this.chargeService.cancelCharge(this.clickedMenuLine!.id).subscribe(
                    data => {
                        this.messageService.add({
                            severity: 'success',
                            summary: 'Transaction cancellation',
                            detail: 'The charge transaction has been cancelled.'
                        });
                        this.getData();
                    }
                );
                break;
            case 'edit':
                break;
        }
    }

    toggleMenu(item: Charge, event: Event) {
        this.clickedMenuLine = item;
        this.menu.toggle(event);
    }

    isDate(date: any) {
        return date instanceof Date;
    }

}
