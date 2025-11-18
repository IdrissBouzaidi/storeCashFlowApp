package com.idApps.storeCashFlowApi.persistence.service;

import com.idApps.storeCashFlowApi.models.dto.NotConsInputDto;
import com.idApps.storeCashFlowApi.models.response.ApiResponse;
import com.idApps.storeCashFlowApi.persistence.entity.NotConsInputEntity;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.List;

public interface NotConsInputService {
    List<NotConsInputDto> getNotConsInputs(LocalDate transactionDateMin, LocalDate transactionDateMax, Integer periodId, Integer executedById, Integer notConsInputStateId);

    ResponseEntity<ApiResponse<NotConsInputEntity>> addNotConsInput(String userAccessToken, NotConsInputDto notConsInputDto);

    ResponseEntity<ApiResponse<NotConsInputEntity>> cancelNotConsInput(String tokenValue, int id);
}
