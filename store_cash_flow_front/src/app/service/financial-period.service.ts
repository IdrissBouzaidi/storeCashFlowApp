import { DatePipe } from "@angular/common";
import { HttpClient, HttpParams } from "@angular/common/http";
import { inject, Injectable } from "@angular/core";
import { FinancialPeriod } from "../models/financial-period";
import { ApiResponse } from "../models/api-response";
import { map, Observable } from "rxjs";
import { CANCEL_PERIOD_URL, CLOSE_CURRENT_PERIOD_URL, GET_FINANCIAL_PERIODS_URL, GET_LAST_PERIOD_URL, IS_SOME_PERIOD_IN_PROGRESS_URL, REOPEN_PERIOD_URL } from "../utils/urls/external-urls";
import { convertTimeToString } from "../utils/functions/date-converer";

@Injectable({
    providedIn: 'root'
})
export class FinancialPeriodService {
    private http = inject(HttpClient);
    private datePipe = inject(DatePipe);

    getFinancialPeriods(startDateMin: Date | undefined, startDateMax: Date | undefined, endDateMin: Date | undefined,
                        endDateMax: Date | undefined, stateId: number | undefined): Observable<FinancialPeriod[]> {
        let params = new HttpParams();
        const startDateMinPipe: string | null = this.datePipe.transform(startDateMin, 'yyyy-MM-dd');
        if(startDateMinPipe) params = params.set("startDateMin", startDateMinPipe);
        const startDateMaxPipe: string | null = this.datePipe.transform(startDateMax, 'yyyy-MM-dd');
        if(startDateMaxPipe) params = params.set("startDateMax", startDateMaxPipe);
        const endDateMinPipe: string | null = this.datePipe.transform(endDateMin, 'yyyy-MM-dd');
        if(endDateMinPipe) params = params.set("endDateMin", endDateMinPipe);
        const endDateMaxPipe: string | null = this.datePipe.transform(endDateMax, 'yyyy-MM-dd');
        if(endDateMaxPipe) params = params.set("endDateMax", endDateMaxPipe);
        if(stateId) params = params.set("stateId", stateId);
        return this.http.get<ApiResponse<FinancialPeriod[]>>(GET_FINANCIAL_PERIODS_URL, {params}).pipe(map((data: any) => data.data));
    }
    
    addFinancialPeriod(label: string, startDate: Date | undefined, startTime: Date | undefined, description: string | undefined): Observable<ApiResponse<FinancialPeriod>> {
        const body = {
            label,
            startDate,
            startTime: startTime? convertTimeToString(startTime): undefined,
            details: description
        }
        return this.http.post<ApiResponse<FinancialPeriod>>(GET_FINANCIAL_PERIODS_URL, body);
    }
    
    isSomePeriodInProgress() {
        return this.http.get<ApiResponse<boolean>>(IS_SOME_PERIOD_IN_PROGRESS_URL).pipe(map((data: any) => data.data));
    }

    getLastPeriod(): Observable<FinancialPeriod> {
        return this.http.get<ApiResponse<FinancialPeriod>>(GET_LAST_PERIOD_URL).pipe(map((data: any) => data.data))
    }
    
    closeCurrentPeriod(endDate: Date | undefined, endTime: Date | undefined) {
        const body = {
            endDate: endDate,
            endTime: endTime? convertTimeToString(endTime): undefined
        }
        return this.http.post<ApiResponse<FinancialPeriod>>(CLOSE_CURRENT_PERIOD_URL, body).pipe(map((data: any) => data.data));
    }
    
    cancel(financialPeriodId: number) {
        debugger
        let params = new HttpParams();
        params = params.set('id', financialPeriodId);
        return this.http.post<ApiResponse<FinancialPeriod>>(CANCEL_PERIOD_URL, {}, {params}).pipe(map(data => data.data));
    }

    reopen(financialPeriodId: number) {
        let params = new HttpParams();
        params = params.set('id', financialPeriodId);
        return this.http.post<ApiResponse<FinancialPeriod>>(REOPEN_PERIOD_URL, {}, {params}).pipe(map(data => data.data));
    }
}