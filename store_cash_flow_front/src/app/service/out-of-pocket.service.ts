import { DatePipe } from "@angular/common";
import { HttpClient, HttpParams } from "@angular/common/http";
import { inject, Injectable } from "@angular/core";
import { CANCEL_OUT_OF_POCKET_URL, OUT_OF_POCKET_URL } from "../utils/urls/external-urls";
import { map, Observable } from "rxjs";
import { OutOfPocket } from "../models/out-of-pocket";
import { convertTimeToString } from "../utils/functions/date-converer";
import { ApiResponse } from "../models/api-response";

@Injectable({
    providedIn: 'root'
})
export class OutOfPocketService {
    private http = inject(HttpClient);
    private datePipe = inject(DatePipe);

    getOutOfPockets(transDateMin: Date | undefined, transDateMax: Date | undefined, stateId: number | undefined, periodId: number | undefined, borrowerId: number | undefined): Observable<OutOfPocket[]> {
        let params = new HttpParams();
        const transDateMinPipe: string | null = this.datePipe.transform(transDateMin, 'yyyy-MM-dd');
        if(transDateMinPipe) params = params.set('transDateMin', transDateMinPipe);
        const transDateMaxPipe = this.datePipe.transform(transDateMax, 'yyyy-MM-dd');
        if(transDateMaxPipe) params = params.set('transDateMax', transDateMaxPipe);
        if(stateId) params = params.set('stateId', stateId);
        if(periodId) params = params.set('periodId', periodId);
        if(borrowerId) params = params.set('borrowerId', borrowerId);
        return this.http.get<OutOfPocket[]>(OUT_OF_POCKET_URL, {params});
    }

    addOutOfPocket(amount: number | undefined, borrowerId: number, description: string | undefined,
            shortDesc: string | undefined, borrowingDate: Date, borrowingTime: Date): Observable<OutOfPocket> {
        const body = {
            amount: amount,
            borrowerId: borrowerId,
            details: description,
            label: shortDesc,
            borrowingDate: borrowerId,
            borrowingTime: convertTimeToString(borrowingTime)
        }
        return this.http.post<ApiResponse<OutOfPocket>>(OUT_OF_POCKET_URL, body).pipe(map(data => data.data!));
    }
    
    cancelOutOfPocket(id: number) {
        let params = new HttpParams();
        params = params.set('id', id);
        return this.http.post<ApiResponse<OutOfPocket>>(CANCEL_OUT_OF_POCKET_URL, {}, { params }).pipe(map(data => data.data));
    }
}