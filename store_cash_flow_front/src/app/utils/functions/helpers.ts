import { AbstractControl, ValidationErrors, ValidatorFn } from "@angular/forms";
import { ConsInput } from "../../models/cons-input";

export function getFileFormat(fileName: string) {
    return fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
}

export function compareValues(value1: any, value2: any, sortOrder: number) {
    if (value1 == null && value2 != null) return -1 * sortOrder;
    if (value1 != null && value2 == null) return 1 * sortOrder;
    if (value1 == null && value2 == null) return 0;

    const isDate1 = value1 instanceof Date || !isNaN(Date.parse(value1));
    const isDate2 = value2 instanceof Date || !isNaN(Date.parse(value2));

    if (isDate1 && isDate2) {
        const date1 = new Date(value1).getTime();
        const date2 = new Date(value2).getTime();
        return (date1 - date2) * sortOrder;
    }

    const isNumber1 = typeof value1 === 'number' || !isNaN(Number(value1));
    const isNumber2 = typeof value2 === 'number' || !isNaN(Number(value2));

    if (isNumber1 && isNumber2) {
        return (Number(value1) - Number(value2)) * sortOrder;
    }

    // fallback : string localeCompare
    return String(value1).localeCompare(String(value2)) * sortOrder;
}

export function convertListToMapObject(dataList: any[]): { [id: number]: any } {
    const map: { [id: number]: any } = {};
    dataList.forEach(item => map[item.id] = item);
    return map;
}

export function getFieldErrorFromFormBuilder(fieldControl: any) {
    if(fieldControl?.hasError('required')) {
        return 'Field is required';
    }
    else if(fieldControl?.hasError('min')) {
        const minValue = fieldControl.getError('min').min;
        return 'Field value must be >= ' + minValue;
    }
    return '';
}

export function atLeastOneRequiredValidator(controlNames: string[]): ValidatorFn {
    return (group: AbstractControl): ValidationErrors | null => {
        const hasValue = controlNames.some(name => {
            const control = group.get(name);
            return control && control.value && control.value.toString().trim() !== '';
        });

        // reset erreurs sur les champs
        controlNames.forEach(name => {
            const control = group.get(name);
            if (control) {
                control.setErrors(null);
            }
        });

        if (!hasValue) {
            controlNames.forEach(name => {
                const control = group.get(name);
                if (control) {
                control.setErrors({ atLeastOneRequired: true });
                }
            });
            return { atLeastOneRequired: true };
        }

        return null;
    };
}

export type UploadState = 'pending' | 'uploading' | 'success' | 'error';
export interface UploadInfo {
  label: string;
  icon: string;
  severity: 'info' | 'success' | 'warning' | 'danger';
}
export function getUploadInfo(status: UploadState): UploadInfo {
    const map: Record<UploadState, UploadInfo> = {
        pending: {
        label: 'En attente',
        icon: 'pi pi-clock',
        severity: 'warning'
        },
        uploading: {
        label: 'En cours...',
        icon: 'pi pi-spin pi-spinner',
        severity: 'info'
        },
        success: {
        label: 'Validée',
        icon: 'pi pi-check-circle',
        severity: 'success'
        },
        error: {
        label: 'Erreur',
        icon: 'pi pi-times-circle',
        severity: 'danger'
        }
    };

    return map[status];
}

export function mapObjectToAnotherType<T>(data: any, ClassType: new () => T): T {
    const instance = new ClassType();
    
    Object.keys(data).forEach(
        key => {
            if(key in (instance as any))
                (instance as any)[key] = data[key];
        }
    )
    return instance;
}