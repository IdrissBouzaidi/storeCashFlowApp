package com.idApps.storeCashFlowApi.persistence.service;

import com.idApps.storeCashFlowApi.models.dto.ConsInputDto;
import com.idApps.storeCashFlowApi.models.dto.FinancialPeriodDto;
import com.idApps.storeCashFlowApi.models.response.ApiResponse;
import com.idApps.storeCashFlowApi.persistence.entity.FinancialPeriodEntity;
import org.springframework.http.ResponseEntity;

import java.sql.Date;
import java.sql.Time;
import java.util.List;
import java.util.Optional;

public interface FinancialPeriodService {

    List<FinancialPeriodDto> getFinancialPeriods(Date startDateMin, Date startDateMax, Date endDateMin, Date endDateMax, Integer stateId);

    ResponseEntity<ApiResponse<FinancialPeriodEntity>> addFinancialPeriod(String userAccessToken, FinancialPeriodDto financialPeriodDto);

    Integer getActivePeriodId();

    Boolean isSomePeriodInProgress();

    ResponseEntity<ApiResponse<FinancialPeriodEntity>> closeCurrentPeriod(String userAccessToken, Date endDate, Time endTime);

    Optional<FinancialPeriodEntity> getLastPeriod();

    ResponseEntity<ApiResponse<FinancialPeriodEntity>> cancel(String tokenValue, int id);

    ResponseEntity<ApiResponse<FinancialPeriodEntity>> reopen(String tokenValue, int id);
}
