export class NotConsInput {
    // not_consumable_input table
    id!: number;
    label!: string;
    idInput!: number;
    reusableInputId?: number;

    // input table
    cost!: number;
    initialQuantity?: number;
    remainingQuantity?: number;
    total!: number;
    idTransaction!: number;

    // transaction table
    addingDate!: Date;
    addingTime!: string;
    details?: string;
    idPeriod!: number;
    executedBy!: number;
    imageSrc?: string;
}