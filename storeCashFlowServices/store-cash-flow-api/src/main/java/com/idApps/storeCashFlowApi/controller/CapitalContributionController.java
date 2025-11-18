package com.idApps.storeCashFlowApi.controller;


import com.idApps.storeCashFlowApi.models.dto.CapitalContributionDto;
import com.idApps.storeCashFlowApi.models.response.ApiResponse;
import com.idApps.storeCashFlowApi.persistence.entity.AdvanceEntity;
import com.idApps.storeCashFlowApi.persistence.entity.CapitalContributionEntity;
import com.idApps.storeCashFlowApi.persistence.service.CapitalContributionService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.sql.Date;
import java.util.List;

@RestController
@RequestMapping("/api/v1/capitalContributions")
public class CapitalContributionController {
    @Autowired
    private CapitalContributionService capitalContributionService;

    @GetMapping
    @SecurityRequirement(name = "keycloak")
    public ResponseEntity<List<CapitalContributionDto>> getCapitalContributions(@RequestParam(required = false) Date contributionDateMin, @RequestParam(required = false) Date contributionDateMax,
                                                                                @RequestParam(required = false) Integer capitalContributionStateId, @RequestParam(required = false) Integer periodId, @RequestParam(required = false) Integer contributorId) {
        return ResponseEntity.ok(this.capitalContributionService.getCapitalContributions(contributionDateMin, contributionDateMax, capitalContributionStateId, periodId, contributorId));
    }

    @PostMapping
    @SecurityRequirement(name = "keycloak")
    public ResponseEntity<ApiResponse<CapitalContributionEntity>> addCapitalContribution(@RequestBody CapitalContributionDto body, @AuthenticationPrincipal Jwt jwt) {
        return this.capitalContributionService.addCapitalContribution(body, jwt.getTokenValue());
    }

    @SecurityRequirement(name = "keycloak")
    @PostMapping("cancel")
    public ResponseEntity<ApiResponse<CapitalContributionEntity>> cancelCapitalContribution(@AuthenticationPrincipal Jwt jwt, @RequestParam(name = "id") int idCapitalContribution) {
        return this.capitalContributionService.cancelCapitalContribution(jwt.getTokenValue(), idCapitalContribution);
    }
}