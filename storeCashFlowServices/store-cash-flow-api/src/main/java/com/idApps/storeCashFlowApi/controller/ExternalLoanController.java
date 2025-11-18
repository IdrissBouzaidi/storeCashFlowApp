package com.idApps.storeCashFlowApi.controller;

import com.idApps.storeCashFlowApi.models.dto.ExternalLoanDto;
import com.idApps.storeCashFlowApi.models.response.ApiResponse;
import com.idApps.storeCashFlowApi.persistence.entity.ConsInputEntity;
import com.idApps.storeCashFlowApi.persistence.entity.ExternalLoanEntity;
import com.idApps.storeCashFlowApi.persistence.service.ExternalLoanService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.sql.Date;
import java.util.List;

@RestController
@RequestMapping("api/v1/externalLoans")
public class ExternalLoanController {
    @Autowired
    private ExternalLoanService externalLoanService;

    @SecurityRequirement(name = "keycloak")
    @GetMapping
    public ResponseEntity<List<ExternalLoanDto>> getExternalLoans(@RequestParam(required = false) Date loanDateMin, @RequestParam(required = false) Date loanDateMax, @RequestParam(required = false) Integer stateId, @RequestParam(required = false) Integer periodId, @RequestParam(required = false) Integer creditorId) {
        return ResponseEntity.ok(this.externalLoanService.getExternalLoans(loanDateMin, loanDateMax, stateId, periodId, creditorId));
    }

    @SecurityRequirement(name = "keycloak")
    @PostMapping
    public ResponseEntity<ApiResponse<ExternalLoanEntity>> addExternalLoan(@RequestBody ExternalLoanDto body, @AuthenticationPrincipal Jwt jwt) {
        return this.externalLoanService.addExternalLoan(body, jwt.getTokenValue());
    }

    @SecurityRequirement(name = "keycloak")
    @PostMapping("cancel")
    public ResponseEntity<ApiResponse<ExternalLoanEntity>> cancelExternalLoan(@AuthenticationPrincipal Jwt jwt, @RequestParam(name = "id") int idExternalLoan) {
        return this.externalLoanService.cancelExternalLoan(jwt.getTokenValue(), idExternalLoan);
    }
}
