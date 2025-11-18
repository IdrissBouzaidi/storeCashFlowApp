import { HttpClient, HttpParams } from "@angular/common/http";
import { inject, Injectable } from "@angular/core";
import { map, Observable } from "rxjs";
import { ConsInput } from "../models/cons-input";
import { CANCEL_OUTPUT_URL, OUTPUTS_URL } from "../utils/urls/external-urls";
import { DatePipe } from "@angular/common";
import { convertTimeToString } from "../utils/functions/date-converer";
import { Output } from "../models/output";
import { ApiResponse } from "../models/api-response";

@Injectable({
        providedIn: 'root'
})
export class OutputService {
    private http = inject(HttpClient);
    private datePipe = inject(DatePipe);

    getOutputsList(productId: number | undefined, transactionStateId: number | undefined, periodId: number | undefined, soldById: number | undefined, transactionDateMin: Date | undefined, transactionDateMax: Date | undefined): Observable<Output[]> {
        let params = new HttpParams();
        if(productId)
            params = params.set("productId", productId);
        if(transactionStateId)
            params = params.set("transactionStateId", transactionStateId);
        if(periodId)
            params = params.set("periodId", periodId);
        if(soldById)
            params = params.set("executedById", soldById);
        const formattedDateMin: string | null = this.datePipe.transform(transactionDateMin, 'yyyy-MM-dd');
        if(formattedDateMin) {
            params = params.set("transactionDateMin", formattedDateMin);
        }
        const formattedDateMax: string | null = this.datePipe.transform(transactionDateMax, 'yyyy-MM-dd');
        if(formattedDateMax) {
            params = params.set("transactionDateMax", formattedDateMax);
        }
        debugger
        return this.http.get<Output[]>(OUTPUTS_URL, { params });
    }

    addOutput(productId: number, consInputId: number, consInputLabel: string | undefined, price: number,
        quantity: number, soldById: number | undefined, stateId: number, transactionDate: Date,
        transactionTime: Date, description: string | undefined): Observable<Output> {
        const body = {
            idProduct: productId,
            idConsInput: consInputId,
            label: consInputLabel,
            unitPrice: price,
            quantity: quantity,
            soldBy: soldById,
            idState: stateId,
            transactionDate: transactionDate,
            transactionTime: convertTimeToString(transactionTime),
            details: description
        }
        return this.http.post<ApiResponse<Output>>(OUTPUTS_URL, body).pipe(map(data => data.data!));
    }
    
    cancelOutput(id: number) {
        let params = new HttpParams();
        params = params.set('id', id);
        return this.http.post<ApiResponse<Output>>(CANCEL_OUTPUT_URL, {}, { params }).pipe(map(data => data.data));
    }
}