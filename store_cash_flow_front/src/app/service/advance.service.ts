import { HttpClient, HttpParams } from "@angular/common/http";
import { inject, Injectable } from "@angular/core";
import { map, Observable } from "rxjs";
import { Advance } from "../models/advance";
import { DatePipe } from "@angular/common";
import { ADVANCES_URL, CANCEL_ADVANCE_URL } from "../utils/urls/external-urls";
import { convertTimeToString } from "../utils/functions/date-converer";
import { ApiResponse } from "../models/api-response";

@Injectable({
    providedIn: 'root'
})
export class AdvanceService {
    private http = inject(HttpClient);
    private datePipe = inject(DatePipe);

    getAdvances(advanceDateMin: Date | undefined, advanceDateMax: Date | undefined, stateId: number | undefined, periodId: number | undefined, takerId: number | undefined): Observable<Advance[]> {
        let params = new HttpParams();
        const advanceDateMinPipe: string | null = this.datePipe.transform(advanceDateMin, 'yyyy-MM-dd');
        if(advanceDateMinPipe)
            params = params.set('advanceDateMin', advanceDateMinPipe);
        const advanceDateMaxPipe: string | null = this.datePipe.transform(advanceDateMax, 'yyyy-MM-dd');
        if(advanceDateMaxPipe)
            params = params.set('advanceDateMax', advanceDateMaxPipe);
        if(stateId) params = params.set('stateId', stateId);
        if(periodId) params = params.set('periodId', periodId);
        if(takerId) params = params.set('takerId', takerId);
        return this.http.get<Advance[]>(ADVANCES_URL, {params});
    }
    
    addAdvance(amount: number | undefined, takerId: number, description: string | undefined, shortDesc: string | undefined, advanceDate: Date, advanceTime: Date): Observable<Advance> {
        const body = {
            amount: amount,
            takerId: takerId,
            details: description,
            label: shortDesc,
            advanceDate: advanceDate,
            advanceTime: convertTimeToString(advanceTime)
        }
        return this.http.post<ApiResponse<Advance>>(ADVANCES_URL, body).pipe(map(data => data.data!));
    }
        
    cancelAdvance(id: number) {
        let params = new HttpParams();
        params = params.set('id', id);
        return this.http.post<ApiResponse<Advance>>(CANCEL_ADVANCE_URL, {}, { params }).pipe(map(data => data.data));
    }
}