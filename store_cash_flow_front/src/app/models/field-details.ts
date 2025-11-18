import { AutocompleteModel } from "./autocomplete-model";

export class FieldDetails {
    libelle!: string;
    type!: 'numeric' | 'text' | 'list' | 'autoComplete' | 'date' | 'time' | 'image';
    isMontant?: boolean;
    listCode?: string;
    formControl?: any[];
    autoCompleteDetails?: AutocompleteModel;
    required?: boolean;
    isDecimal?: boolean;
}