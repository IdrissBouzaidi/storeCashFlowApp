import { HttpClient, HttpParams } from "@angular/common/http";
import { inject, Injectable } from "@angular/core";
import { map, Observable } from "rxjs";
import { CANCEL_PRODUCT_URL, GET_PRODUCTS_URL } from "../utils/urls/external-urls";
import { DatePipe } from "@angular/common";
import { ApiResponse } from "../models/api-response";
import { Product } from "../models/product";

@Injectable({
        providedIn: 'root'
})
export class ProductService {
    private http = inject(HttpClient);
    private datePipe = inject(DatePipe);

    getProductsList(addingDateMin: Date | undefined, addingDateMax: Date | undefined,stateId: number | undefined,
                        creatorId: number | undefined, categoryId: number | undefined): Observable<Product[]> {
        let params = new HttpParams();

        const formattedDateMin: string | null = this.datePipe.transform(addingDateMin, 'yyyy-MM-dd');
        if(formattedDateMin)
            params = params.set("addingDateMin", formattedDateMin);
        const formattedDateMax: string | null = this.datePipe.transform(addingDateMax, 'yyyy-MM-dd');
        if(formattedDateMax) {
            params = params.set("addingDateMax", formattedDateMax);
        }
        if(stateId)
            params = params.set("stateId", stateId);
        if(creatorId)
            params = params.set("creatorId", creatorId);
        if(categoryId)
            params = params.set("categoryId", categoryId);
        debugger
        return this.http.get<ApiResponse<Product[]>>(GET_PRODUCTS_URL, { params }).pipe(map(item => item.data!));
    }

    addProduct(shortDesc: string, description: string, imageSrc: string | undefined, categoryIdList: number[] | undefined) {
        const body = {
            label: shortDesc,
            details: description,
            imageSrc,
            categoryIdList
        }
        return this.http.post<ApiResponse<Product>>(GET_PRODUCTS_URL, body).pipe(map(item => item.data));
    }
    
    cancelProduct(id: number) {
        let params = new HttpParams();
        params = params.set('id', id);
        return this.http.post<ApiResponse<Product>>(CANCEL_PRODUCT_URL, {}, { params }).pipe(map(data => data.data));
    }
}