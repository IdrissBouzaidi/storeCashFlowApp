import { ChangeDetectorRef, Component, EventEmitter, Input, OnChanges, Output, SimpleChanges } from '@angular/core';
import { AutoCompleteCompleteEvent, AutoCompleteModule, AutoCompleteSelectEvent } from "primeng/autocomplete";
import { CommonModule } from '@angular/common';
import { FloatLabel } from "primeng/floatlabel";
import { FormControl, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { RefTable } from '../../models/ref-table';
import { AutocompleteModel } from '../../models/autocomplete-model';
import { getFieldErrorFromFormBuilder } from '../../utils/functions/helpers';

@Component({
    selector: 'app-autocomplete',
    imports: [
        CommonModule,
        FormsModule,
        FloatLabel,
        AutoCompleteModule,

        ReactiveFormsModule
    ],
    templateUrl: './autocomplete.component.html',
    styleUrl: './autocomplete.component.scss'
})
export class AutocompleteComponent implements OnChanges {
    @Input() control: any;
    @Input() details?: AutocompleteModel;
    values: RefTable[] = [];
    
    @Output() detailsChange = new EventEmitter<AutocompleteModel>();

    @Output() onSelect = new EventEmitter<RefTable | undefined>();

    @Input() required: boolean = false;

    selectedValue: any;

    constructor(
        private cdr: ChangeDetectorRef
    ) { }

    ngOnChanges(changes: SimpleChanges): void {
        if(changes['details'] && this.details) {
            this.values = [...this.details.values];
            this.details.filteredValues = [...this.values];
        }
    }

    filterValues(event: AutoCompleteCompleteEvent) {
        let searchedValue: string = event.query;
        searchedValue = searchedValue.toLowerCase();
        if(this.details) {
            this.details.filteredValues = this.details.values
                            .filter((item: any) => item[this.details!.optionLabel]?.toString().toLowerCase().includes(searchedValue));
        }
    }

    choseValue(selectedValue: RefTable | string | undefined) {
        if(typeof selectedValue === 'string' && this.details) {
            selectedValue = selectedValue.toLowerCase();
            const valueInList = this.details.values.find((item: any) => item[this.details!.optionLabel]?.toString().toLowerCase() === selectedValue);
            if(selectedValue) {
                if(valueInList)
                    this.control.setValue(valueInList);
                else if(!this.details.searchedValueMayNotMatchList)
                    this.control.setValue(undefined);
                else if(this.details.searchedValueMayNotMatchList)
                    this.control.setValue(new RefTable(selectedValue));
            }
        }
        this.selectValue(this.control?.value);
    }

    selectValue(selectedValue: RefTable | undefined) {
        this.control.setValue(selectedValue);
        this.onSelect.emit(selectedValue);
    }

    getFieldError() {
        return getFieldErrorFromFormBuilder(this.control);
    }
}
