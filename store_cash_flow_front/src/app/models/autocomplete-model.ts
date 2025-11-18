import { convertListToMapObject } from "../utils/functions/helpers";
import { RefTable } from "./ref-table";

export class AutocompleteModel {
    id!: string;
    label!: string;
    optionLabel: string = 'label';
    initialValues: RefTable[] = [];
    values: RefTable[] = [];
    valuesMap: { [id: number]: RefTable } = {};
    filteredValues: RefTable[] = [];
    searchedValueMayNotMatchList: boolean = false;

    constructor(id: string, label: string, values: RefTable[], optionLabel: string | undefined, searchedValueMayNotMatchList: boolean | undefined) {
        this.id = id;
        this.label = label;
        this.values = values;
        this.initialValues = values;
        this.valuesMap = convertListToMapObject(this.values);
        if(optionLabel) this.optionLabel = optionLabel;
        if(searchedValueMayNotMatchList) this.searchedValueMayNotMatchList = searchedValueMayNotMatchList;
    }
}