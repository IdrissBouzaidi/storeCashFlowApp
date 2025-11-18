import { DatePipe } from "@angular/common";
import { HttpClient, HttpParams } from "@angular/common/http";
import { inject, Injectable } from "@angular/core";
import { TRANSACTIIONS_URL } from "../utils/urls/external-urls";
import { Transaction } from "../models/transaction";
import { Observable } from "rxjs";

@Injectable({
    providedIn: 'root'
})
export class TransactionService {
    private http = inject(HttpClient);
    private datePipe = inject(DatePipe)

    getTransactions(idTransactionType: number | undefined, idPeriod: number | undefined, executedBy: number | undefined,
                    transactionDateMin: Date | undefined, transactionDateMax: Date | undefined): Observable<Transaction[]> {
        let params = new HttpParams();
        if(idTransactionType)
            params = params.set('idTransactionType', idTransactionType);
        if(idPeriod)
            params = params.set('idPeriod', idPeriod);
        if(executedBy)
            params = params.set('executedBy', executedBy);
        const transactionDateMinPipe = this.datePipe.transform(transactionDateMin, 'yyyy-MM-dd');
        if(transactionDateMinPipe)
            params = params.set('transactionDateMin', transactionDateMinPipe)
        const transactionDateMaxPipe = this.datePipe.transform(transactionDateMax, 'yyyy-MM-dd');
        if(transactionDateMaxPipe)
            params = params.set('transactionDateMax', transactionDateMaxPipe);
        return this.http.get<Transaction[]>(TRANSACTIIONS_URL, { params });
    }
}