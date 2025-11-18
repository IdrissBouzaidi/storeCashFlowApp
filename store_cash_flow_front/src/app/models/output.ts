export class Output {
    id!: number;
    label?: string;
    idProduct?: number;
    quantity?: number;
    unitCost?: number;
    totalCost?: number;
    unitPrice?: number;
    totalPrice?: number;
    unitProfit?: number;
    totalProfit?: number;
    addingDate?: Date;
    addingTime?: string; // en TS, on gère souvent le temps comme string ou Date
    transactionDate!: Date;
    transactionTime!: string;
    imageSrc?: string;
    idTransactionType?: number;
    idState?: number;
    idPeriod!: number;
    soldBy?: number;
    executedBy!: number;
}