export class CustomerCredit {
    // Customer table
    id!: number;
    label!: string;
    initialAmount!: number;      // correspond à BigDecimal
    paidAmount!: number;         // correspond à BigDecimal
    remainingAmount!: number;    // correspond à BigDecimal
    idTransaction!: number;
    stateId!: number;
    creditDate?: Date;           // ex: new Date("2025-09-01")
    creditTime?: string;         // ex: "14:30:00"
    customerId?: number;

    // Transaction table
    addingDate!: Date;           // ex: new Date("2025-09-01")
    addingTime!: string;         // ex: "17:45:00"
    details?: string;
    idPeriod!: number;
    executedBy!: number;
}