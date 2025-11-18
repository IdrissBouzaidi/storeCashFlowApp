package com.idApps.storeCashFlowApi.persistence.service;

import com.idApps.storeCashFlowApi.models.dto.OutOfPocketDto;
import com.idApps.storeCashFlowApi.models.response.ApiResponse;
import com.idApps.storeCashFlowApi.persistence.entity.OutOfPocketEntity;
import org.springframework.http.ResponseEntity;

import java.sql.Date;
import java.util.List;

public interface OutOfPocketService {
    List<OutOfPocketDto> getOutOfPockets(Date borrowingDateMin, Date borrowingDateMax, Integer stateId, Integer idPeriod, Integer borrowerId);

    ResponseEntity<ApiResponse<OutOfPocketEntity>> addOutOfPocket(String currentUserToken, OutOfPocketDto outOfPocketDto);

    ResponseEntity<ApiResponse<OutOfPocketEntity>> cancelOutOfPocket(String tokenValue, int id);
}
