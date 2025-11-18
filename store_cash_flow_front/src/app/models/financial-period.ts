export class FinancialPeriod {
    id?: number;
    label!: string;
    startDate!: Date;
    startTime!: string;
    endDate!: Date;
    endTime!: string;
    duration!: number;
    details?: string;
    stateId?: number;
}