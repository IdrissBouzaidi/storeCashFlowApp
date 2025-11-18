package com.idApps.storeCashFlowApi.controller;

import com.idApps.storeCashFlowApi.models.dto.FinancialPeriodDto;
import com.idApps.storeCashFlowApi.models.response.ApiResponse;
import com.idApps.storeCashFlowApi.persistence.entity.ConsInputEntity;
import com.idApps.storeCashFlowApi.persistence.entity.FinancialPeriodEntity;
import com.idApps.storeCashFlowApi.persistence.service.FinancialPeriodService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.sql.Date;
import java.sql.Time;
import java.util.List;

@RestController
@RequestMapping("/api/v1/financialPeriods")
public class FinancialPeriodController {
    @Autowired
    private FinancialPeriodService financialPeriodService;

    @SecurityRequirement(name = "keycloak")
    @GetMapping("activeId")
    public ResponseEntity<ApiResponse<Integer>> getActivePeriodId() {
        return ResponseEntity.ok(new ApiResponse(this.financialPeriodService.getActivePeriodId()));
    }

    @SecurityRequirement(name = "keycloak")
    @GetMapping
    public ResponseEntity<ApiResponse<List<FinancialPeriodDto>>> getFinancialPeriods(@RequestParam(required = false) Date startDateMin, @RequestParam(required = false) Date startDateMax, @RequestParam(required = false) Date endDateMin,
                                                                                     @RequestParam(required = false) Date endDateMax, @RequestParam(required = false) Integer stateId) {
        return ResponseEntity.ok(new ApiResponse<>(this.financialPeriodService.getFinancialPeriods(startDateMin, startDateMax, endDateMin, endDateMax, stateId)));
    }

    @SecurityRequirement(name = "keycloak")
    @PostMapping
    public ResponseEntity<ApiResponse<FinancialPeriodEntity>> addFinancialPeriod(@AuthenticationPrincipal Jwt jwt, @RequestBody FinancialPeriodDto financialPeriodDto) {
        return this.financialPeriodService.addFinancialPeriod(jwt.getTokenValue(), financialPeriodDto);
    }

    @SecurityRequirement(name = "keycloak")
    @PostMapping("closeCurrent")
    public ResponseEntity<ApiResponse<FinancialPeriodEntity>> closeCurrentPeriod(@AuthenticationPrincipal Jwt jwt, @RequestBody FinancialPeriodDto financialPeriodDto) {
        return this.financialPeriodService.closeCurrentPeriod(jwt.getTokenValue(), financialPeriodDto.getStartDate(), financialPeriodDto.getStartTime());
    }

    @SecurityRequirement(name = "keycloak")
    @GetMapping("isSomePeriodInProgress")
    public ResponseEntity<ApiResponse<Boolean>> isSomePeriodInProgress() {
        return ResponseEntity.ok(new ApiResponse<>(this.financialPeriodService.isSomePeriodInProgress()));
    }

    @SecurityRequirement(name = "keycloak")
    @GetMapping("last")
    public ResponseEntity<ApiResponse<FinancialPeriodEntity>> getLastPeriod() {
        return ResponseEntity.ok(new ApiResponse<>(this.financialPeriodService.getLastPeriod().orElse(null)));
    }

    @SecurityRequirement(name = "keycloak")
    @PostMapping("cancel")
    public ResponseEntity<ApiResponse<FinancialPeriodEntity>> cancel(@AuthenticationPrincipal Jwt jwt, @RequestParam int id) {
        return this.financialPeriodService.cancel(jwt.getTokenValue(), id);
    }

    @SecurityRequirement(name = "keycloak")
    @PostMapping("reopen")
    public ResponseEntity<ApiResponse<FinancialPeriodEntity>> reopen(@AuthenticationPrincipal Jwt jwt, @RequestParam int id) {
        return this.financialPeriodService.reopen(jwt.getTokenValue(), id);
    }
}
