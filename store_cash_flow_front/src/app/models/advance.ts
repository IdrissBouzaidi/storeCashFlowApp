export class Advance {
    // advance table
    id!: number;
    label!: string;
    amount!: number;
    idTransaction!: number;
    stateId!: number;
    advanceDate?: Date;          // ex: new Date("2025-09-01")
    advanceTime?: string;        // ex: "14:30:00"
    takerId?: number;

    // transaction table
    addingDate!: Date;           // ex: new Date("2025-09-01")
    addingTime!: string;         // ex: "17:45:00"
    details?: string;
    idPeriod!: number;
    executedBy!: number;
}