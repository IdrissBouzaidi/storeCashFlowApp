import { HttpClient, HttpParams } from "@angular/common/http";
import { inject, Injectable } from "@angular/core";
import { map, Observable } from "rxjs";
import { Charge } from "../models/charge";
import { CANCEL_CHARGE_URL, CHARGES_URL } from "../utils/urls/external-urls";
import { DatePipe } from "@angular/common";
import { convertTimeToString } from "../utils/functions/date-converer";
import { ApiResponse } from "../models/api-response";

@Injectable({
    providedIn: 'root'
})
export class ChargeService {
    private http = inject(HttpClient);
    private datePipe = inject(DatePipe);

    getChargesList(chargeTypeId: number, minTransactionDate: Date | undefined,maxTransactionDate: Date | undefined,
                    stateId: number, periodId: number, consumedBy: number): Observable<Charge[]> {
        let params: HttpParams = new HttpParams();
        if(chargeTypeId) params = params.set("chargeTypeId", chargeTypeId);
        const formattedDateMin: string | null = this.datePipe.transform(minTransactionDate, 'yyyy-MM-dd');
        if(formattedDateMin) params = params.set("minTransactionDate", formattedDateMin);
        const formattedDateMax: string | null = this.datePipe.transform(maxTransactionDate, 'yyyy-MM-dd');
        if(formattedDateMax) params = params.set("maxTransactionDate", formattedDateMax);
        if(stateId) params = params.set("stateId", stateId);
        if(periodId) params = params.set("periodId", periodId);
        if(consumedBy) params = params.set("consumedBy", consumedBy);
        return this.http.get<Charge[]>(CHARGES_URL, { params });
    }

    addCharge(chargeTypeId: number | undefined, cost: number, shortDesc: string | undefined,
        consumedBy: number | undefined, transactionDate: Date, transactionTime: Date,quantity: number,
        description: string | undefined, imageSrc: string | undefined): Observable<Charge> {
            
        const body = {
            idChargeType: chargeTypeId,
            cost: cost,
            label: shortDesc,
            consumedBy: consumedBy,
            transactionDate: transactionDate,
            transactionTime: convertTimeToString(transactionTime),
            quantity: quantity,
            details: description,
            imageSrc: imageSrc
        }
        debugger;
        return this.http.post<ApiResponse<Charge>>(CHARGES_URL, body).pipe(map(data => data.data!));;
    }
            
    cancelCharge(id: number) {
        let params = new HttpParams();
        params = params.set('id', id);
        return this.http.post<ApiResponse<Charge>>(CANCEL_CHARGE_URL, {}, { params }).pipe(map(data => data.data));
    }
}