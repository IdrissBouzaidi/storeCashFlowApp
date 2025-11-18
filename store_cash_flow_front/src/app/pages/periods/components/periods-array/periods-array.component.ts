import { Component, EventEmitter, Input, OnChanges, Output, SimpleChanges, ViewChild } from '@angular/core';
import { finalize, forkJoin, Observable, tap } from 'rxjs';
import { Table, TableModule } from 'primeng/table';
import { DialogModule } from 'primeng/dialog';
import { ButtonModule } from 'primeng/button';
import { CommonModule } from '@angular/common';
import { Menu } from 'primeng/menu';
import { MenuItem, MessageService, SortMeta } from 'primeng/api';
import { RefTable } from '../../../../models/ref-table';
import { FieldDetails } from '../../../../models/field-details';
import { RefTableService } from '../../../../service/ref-table.service';
import { convertListToMapObject, getFieldErrorFromFormBuilder } from '../../../../utils/functions/helpers';
import { convertServiceDateToDateObject, convertServiceTimeToTimeObject } from '../../../../utils/functions/date-converer';
import { convertNumberToFrNumber } from '../../../../utils/functions/number-converter';
import { FinancialPeriod } from '../../../../models/financial-period';
import { FinancialPeriodService } from '../../../../service/financial-period.service';
import { Form, FormBuilder, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { DatePicker } from 'primeng/datepicker';
import { FloatLabel } from 'primeng/floatlabel';
import { FINANCIAL_PERIOD_STATES } from '../../../../utils/consts/states-consts';

@Component({
    selector: 'app-periods-array',
    imports: [
        Menu,
        DialogModule,
        ButtonModule,
        TableModule,
        CommonModule,

        ReactiveFormsModule,
        DatePicker,
        FloatLabel
    ],
    templateUrl: './periods-array.component.html',
    styleUrl: './periods-array.component.scss'
})
export class PeriodsArrayComponent implements OnChanges {
    refTablesMap: { [code: string]: { [id: number]: RefTable } } = {};

    attributesCodes: string[] = [];
    attributsDetailsMap!: { [key: string]: FieldDetails };
    multiSortMeta: SortMeta[] = [];

    @Input() dataList$?: Observable<FinancialPeriod[]>;
    sourceData: FinancialPeriod[] = [];
    data: FinancialPeriod[] = [];

    
    clickedMenuLine?: FinancialPeriod;
    dataTableIsShown: boolean = false;
    
    selectedLine?: FinancialPeriod;
    
    dataIsLoading = true;
    
    @ViewChild('menu') menu!: Menu;
    items: MenuItem[] | undefined;
    itemsMap: { [id: string]: MenuItem } = {};

    @Output() onAddButtonClick: EventEmitter<void> = new EventEmitter();

    somePeriodIsInProgress: boolean = false;
    lastPeriod: FinancialPeriod | undefined;

    closePeriodForm!: FormGroup;
    closePeriodDialogIsVisible: boolean = false;

    cancelPeriodDialogIsVisible: boolean = false;
    reopenPeriodDialogIsVisible: boolean = false;

    constructor(
        private refTableService: RefTableService,
        private financialPeriodService: FinancialPeriodService,
        private messageService: MessageService,
        private fb: FormBuilder
    ) {
        this.initForm();
        this.initAttributsDetails();
        this.initMenuItems();
        this.getRefTables();
    }

    ngOnChanges(changes: SimpleChanges): void {
        if(changes['dataList$'] && !changes['dataList$'].firstChange) {
            this.getData();
        }
    }

    initForm() {
        this.closePeriodForm = this.fb.group({
            endDate: [new Date()],
            endTime: [new Date()]
        });
    }

    initAttributsDetails() {
        this.attributsDetailsMap = {
            id: {libelle: 'Id', type: 'numeric'},
            label: {libelle: 'Label', type: 'text'},

            startDate: {libelle: 'Start date', type: 'date'},
            startTime: {libelle: 'Start time', type: 'time'},
            endDate: {libelle: 'End date', type: 'date'},
            endTime: {libelle: 'End time', type: 'time'},
            duration: {libelle: 'Duration', type: 'numeric'},
            details: {libelle: 'Details', type: 'text'},
            stateId: {libelle: 'State', type: 'list', listCode: 'periodState'},
            
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
        const getPeriodsStatesRefTable$ = this.refTableService.getPeriodsStatesRefTable()
                .pipe(
                    tap(
                        data => this.refTablesMap['periodState'] = convertListToMapObject(data)
                    )
                );
        forkJoin([getPeriodsStatesRefTable$]).subscribe(
            data => {
                this.getData();
            }
        );
    }

    getData() {
        
        this.dataIsLoading = true;

        this.isSomePeriodInProgress();
        this.getLastPeriod();

        this.dataList$?.subscribe(
            {
                next: (data: FinancialPeriod[]) => {
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

    isSomePeriodInProgress() {
        this.financialPeriodService.isSomePeriodInProgress().subscribe(data => this.somePeriodIsInProgress = data);
    }

    getLastPeriod() {
        this.financialPeriodService.getLastPeriod().subscribe(data => this.lastPeriod = data);
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
                command: () => this.onMenuItemClick('details'),
                disabled: true
            },
            {
                id: 'reopen',
                label: 'Reopen',
                command: () => this.onMenuItemClick('reopen'),
                disabled: true
            },
            {
                id: 'cancel',
                label: 'Cancel',
                command: () => this.onMenuItemClick('cancel'),
                disabled: true
            }
        ]
        this.items.forEach((item: any) => this.itemsMap[item.id] = item);

    }

    onMenuItemClick(itemName: 'details' | 'reopen' | 'cancel') {
        switch(itemName) {
            case 'details':
                break;
            case 'reopen':
                this.reopenPeriodDialogIsVisible = true;
                break;
            case 'cancel':
                this.cancelPeriodDialogIsVisible = true;
                break;
        }
    }

    toggleMenu(item: FinancialPeriod, event: Event) {
        debugger
        this.clickedMenuLine = item;

        const periodIsInProgress: boolean = item.stateId === FINANCIAL_PERIOD_STATES.IN_PROG;
        const periodIsClosed: boolean = item.stateId === FINANCIAL_PERIOD_STATES.CLOSED;
        const periodIsTheLastPeriod: boolean = item.id === this.lastPeriod?.id;
        const cancelPeriod: boolean = periodIsInProgress;
        const reopenPeriod = (periodIsClosed && periodIsTheLastPeriod);
        this.itemsMap['cancel'].disabled = !cancelPeriod;

        this.itemsMap['reopen'].disabled = !reopenPeriod;
        this.menu.toggle(event);
    }

    isDate(date: any) {
        return date instanceof Date;
    }

    onClosePeriodConfirm() {
        const endDate: Date | undefined = this.closePeriodForm.get('endDate')!.value;
        const endTime: Date | undefined = this.closePeriodForm.get('endTime')!.value;
        this.financialPeriodService.closeCurrentPeriod(endDate, endTime).subscribe(
            data => {
                this.closePeriodDialogIsVisible = false;
                this.messageService.add({
                    severity: 'success', 
                    summary: 'Period Closure', 
                    detail: 'The current financial period has been successfully closed.' 
                });
                this.getData();
            }
        );
    }

    onConfirmCancelPeriod() {
        this.financialPeriodService.cancel(this.clickedMenuLine!.id!)
            .pipe(
                finalize(() => {
                    this.cancelPeriodDialogIsVisible = false;
                })
            ).subscribe(
            data => {
                this.messageService.add({
                    severity: 'success',
                    summary: 'Period Cancellation',
                    detail: 'The selected financial period has been successfully canceled.'
                });
                this.getData();
                this.isSomePeriodInProgress();
            }
        );
    }

    onConfirmReopenPeriod() {
        this.financialPeriodService.reopen(this.clickedMenuLine!.id!)
            .pipe(
                finalize(() => {
                    this.reopenPeriodDialogIsVisible = false;
                })
            ).subscribe(
            data => {
                this.messageService.add({
                    severity: 'success',
                    summary: 'Period Reopening',
                    detail: 'The financial period has been successfully reopened and is now in progress.'
                });
                this.getData();
                this.isSomePeriodInProgress();
            }
        );
    }
    
    getFieldError(fieldName: string) {
        const fieldControl = this.closePeriodForm.get(fieldName);
        const fieldError: string = getFieldErrorFromFormBuilder(fieldControl);
        return fieldError;
    }

}
