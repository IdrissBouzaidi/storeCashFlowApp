export class Transaction {
    // transaction table
    id!: number;
    label?: string;
    amount!: number;

    addingDate!: Date;
    addingTime!: string;

    transactionDate!: Date;
    transactionTime!: string;

    details?: string;
    imageSrc?: string;

    idTransactionType!: number;
    idPeriod!: number;
    executedBy!: number;

    // --- Colonnes ajoutées ---
    currentCapital?: number;
    currentProfitGross?: number;
    currentProfitNet?: number;
    totalExpenses?: number;
    totalCustomerCredit?: number;
    totalExternalLoan?: number;
    totalAdvance?: number;
    totalConsumableInputs?: number;
    totalNonConsumableInputs?: number;
    cashRegisterBalance?: number;
    totalOutOfPocketExpenses?: number;
}