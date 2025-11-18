package com.idApps.storeCashFlowApi.persistence.service;

import com.idApps.storeCashFlowApi.models.dto.RefTableDto;

import java.util.List;

public interface RefTableService {
    List<RefTableDto> getTransactionTypesRefTable();

    List<RefTableDto> getFinancialPeriodsRefTable();

    List<RefTableDto> getUsersRefTable();

    List<RefTableDto> getProductsTransactionStatesRefTable();

    List<RefTableDto> getChargesTransactionStatesRefTable();

    List<RefTableDto> getCapitalContributionTransactionStatesRefTable();

    List<RefTableDto> getChargeTypesRefTable();

    List<RefTableDto> getProductsRefTable();

    List<RefTableDto> getProductStatesRefTable();

    List<RefTableDto> getCategoriesRefTable();

    List<RefTableDto> getCategoryStatesRefTable();

    List<RefTableDto> getReusableInputsRefTable();

    List<RefTableDto> getNotConsInputStatesRefTable();

    List<RefTableDto> getAdvanceStatesRefTable();

    List<RefTableDto> getOutOfPocketStatesRefTable();

    List<RefTableDto> getCustomersRefTable();

    List<RefTableDto> getCustomerCreditStatesRefTable();

    List<RefTableDto> getExternalLoanStatesRefTable();

    List<RefTableDto> getPeriodStatesRefTable();
}
