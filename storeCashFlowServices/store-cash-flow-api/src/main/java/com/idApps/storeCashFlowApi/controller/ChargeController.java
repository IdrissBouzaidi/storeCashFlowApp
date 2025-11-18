package com.idApps.storeCashFlowApi.controller;

import com.idApps.storeCashFlowApi.models.dto.ChargeDto;
import com.idApps.storeCashFlowApi.models.response.ApiResponse;
import com.idApps.storeCashFlowApi.persistence.entity.AdvanceEntity;
import com.idApps.storeCashFlowApi.persistence.entity.ChargeEntity;
import com.idApps.storeCashFlowApi.persistence.service.ChargeService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.sql.Date;
import java.util.List;

@RestController
@RequestMapping("/api/v1/charges")
public class ChargeController {
    @Autowired
    private ChargeService chargeService;

    @GetMapping
    @SecurityRequirement(name = "keycloak")
    public ResponseEntity<List<ChargeDto>> getCharges(@RequestParam(required = false) Integer chargeTypeId, @RequestParam(required = false) Date minTransactionDate, @RequestParam(required = false) Date maxTransactionDate,
                                                     @RequestParam(required = false) Integer stateId, @RequestParam(required = false) Integer periodId, @RequestParam(required = false) Integer consumedBy) {
        return ResponseEntity.ok(this.chargeService.getCharges(chargeTypeId, minTransactionDate, maxTransactionDate, stateId, periodId, consumedBy));
    }

    @PostMapping
    @SecurityRequirement(name="keycloak")
    public ResponseEntity<ApiResponse<ChargeDto>> addCharge(@AuthenticationPrincipal Jwt jwt, @RequestBody ChargeDto body) {
        return this.chargeService.addCharge(body, jwt.getTokenValue());
    }

    @SecurityRequirement(name = "keycloak")
    @PostMapping("cancel")
    public ResponseEntity<ApiResponse<ChargeEntity>> cancelCharge(@AuthenticationPrincipal Jwt jwt, @RequestParam(name = "id") int idCharge) {
        return this.chargeService.cancelCharge(jwt.getTokenValue(), idCharge);
    }
}
