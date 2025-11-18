package com.idApps.storeCashFlowApi.persistence.service;

import com.idApps.storeCashFlowApi.models.dto.ConsInputDto;
import com.idApps.storeCashFlowApi.models.response.ApiResponse;
import com.idApps.storeCashFlowApi.persistence.entity.ConsInputEntity;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ConsumableInputService {

    List<ConsInputDto> getConsInputs(Integer productId, Integer transactionTypeId, Integer transactionStateId, Integer periodId, Integer executedById, LocalDate transactionDateMin, LocalDate transactionDateMax);

    ConsInputDto getConsInputById(Integer consInputId, List<String> fields);

    Optional<ConsInputEntity> findById(Integer consInputId);

    List<ConsInputDto> searchConsInputs(String consInputLabel);

    ResponseEntity<ApiResponse<ConsInputDto>> addConsInput(ConsInputDto constInput, String userAccessToken);

    ConsInputEntity save(ConsInputEntity consInputEntity);

    ResponseEntity<ApiResponse<List<ConsInputEntity>>> addConsInputList(List<ConsInputDto> body, String tokenValue);

    ResponseEntity<ApiResponse<ConsInputEntity>> cancelConsInput(String tokenValue, int id);
}
