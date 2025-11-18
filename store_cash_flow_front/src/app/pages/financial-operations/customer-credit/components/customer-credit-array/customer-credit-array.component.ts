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
import { Charge } from '../../../../../models/charge';
import { CustomerCreditService } from '../../../../../service/customer-credit.service';
import { FinancialPeriodService } from '../../../../../service/financial-period.service';
import { FinancialPeriod } from '../../../../../models/financial-period';
import { FINANCIAL_PERIOD_STATES } from '../../../../../utils/consts/states-consts';

@Component({
    selector: 'app-customer-credit-array',
    imports: [
        Menu,
        DialogModule,
        ButtonModule,
        TableModule,
        CommonModule
    ],
    templateUrl: './customer-credit-array.component.html',
    styleUrl: './customer-credit-array.component.scss'
})
export class CustomerCreditArrayComponent implements OnChanges {
    refTablesMap: { [code: string]: { [id: number]: RefTable } } = {};

    attributesCodes: string[] = [];
    attributsDetailsMap!: { [key: string]: FieldDetails };
    multiSortMeta: SortMeta[] = [];

    @Input() dataList$?: Observable<Charge[]>;
    sourceData: Charge[] = [];
    data: Charge[] = [];
    dataTableIsShown: boolean = false;
    
    clickedMenuLine?: Charge;
    selectedLine?: Charge;
    
    dataIsLoading = true;
    
    @ViewChild('menu') menu!: Menu;
    items: MenuItem[] | undefined;

    @Output() onAddCustomerCreditClick: EventEmitter<void> = new EventEmitter();

    lastPeriodIsInProgress: boolean = false;

    cancelCustomerCreditDialogIsVisible: boolean = false;
    cancelDialogIsLoading: boolean = false;

    constructor(
        private refTableService: RefTableService,
        private messageService: MessageService,
        private customerCreditService: CustomerCreditService,
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

            initialAmount: {libelle: 'Initial amount', type: 'numeric', isMontant: true},
            paidAmount: {libelle: 'Paid amount', type: 'numeric', isMontant: true},
            remainingAmount: {libelle: 'Remaining amount', type: 'numeric', isMontant: true},

            addingDate: {libelle: 'Adding date', type: 'date'},
            addingTime: {libelle: 'Adding time', type: 'time'},
            creditDate: {libelle: 'Credit date', type: 'date'},
            creditTime: {libelle: 'Credit time', type: 'time'},

            stateId: {libelle: 'State', type: 'list', listCode: 'transState'},
            customerId: {libelle: 'Customer', type: 'list', listCode: 'customer'},
            idPeriod: {libelle: 'Period', type: 'list', listCode: 'period'},
            executedBy: {libelle: 'Executed by', type: 'list', listCode: 'executedBy'}
        }

        this.attributesCodes = Object.keys(this.attributsDetailsMap);
        
        this.multiSortMeta = [
            { field: 'id', order: -1 }
        ];
    }

    getRefTables() {
        // ⚙️ Récupération parallèle des tables de référence pour "Customer Credit"
        const getCustomerCreditStatesRefTable$ = this.refTableService.getCustomerCreditStatesRefTable()
            .pipe(
                tap(
                    data => this.refTablesMap['transState'] = convertListToMapObject(data) // 🔄 États des crédits client
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
                tap(
                    data => this.refTablesMap['executedBy'] = convertListToMapObject(data) // 👷 Utilisateur exécutant
                )
            );

        const getCustomersRefTable$ = this.refTableService.getCustomersRefTable()
            .pipe(
                tap(
                    data => this.refTablesMap['customer'] = convertListToMapObject(data) // 🧾 Liste des clients
                )
            );

        // 🔁 Exécution parallèle de toutes les requêtes
        forkJoin([
            getCustomerCreditStatesRefTable$,
            getPeriodsRefTable$,
            getUsersRefTable$,
            getCustomersRefTable$
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
                this.cancelCustomerCreditDialogIsVisible = true;
                // this.customerCreditService.cancelCustomerCredit(this.clickedMenuLine!.id!).subscribe(
                //     data => {
                //         this.getData();
                //         this.messageService.add({
                //             severity: 'success',
                //             summary: 'Transaction cancellation',
                //             detail: 'The customer credit transaction has been cancelled.'
                //         });
                //     }
                // );
                break;
            case 'edit':
                break;
        }
    }

    toggleMenu(item: Charge, event: Event) {
        this.clickedMenuLine = item;
        this.menu.toggle(event);
    }
    
    onConfirmCancelCustomerCredit() {
        this.cancelDialogIsLoading = true;
        this.customerCreditService.cancelCustomerCredit(this.clickedMenuLine!.id!)
            .pipe(
                finalize(() => {
                    this.cancelCustomerCreditDialogIsVisible = false;
                    this.cancelDialogIsLoading = false;
                })
            ).subscribe(
            data => {
                this.messageService.add({
                    severity: 'success',
                    summary: 'Customer Credit Cancellation',
                    detail: 'The selected customer credit has been successfully canceled.'
                });
                this.getData();
            }
        );
    }

    isDate(date: any) {
        return date instanceof Date;
    }

}
