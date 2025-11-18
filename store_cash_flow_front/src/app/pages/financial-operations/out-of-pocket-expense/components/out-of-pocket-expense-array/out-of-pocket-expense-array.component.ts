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
import { OutOfPocket } from '../../../../../models/out-of-pocket';
import { OutOfPocketService } from '../../../../../service/out-of-pocket.service';
import { FinancialPeriodService } from '../../../../../service/financial-period.service';
import { FinancialPeriod } from '../../../../../models/financial-period';
import { FINANCIAL_PERIOD_STATES } from '../../../../../utils/consts/states-consts';

@Component({
    selector: 'app-out-of-pocket-expense-array',
    imports: [
        Menu,
        DialogModule,
        ButtonModule,
        TableModule,
        CommonModule
    ],
    templateUrl: './out-of-pocket-expense-array.component.html',
    styleUrl: './out-of-pocket-expense-array.component.scss'
})
export class OutOfPocketExpenseArrayComponent implements OnChanges {
    refTablesMap: { [code: string]: { [id: number]: RefTable } } = {};

    attributesCodes: string[] = [];
    attributsDetailsMap!: { [key: string]: FieldDetails };
    multiSortMeta: SortMeta[] = [];

    @Input() dataList$?: Observable<OutOfPocket[]>;
    sourceData: OutOfPocket[] = [];
    data: OutOfPocket[] = [];
    dataTableIsShown: boolean = false;
    
    clickedMenuLine?: OutOfPocket;
    selectedLine?: OutOfPocket;
    
    dataIsLoading = true;
    
    @ViewChild('menu') menu!: Menu;
    items: MenuItem[] | undefined;

    @Output() onAddOutOfPocketClick: EventEmitter<void> = new EventEmitter();

    lastPeriodIsInProgress: boolean = false;

    cancelOutOfPocketExpenseDialogIsVisible: boolean = false;
    cancelDialogIsLoading: boolean = false;

    constructor(
        private refTableService: RefTableService,
        private outOfPocketService: OutOfPocketService,
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

            amount: {libelle: 'Amount', type: 'numeric', isMontant: true},

            addingDate: {libelle: 'Adding date', type: 'date'},
            addingTime: {libelle: 'Adding time', type: 'time'},
            borrowingDate: {libelle: 'Borrowing date', type: 'date'},
            borrowingTime: {libelle: 'Borrowing time', type: 'time'},

            stateId: {libelle: 'State', type: 'list', listCode: 'transState'},
            borrowerId: {libelle: 'Borrower', type: 'list', listCode: 'borrower'},
            idPeriod: {libelle: 'Period', type: 'list', listCode: 'period'},
            executedBy: {libelle: 'Executed by', type: 'list', listCode: 'executedBy'}
        }

        this.attributesCodes = Object.keys(this.attributsDetailsMap);
        
        this.multiSortMeta = [
            { field: 'id', order: -1 }
        ];
    }

    getRefTables() {
        // ⚙️ Récupération parallèle des tables de référence pour "Out Of Pocket"
        const getOutOfPocketStatesRefTable$ = this.refTableService.getOutOfPocketStatesRefTable()
            .pipe(
                tap(
                    data => this.refTablesMap['transState'] = convertListToMapObject(data) // 🔄 États des transactions "Out Of Pocket"
                )
            );

        const getPeriodsRefTable$ = this.refTableService.getPeriodsRefTable()
            .pipe(
                tap(
                    data => this.refTablesMap['period'] = convertListToMapObject(data) // 🗓️ Liste des périodes
                )
            );

        const getUsersRefTable$ = this.refTableService.getUsersRefTable()
            .pipe(
                tap(data => {
                    const map: { [id: number]: any } = convertListToMapObject(data);
                    this.refTablesMap['borrower'] = map;     // 💸 Emprunteur
                    this.refTablesMap['executedBy'] = map;   // 👷 Utilisateur exécutant
                })
            );

        // 🔁 Exécution parallèle de toutes les requêtes
        forkJoin([
            getOutOfPocketStatesRefTable$,
            getPeriodsRefTable$,
            getUsersRefTable$
        ]).subscribe(
            () => {
                // ✅ Action exécutée une fois que toutes les refTables sont chargées
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
                next: (data: OutOfPocket[]) => {
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

                        this.dataIsLoading = false;
                },
                error: (error) => {
                    this.dataTableIsShown = false;
                    this.dataIsLoading = false;
                }
            });
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
                this.cancelOutOfPocketExpenseDialogIsVisible = true;
                // this.outOfPocketService.cancelOutOfPocket(this.clickedMenuLine!.id!).subscribe(
                //     data => {
                //         this.messageService.add({
                //             severity: 'success',
                //             summary: 'Transaction cancellation',
                //             detail: 'The out-of-pocket transaction has been cancelled.'
                //         });
                //     }
                // );
                break;
            case 'edit':
                break;
        }
    }

    toggleMenu(item: OutOfPocket, event: Event) {
        this.clickedMenuLine = item;
        this.menu.toggle(event);
    }

    onConfirmCancelOutOfPocketExpense() {
        this.cancelDialogIsLoading = true;
        this.outOfPocketService.cancelOutOfPocket(this.clickedMenuLine!.id!)
            .pipe(
                finalize(() => {
                    this.cancelOutOfPocketExpenseDialogIsVisible = false;
                    this.cancelDialogIsLoading = false;
                })
            ).subscribe(
            data => {
                this.messageService.add({
                    severity: 'success',
                    summary: 'Out of Pocket Expense Cancellation',
                    detail: 'The selected out of pocket expense has been successfully canceled.'
                });
                this.getData();
            }
        );
    }

    isDate(date: any) {
        return date instanceof Date;
    }

}
