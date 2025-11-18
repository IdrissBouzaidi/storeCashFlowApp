import { HttpClient, HttpParams } from "@angular/common/http";
import { inject, Injectable } from "@angular/core";
import { map, Observable } from "rxjs";
import { DatePipe } from "@angular/common";
import { CANCEL_EXTERNAL_LOAN_URL, EXTERNAL_LOANS_URL } from "../utils/urls/external-urls";
import { convertTimeToString } from "../utils/functions/date-converer";
import { ExternalLoan } from "../models/external-loan";
import { ApiResponse } from "../models/api-response";

@Injectable({
    providedIn: 'root'
})
export class ExternalLoanService {
    private http = inject(HttpClient);
    private datePipe = inject(DatePipe);

    getExternalLoans(loanDateMin: Date | undefined, loanDateMax: Date | undefined, stateId: number | undefined, periodId: number | undefined, creditorId: number | undefined): Observable<ExternalLoan[]> {
        let params = new HttpParams();
        const loanDateMinPipe: string | null = this.datePipe.transform(loanDateMin, 'yyyy-MM-dd');
        if(loanDateMinPipe)
            params = params.set('loanDateMin', loanDateMinPipe);
        const loanDateMaxPipe: string | null = this.datePipe.transform(loanDateMax, 'yyyy-MM-dd');
        if(loanDateMaxPipe)
            params = params.set('loanDateMax', loanDateMaxPipe);
        if(stateId) params = params.set('stateId', stateId);
        if(periodId) params = params.set('periodId', periodId);
        if(creditorId) params = params.set('takerId', creditorId);
        return this.http.get<ExternalLoan[]>(EXTERNAL_LOANS_URL, {params});
    }

    addExternalLoan(amount: number | undefined, creditorId: number, description: string | undefined,
            shortDesc: string | undefined, loanDate: Date, loanTime: Date): Observable<ExternalLoan> {
        const body = {
            initialAmount: amount,
            creditorId: creditorId,
            details: description,
            label: shortDesc,
            loanDate: loanDate,
            loanTime: convertTimeToString(loanTime)
        }
        return this.http.post<ApiResponse<ExternalLoan>>(EXTERNAL_LOANS_URL, body).pipe(map(data => data.data!));;
    }
    
    cancelExternalLoan(id: number) {
        let params = new HttpParams();
        params = params.set('id', id);
        return this.http.post<ApiResponse<ExternalLoan>>(CANCEL_EXTERNAL_LOAN_URL, {}, { params }).pipe(map(data => data.data));
    }
}