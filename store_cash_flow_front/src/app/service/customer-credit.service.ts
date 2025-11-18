import { HttpClient, HttpParams } from "@angular/common/http";
import { inject, Injectable } from "@angular/core";
import { map, Observable } from "rxjs";
import { DatePipe } from "@angular/common";
import { CANCEL_CUSTOMER_CREDIT_URL, CUSTOMER_CREDITS_URL } from "../utils/urls/external-urls";
import { convertTimeToString } from "../utils/functions/date-converer";
import { CustomerCredit } from "../models/customer-credit";
import { ApiResponse } from "../models/api-response";

@Injectable({
    providedIn: 'root'
})
export class CustomerCreditService {
    private http = inject(HttpClient);
    private datePipe = inject(DatePipe);

    getCustomerCredits(creditDateMin: Date | undefined, creditDateMax: Date | undefined, stateId: number | undefined, periodId: number | undefined, takerId: number | undefined): Observable<CustomerCredit[]> {
        let params = new HttpParams();
        const creditDateMinPipe: string | null = this.datePipe.transform(creditDateMin, 'yyyy-MM-dd');
        if(creditDateMinPipe)
            params = params.set('creditDateMin', creditDateMinPipe);
        const creditDateMaxPipe: string | null = this.datePipe.transform(creditDateMax, 'yyyy-MM-dd');
        if(creditDateMaxPipe)
            params = params.set('creditDateMax', creditDateMaxPipe);
        if(stateId) params = params.set('stateId', stateId);
        if(periodId) params = params.set('periodId', periodId);
        if(takerId) params = params.set('takerId', takerId);
        return this.http.get<CustomerCredit[]>(CUSTOMER_CREDITS_URL, {params});
    }

    addCustomerCredit(amount: number | undefined, customerId: number, description: string | undefined,
        shortDesc: string | undefined, creditDate: Date, creditTime: Date): Observable<CustomerCredit> {
        const body = {
            initialAmount: amount,
            customerId: customerId,
            details: description,
            label: shortDesc,
            creditDate: creditDate,
            creditTime: convertTimeToString(creditTime)
        }
        return this.http.post<ApiResponse<CustomerCredit>>(CUSTOMER_CREDITS_URL, body).pipe(map(data => data.data!));;
    }

    cancelCustomerCredit(id: number) {
        let params = new HttpParams();
        params = params.set('id', id);
        return this.http.post<ApiResponse<CustomerCredit>>(CANCEL_CUSTOMER_CREDIT_URL, {}, { params }).pipe(map(data => data.data));
    }
}