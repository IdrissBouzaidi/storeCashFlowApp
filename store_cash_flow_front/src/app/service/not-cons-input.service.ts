import { HttpClient, HttpParams } from "@angular/common/http";
import { inject, Injectable } from "@angular/core";
import { map, Observable } from "rxjs";
import { NotConsInput } from "../models/not-cons-input";
import { DatePipe } from "@angular/common";
import { CANCEL_NOT_CONS_INPUT_URL, NOT_CONS_INPUT_URL } from "../utils/urls/external-urls";
import { convertTimeToString } from "../utils/functions/date-converer";
import { ApiResponse } from "../models/api-response";

@Injectable({
    providedIn: 'root'
})
export class NotConsInputService {
    private http = inject(HttpClient);
    private datePipe = inject(DatePipe);

    getNotConsInputs(transactionDateMin: Date | undefined, transactionDateMax: Date | undefined, periodId: number | undefined, executedById: number | undefined, notConsInputStateId: number | undefined): Observable<NotConsInput[]> {
        let params = new HttpParams();
        const formattedTranDateMin: string | null = this.datePipe.transform(transactionDateMin, 'yyyy-MM-dd');
        if(formattedTranDateMin) params = params.set('transactionDateMin', formattedTranDateMin);
        const formattedTranDateMax: string | null = this.datePipe.transform(transactionDateMax, 'yyyy-MM-dd');
        if(formattedTranDateMax) params = params.set('transactionDateMax', formattedTranDateMax);
        if(periodId) params = params.set('periodId', periodId);
        if(executedById) params = params.set('executedById', executedById);
        if(notConsInputStateId) params = params.set('notConsInputStateId', notConsInputStateId);
        return this.http.get<NotConsInput[]>(NOT_CONS_INPUT_URL, { params });
    }

    addNotConsInput(reusableInputId: number | undefined, cost: number, quantity: number,
        contributorId: number | undefined, shortDescr: string | undefined,
        transactionDate: Date, transactionTime: Date, description: string | undefined,
        imageSrc: string | undefined): Observable<NotConsInput> {
            const body = {
                reusableInputId: reusableInputId,
                cost: cost,
                initialQuantity: quantity,
                contributor: contributorId,
                label: shortDescr,
                transactionDate: transactionDate,
                transactionTime: convertTimeToString(transactionTime),
                details: description,
                imageSrc: imageSrc
            }
            return this.http.post<ApiResponse<NotConsInput>>(NOT_CONS_INPUT_URL, body).pipe(map(data => data.data!));
    }
    
    cancelNotConsInput(id: number) {
        let params = new HttpParams();
        params = params.set('id', id);
        return this.http.post<ApiResponse<NotConsInput>>(CANCEL_NOT_CONS_INPUT_URL, {}, { params }).pipe(map(data => data.data));
    }
}