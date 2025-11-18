package com.idApps.storeCashFlowApi.persistence.service;

import com.idApps.storeCashFlowApi.models.dto.CapitalContributionDto;
import com.idApps.storeCashFlowApi.models.response.ApiResponse;
import com.idApps.storeCashFlowApi.persistence.entity.CapitalContributionEntity;
import org.springframework.http.ResponseEntity;

import java.sql.Date;
import java.util.List;

public interface CapitalContributionService {
    List<CapitalContributionDto> getCapitalContributions(Date contributionDateMin, Date contributionDateMax, Integer capitalContributionStateId, Integer periodId, Integer contributorId);

    ResponseEntity<ApiResponse<CapitalContributionEntity>> addCapitalContribution(CapitalContributionDto capitalContributionDto, String userAccessToken);

    ResponseEntity<ApiResponse<CapitalContributionEntity>> cancelCapitalContribution(String tokenValue, int id);
}