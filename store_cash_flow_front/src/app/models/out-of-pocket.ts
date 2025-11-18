export class OutOfPocket {
    // out_of_pocket table
    id!: number;
    label?: string;
    amount!: number;
    idTransaction!: number;
    stateId!: number;
    borrowingDate?: Date;             // ex: new Date("2025-09-12")
    borrowingTime?: string;           // ex: "14:28:07"
    borrowerId?: number;

    // transaction table
    addingDate!: Date;                // ex: new Date("2025-09-12")
    addingTime!: string;              // ex: "17:45:00"
    details?: string;
    idPeriod!: number;
    executedBy!: number;
}