import { HttpClient, HttpParams } from "@angular/common/http";
import { inject, Injectable } from "@angular/core";
import { map, Observable } from "rxjs";
import { CapitalContribution } from "../models/capital-contribution";
import { CANCEL_CAPITAL_CONTRIBUTION_URL, CAPITAL_CONTRIBUTIONS_URL } from "../utils/urls/external-urls";
import { DatePipe } from "@angular/common";
import { convertTimeToString } from "../utils/functions/date-converer";
import { ApiResponse } from "../models/api-response";

@Injectable({
    providedIn: 'root'
})
export class CapitalContributionService {
    private http: HttpClient = inject(HttpClient);
    private datePipe = inject(DatePipe);

    getCapitalContributionList(transDateMin: Date | undefined | null, transDateMax: Date | undefined | null, transactionStateId: number | undefined, periodId: number | undefined, contributorId: number | undefined): Observable<CapitalContribution[]> {
        let params = new HttpParams();
        const formattedDateMin: string | null = this.datePipe.transform(transDateMin, 'yyyy-MM-dd');
        if(formattedDateMin) params = params.set("contributionDateMin", formattedDateMin);
        const formattedDateMax: string | null = this.datePipe.transform(transDateMax, 'yyyy-MM-dd');
        if(formattedDateMax) params = params.set("contributionDateMax", formattedDateMax);
        if(transactionStateId) params = params.set("capitalContributionStateId", transactionStateId);
        if(periodId) params = params.set("periodId", periodId);
        if(contributorId) params = params.set("contributorId", contributorId);
        return this.http.get<CapitalContribution[]>(CAPITAL_CONTRIBUTIONS_URL, { params });
    }
    
    addCapitalContribution(amount: number | undefined, contributorId: number | undefined, description: string | undefined, shortDesc: string | undefined, contributionDate: Date, contributionTime: Date): Observable<CapitalContribution> {
        const body = {
            amount: amount,
            contributorId: contributorId,
            details: description,
            label: shortDesc,
            contributionDate: contributionDate,
            contributionTime: convertTimeToString(contributionTime)
        }
        return this.http.post<ApiResponse<CapitalContribution>>(CAPITAL_CONTRIBUTIONS_URL, body).pipe(map(data => data.data!));
    }
            
    cancelCapitalContribution(id: number) {
        let params = new HttpParams();
        params = params.set('id', id);
        return this.http.post<ApiResponse<CapitalContribution>>(CANCEL_CAPITAL_CONTRIBUTION_URL, {}, { params }).pipe(map(data => data.data));
    }
}