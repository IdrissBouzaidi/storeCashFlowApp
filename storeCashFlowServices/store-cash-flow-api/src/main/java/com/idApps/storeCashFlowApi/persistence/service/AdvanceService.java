package com.idApps.storeCashFlowApi.persistence.service;

import com.idApps.storeCashFlowApi.models.dto.AdvanceDto;
import com.idApps.storeCashFlowApi.models.response.ApiResponse;
import com.idApps.storeCashFlowApi.persistence.entity.AdvanceEntity;
import org.springframework.http.ResponseEntity;

import java.sql.Date;
import java.util.List;

public interface AdvanceService {
    List<AdvanceDto> getAdvances(Date advanceDateMin, Date advanceDateMax, Integer stateId, Integer periodId, Integer takerId);

    ResponseEntity<ApiResponse<AdvanceEntity>> addAdvance(String tokenValue, AdvanceDto advanceDto);

    ResponseEntity<ApiResponse<AdvanceEntity>> cancelAdvance(String tokenValue, int idAdvance);
}