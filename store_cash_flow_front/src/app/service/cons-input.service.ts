import { HttpClient, HttpParams } from "@angular/common/http";
import { inject, Injectable } from "@angular/core";
import { map, Observable } from "rxjs";
import { ConsInput } from "../models/cons-input";
import { ADD_CONS_INPUT_LIST_URL, CANCEL_CONS_INPUT_URL, CONS_INPUTS_URL, SEARCH_CONS_INPUTS_URL } from "../utils/urls/external-urls";
import { DatePipe } from "@angular/common";
import { convertTimeToString } from "../utils/functions/date-converer";
import { FileDetails } from "../models/file-details";
import { mapObjectToAnotherType } from "../utils/functions/helpers";
import { ApiResponse } from "../models/api-response";

@Injectable({
        providedIn: 'root'
})
export class ConsInputService {
    private http = inject(HttpClient);
    private datePipe = inject(DatePipe);

    getConsInputsList(productId: number | undefined, transactionStateId: number | undefined, periodId: number | undefined, executedById: number | undefined, transactionDateMin: Date | undefined, transactionDateMax: Date | undefined): Observable<ConsInput[]> {
        let params = new HttpParams();
        if(productId)
            params = params.set("productId", productId);
        if(transactionStateId)
            params = params.set("transactionStateId", transactionStateId);
        if(periodId)
            params = params.set("periodId", periodId);
        if(executedById)
            params = params.set("executedById", executedById);
        const formattedDateMin: string | null = this.datePipe.transform(transactionDateMin, 'yyyy-MM-dd');
        if(formattedDateMin) {
            params = params.set("transactionDateMin", formattedDateMin);
        }
        const formattedDateMax: string | null = this.datePipe.transform(transactionDateMax, 'yyyy-MM-dd');
        if(formattedDateMax) {
            params = params.set("transactionDateMax", formattedDateMax);
        }
        debugger
        return this.http.get<ConsInput[]>(CONS_INPUTS_URL, { params: params });
    }

    searchConsInputs(consInputLabel: string | undefined): Observable<ConsInput[]> {
        let params = new HttpParams();
        if(consInputLabel) params = params.set("consInputLabel", consInputLabel);
        return this.http.get<ConsInput[]>(SEARCH_CONS_INPUTS_URL, { params })
    }

    addConsInput(productId: number, cost: number, shortDescr: string, transactionDate: Date,
            transactionTime: Date, quantity: number, periodId: number, description: string,
            inputImageSrc: string | undefined, receiptImageSrc: string | undefined): Observable<ConsInput> {
        const body = {
            cost: cost,
            idProduct: productId,
            label: shortDescr,
            transactionDate: transactionDate,
            transactionTime: convertTimeToString(transactionTime),
            initialQuantity: quantity,
            idPeriod: periodId,
            details: description,
            imageSrc: inputImageSrc,
            receiptSrc: receiptImageSrc
        }
        return this.http.post<ApiResponse<ConsInput>>(CONS_INPUTS_URL, body).pipe(map(data => data.data!));
    }

    addConsInputList(list: ConsInput[], imageDetailsMap: { [key: string]: FileDetails }): Observable<void> {
        debugger
        const listToSend = list.map((item: any) => {
            const convertedItem: ConsInput = mapObjectToAnotherType(item, ConsInput);
            convertedItem.id = undefined;
            convertedItem.imageSrc = imageDetailsMap[item.image?.imageDetailsKey]?.fileName;
            convertedItem.receiptSrc = imageDetailsMap[item.receipt?.imageDetailsKey]?.fileName;
            convertedItem.initialQuantity = item.quantity;
            convertedItem.transactionTime = convertTimeToString(item.transactionTime);
            return convertedItem;
        });
        debugger;
        return this.http.post<void>(ADD_CONS_INPUT_LIST_URL, listToSend);
    }
            
    cancelConsInput(id: number) {
        let params = new HttpParams();
        params = params.set('id', id);
        return this.http.post<ApiResponse<ConsInput>>(CANCEL_CONS_INPUT_URL, {}, { params }).pipe(map(data => data.data));
    }

}