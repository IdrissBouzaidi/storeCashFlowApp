export class RefTable {
    id!: number;
    code?: string;
    label?: string;

    constructor(label: string) {
        this.label = label;
    }
}