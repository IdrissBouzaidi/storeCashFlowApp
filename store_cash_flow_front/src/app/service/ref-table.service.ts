import { HttpClient } from "@angular/common/http";
import { inject, Injectable } from "@angular/core";
import { Observable } from "rxjs";
import { RefTable } from "../models/ref-table";
import { GET_ADVANCE_STATES_REF_TABLE_URL, GET_CAPITAL_CONTRIBUTION_TRANSACTION_STATES_REF_TABLE_URL, GET_CATEGORIES_REF_TABLE_URL, GET_CATEGORY_STATES_REF_TABLE_URL, GET_CHARGE_TYPES_URL, GET_CHARGES_TRANSACTION_STATES_REF_TABLE_URL, GET_CUSTOMER_CREDITS_STATES_REF_TABLE_URL, GET_CUSTOMERS_REF_TABLE_URL, GET_EXTERNAL_LOAN_STATES_REF_TABLE_URL, GET_NOT_CONS_INPUT_STATES_REF_TABLE_URL, GET_OUT_OF_POCKET_STATES_REF_TABLE_URL, GET_PERIODS_REF_TABLE_URL, GET_PERIODS_STATES_REF_TABLE_URL, GET_PRODUCT_STATES_REF_TABLE_URL, GET_PRODUCTS_REF_TABLE_URL, GET_PRODUCTS_TRANSACTION_STATES_REF_TABLE_URL, GET_REUSABLE_INPUTS_REF_TABLE_URL, GET_TRANSACTION_TYPES_REF_TABLE_URL, GET_USERS_REF_TABLE_URL } from "../utils/urls/external-urls";

@Injectable({
    providedIn: 'root',
})
export class RefTableService {
    private http = inject(HttpClient);
    
    getTransactionTypesRefTable(): Observable<RefTable[]> {
        return this.http.get<RefTable[]>(GET_TRANSACTION_TYPES_REF_TABLE_URL);
    }

    getPeriodsRefTable(): Observable<RefTable[]> {
        return this.http.get<RefTable[]>(GET_PERIODS_REF_TABLE_URL);
    }

    getPeriodsStatesRefTable(): Observable<RefTable[]> {
        return this.http.get<RefTable[]>(GET_PERIODS_STATES_REF_TABLE_URL);
    }

    getUsersRefTable(): Observable<RefTable[]> {
        return this.http.get<RefTable[]>(GET_USERS_REF_TABLE_URL);
    }

    getProductsTransactionStatesRefTable(): Observable<RefTable[]> {
        return this.http.get<RefTable[]>(GET_PRODUCTS_TRANSACTION_STATES_REF_TABLE_URL);
    }

    getChargesTransactionStatesRefTable(): Observable<RefTable[]> {
        return this.http.get<RefTable[]>(GET_CHARGES_TRANSACTION_STATES_REF_TABLE_URL);
    }

    getCapitalContributionTransactionStatesRefTable(): Observable<RefTable[]> {
        return this.http.get<RefTable[]>(GET_CAPITAL_CONTRIBUTION_TRANSACTION_STATES_REF_TABLE_URL);
    }

    getAdvanceTransactionStatesRefTable(): Observable<RefTable[]> {
        return this.http.get<RefTable[]>(GET_ADVANCE_STATES_REF_TABLE_URL);
    }

    getOutOfPocketStatesRefTable(): Observable<RefTable[]> {
        return this.http.get<RefTable[]>(GET_OUT_OF_POCKET_STATES_REF_TABLE_URL);
    }

    getProductsRefTable(): Observable<RefTable[]> {
        return this.http.get<RefTable[]>(GET_PRODUCTS_REF_TABLE_URL);
    }

    getChargeTypesRefTable(): Observable<RefTable[]> {
        return this.http.get<RefTable[]>(GET_CHARGE_TYPES_URL);
    }

    getReusableInputsRefTable(): Observable<RefTable[]> {
        return this.http.get<RefTable[]>(GET_REUSABLE_INPUTS_REF_TABLE_URL);
    }

    getNotConsInputStatesRefTable(): Observable<RefTable[]> {
        return this.http.get<RefTable[]>(GET_NOT_CONS_INPUT_STATES_REF_TABLE_URL);
    }

    getCustomerCreditStatesRefTable(): Observable<RefTable[]> {
        return this.http.get<RefTable[]>(GET_CUSTOMER_CREDITS_STATES_REF_TABLE_URL);
    }

    getExternalLoanStatesRefTable(): Observable<RefTable[]> {
        return this.http.get<RefTable[]>(GET_EXTERNAL_LOAN_STATES_REF_TABLE_URL);
    }

    getCustomersRefTable(): Observable<RefTable[]> {
        return this.http.get<RefTable[]>(GET_CUSTOMERS_REF_TABLE_URL);
    }

    getProductStatesRefTable(): Observable<RefTable[]> {
        return this.http.get<RefTable[]>(GET_PRODUCT_STATES_REF_TABLE_URL);
    }

    getCategoryStatesRefTable(): Observable<RefTable[]> {
        return this.http.get<RefTable[]>(GET_CATEGORY_STATES_REF_TABLE_URL);
    }

    getCategoriesRefTable(): Observable<RefTable[]> {
        return this.http.get<RefTable[]>(GET_CATEGORIES_REF_TABLE_URL);
    }
}