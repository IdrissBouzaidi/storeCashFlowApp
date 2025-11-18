export class ConsInput {
    id?: number;
    label?: string;
    idProduct?: number;

    receiptSrc?: string;

    cost?: number;
    initialQuantity?: number;
    remainingQuantity?: number;
    total?: number;

    addingDate?: Date;
    addingTime?: string; // en TS, on gère souvent le temps comme string ou Date
    transactionDate!: Date;
    transactionTime!: string;

    imageSrc?: string;
    idTransactionType?: number;
    idState?: number;
    idPeriod!: number;
    executedBy!: number;

    constructor(data?: any) {
        if(typeof data === 'number') {
            this.id = data;
        }
    }
}