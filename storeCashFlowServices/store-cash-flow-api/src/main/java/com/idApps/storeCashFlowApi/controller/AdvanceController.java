package com.idApps.storeCashFlowApi.controller;

import com.idApps.storeCashFlowApi.models.dto.AdvanceDto;
import com.idApps.storeCashFlowApi.models.response.ApiResponse;
import com.idApps.storeCashFlowApi.persistence.entity.AdvanceEntity;
import com.idApps.storeCashFlowApi.persistence.service.AdvanceService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.sql.Date;
import java.util.List;

@RestController
@RequestMapping("api/v1/advances")
public class AdvanceController {
    @Autowired
    private AdvanceService advanceService;

    @SecurityRequirement(name = "keycloak")
    @GetMapping
    public ResponseEntity<List<AdvanceDto>> getAdvances(@RequestParam(required = false) Date advanceDateMin, @RequestParam(required = false) Date advanceDateMax, @RequestParam(required = false) Integer stateId, @RequestParam(required = false) Integer periodId, @RequestParam(required = false) Integer takerId) {
        return ResponseEntity.ok(this.advanceService.getAdvances(advanceDateMin, advanceDateMax, stateId, periodId, takerId));
    }

    @SecurityRequirement(name = "keycloak")
    @PostMapping
    public ResponseEntity<ApiResponse<AdvanceEntity>> addAdvance(@AuthenticationPrincipal Jwt jwt, @RequestBody AdvanceDto advanceDto) {
        return this.advanceService.addAdvance(jwt.getTokenValue(), advanceDto);
    }

    @SecurityRequirement(name = "keycloak")
    @PostMapping("cancel")
    public ResponseEntity<ApiResponse<AdvanceEntity>> cancelAdvance(@AuthenticationPrincipal Jwt jwt, @RequestParam(name = "id") int idAdvance) {
        return this.advanceService.cancelAdvance(jwt.getTokenValue(), idAdvance);
    }
}
