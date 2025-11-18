package com.idApps.storeCashFlowApi.persistence.service;

import com.idApps.storeCashFlowApi.models.dto.ExternalLoanDto;
import com.idApps.storeCashFlowApi.models.response.ApiResponse;
import com.idApps.storeCashFlowApi.persistence.entity.ExternalLoanEntity;
import org.springframework.http.ResponseEntity;

import java.sql.Date;
import java.util.List;

public interface ExternalLoanService {
    List<ExternalLoanDto> getExternalLoans(Date loanDateMin, Date loanDateMax, Integer stateId, Integer periodId, Integer creditorId);

    ResponseEntity<ApiResponse<ExternalLoanEntity>> addExternalLoan(ExternalLoanDto externalLoanDto, String tokenValue);

    ResponseEntity<ApiResponse<ExternalLoanEntity>> cancelExternalLoan(String tokenValue, int id);
}
