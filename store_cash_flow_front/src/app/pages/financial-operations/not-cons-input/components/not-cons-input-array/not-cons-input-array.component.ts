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
import { NotConsInput } from '../../../../../models/not-cons-input';
import { ApiResponse } from '../../../../../models/api-response';
import { MinIoService } from '../../../../../service/min-io.service';
import { NotConsInputService } from '../../../../../service/not-cons-input.service';
import { FinancialPeriodService } from '../../../../../service/financial-period.service';
import { FinancialPeriod } from '../../../../../models/financial-period';
import { FINANCIAL_PERIOD_STATES } from '../../../../../utils/consts/states-consts';

@Component({
    selector: 'app-not-cons-input-array',
    imports: [
        Menu,
        DialogModule,
        ButtonModule,
        TableModule,
        CommonModule
    ],
    templateUrl: './not-cons-input-array.component.html',
    styleUrl: './not-cons-input-array.component.scss'
})
export class NotConsInputArrayComponent implements OnChanges {
    refTablesMap: { [code: string]: { [id: number]: RefTable } } = {};

    attributesCodes: string[] = [];
    attributsDetailsMap!: { [key: string]: FieldDetails };
    multiSortMeta: SortMeta[] = [];

    @Input() dataList$?: Observable<NotConsInput[]>;
    sourceData: NotConsInput[] = [];
    data: NotConsInput[] = [];
    dataTableIsShown: boolean = false;
    imagesMinIoUrlMaps: {[key: string]: string} = {};
    
    clickedMenuLine?: NotConsInput;
    selectedLine?: NotConsInput;
    
    dataIsLoading = true;
    
    @ViewChild('menu') menu!: Menu;
    items: MenuItem[] | undefined;

    @Output() onAddNotConsInputClick: EventEmitter<void> = new EventEmitter();

    lastPeriodIsInProgress: boolean = false;
    
    cancelNotConsInputDialogIsVisible: boolean = false;
    cancelDialogIsLoading: boolean = false;

    constructor(
        private refTableService: RefTableService,
        private minIoService: MinIoService,
        private messageService: MessageService,
        private notConsInputService: NotConsInputService,
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
            reusableInputId: {libelle: 'Reusable input name', type: 'list', listCode: 'reusableInput'},
            cost: {libelle: 'Cost', type: 'numeric', isMontant: true},
            initialQuantity: {libelle: 'Initial quantity', type: 'numeric'},
            remainingQuantity: {libelle: 'remaining quantity', type: 'numeric'},
            total: {libelle: 'Total cost', type: 'numeric', isMontant: true},
            addingDate: {libelle: 'Adding date', type: 'date'},
            addingTime: {libelle: 'Adding time', type: 'time'},
            transactionDate: {libelle: 'Transaction date', type: 'date'},
            transactionTime: {libelle: 'Transaction time', type: 'time'},
            contributor: {libelle: 'Contributor', type: 'list', listCode: 'contributor'},
            details: {libelle: 'Details', type: 'text'},
            stateId: {libelle: 'State', type: 'list', listCode: 'transState'},
            idPeriod: {libelle: 'Period', type: 'list', listCode: 'period'},
            executedBy: {libelle: 'Executed by', type: 'list', listCode: 'executedBy'},
            imageSrc: {libelle: 'Image', type: 'image'}

        }

        this.attributesCodes = Object.keys(this.attributsDetailsMap);
        
        this.multiSortMeta = [
            { field: 'id', order: -1 }
        ];

    }

    getRefTables() {
        const getReusableInputsRefTable$ = this.refTableService.getReusableInputsRefTable()
            .pipe(
                tap(
                    data => this.refTablesMap['reusableInput'] = convertListToMapObject(data)
                )
            );

        const getNotConsInputStatesRefTable$ = this.refTableService.getNotConsInputStatesRefTable()
            .pipe(
                tap(
                    data => this.refTablesMap['transState'] = convertListToMapObject(data)
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
                    data => this.refTablesMap['executedBy'] = convertListToMapObject(data)
                )
            );

        const getContributorsRefTable$ = this.refTableService.getUsersRefTable()
            .pipe(
                tap(
                    data => this.refTablesMap['contributor'] = convertListToMapObject(data)
                )
            );

        // 🔁 Exécuter toutes les requêtes simultanément
        forkJoin([
            getReusableInputsRefTable$,
            getNotConsInputStatesRefTable$,
            getPeriodsRefTable$,
            getUsersRefTable$,
            getContributorsRefTable$
        ]).subscribe(
            () => {
                // ✅ Action exécutée après le chargement de toutes les refTables
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
                next: (data: NotConsInput[]) => {
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
                this.cancelNotConsInputDialogIsVisible = true;
                // this.notConsInputService.cancelNotConsInput(this.clickedMenuLine!.id!).subscribe(
                //     data => {
                //         this.getData();
                //         this.messageService.add({
                //             severity: 'success',
                //             summary: 'Transaction cancellation',
                //             detail: 'The non-consumable input transaction has been cancelled.'
                //         });
                //     }
                // );
                break;
            case 'edit':
                break;
        }
    }


    toggleMenu(item: NotConsInput, event: Event) {
        this.clickedMenuLine = item;
        this.menu.toggle(event);
    }
    onConfirmCancelNotConsumableInput() {
        this.cancelDialogIsLoading = true;
        this.notConsInputService.cancelNotConsInput(this.clickedMenuLine!.id!)
            .pipe(
                finalize(() => {
                    this.cancelNotConsInputDialogIsVisible = false;
                    this.cancelDialogIsLoading = false;
                })
            ).subscribe(
            data => {
                this.messageService.add({
                    severity: 'success',
                    summary: 'Not Consumable Input Cancellation',
                    detail: 'The selected not consumable input has been successfully canceled.'
                });
                this.getData();
            }
        );
    }

    isDate(date: any) {
        return date instanceof Date;
    }

}
