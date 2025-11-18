export class CapitalContribution {
    // capital_contribution table
    id!: number;
    label?: string;
    amount!: number;
    contributionDate?: Date;          // ex: new Date("2025-09-01")
    contributionTime?: string;       // ex: "14:30:00"
    idTransaction!: number;
    idState!: number;
    contributorId?: number;
    
    // transaction table
    addingDate!: Date;               // ex: new Date("2025-09-01")
    addingTime!: string;             // ex: "17:45:00"
    details?: string;
    idPeriod!: number;
    executedBy!: number;
}