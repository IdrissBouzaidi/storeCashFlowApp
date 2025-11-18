package com.idApps.storeCashFlowApi.persistence.service;

import com.idApps.storeCashFlowApi.models.dto.ChargeDto;
import com.idApps.storeCashFlowApi.models.response.ApiResponse;
import com.idApps.storeCashFlowApi.persistence.entity.ChargeEntity;
import org.springframework.http.ResponseEntity;

import java.sql.Date;
import java.util.List;

public interface ChargeService {
    List<ChargeDto> getCharges(Integer chargeTypeId, Date minTransactionDate, Date maxTransactionDate,
                               Integer stateId, Integer periodId, Integer consumedBy);

    ResponseEntity<ApiResponse<ChargeDto>> addCharge(ChargeDto chargeDto, String userAccessToken);

    ResponseEntity<ApiResponse<ChargeEntity>> cancelCharge(String tokenValue, int id);
}
