export class Charge {
  id!: number;
  label?: string;
  idChargeType?: number;

  quantity?: number;
  cost?: number;
  total?: number;

  idState?: number;
  idTransaction!: number;
  consumedBy?: number;
  addingDate?: Date;
  addingTime?: string;
  transactionDate?: Date;
  transactionTime?: string;
  details?: string;
  imageSrc?: string;
  idPeriod!: number;
  executedBy!: number;
}