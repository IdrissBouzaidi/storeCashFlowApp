package com.idApps.storeCashFlowApi.persistence.service;

import com.idApps.storeCashFlowApi.models.dto.OutputDto;
import com.idApps.storeCashFlowApi.models.response.ApiResponse;
import com.idApps.storeCashFlowApi.persistence.entity.OutputEntity;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.List;

public interface OutputService {
    List<OutputDto> getOutputs(Integer productId, Integer transactionTypeId, Integer transactionStateId, Integer periodId,
                               Integer soldById, LocalDate transactionDateMin, LocalDate transactionDateMax);

    ResponseEntity<ApiResponse<OutputEntity>> addOutput(OutputDto outputDto, String userAccessToken);

    ResponseEntity<ApiResponse<OutputEntity>> cancelOutput(String tokenValue, int id);
}
