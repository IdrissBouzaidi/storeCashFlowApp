package com.idApps.storeCashFlowApi.controller;

import com.idApps.storeCashFlowApi.models.dto.OutOfPocketDto;
import com.idApps.storeCashFlowApi.models.response.ApiResponse;
import com.idApps.storeCashFlowApi.persistence.entity.ConsInputEntity;
import com.idApps.storeCashFlowApi.persistence.entity.OutOfPocketEntity;
import com.idApps.storeCashFlowApi.persistence.service.OutOfPocketService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.sql.Date;
import java.util.List;

@RestController
@RequestMapping("api/v1/outOfPockets")
public class OutOfPocketController {
    @Autowired
    private OutOfPocketService outOfPocketService;

    @SecurityRequirement(name = "keycloak")
    @GetMapping
    public ResponseEntity<List<OutOfPocketDto>> getOutOfPockets(@RequestParam(required = false) Date borrowingDateMin, @RequestParam(required = false) Date borrowingDateMax, @RequestParam(required = false) Integer stateId, @RequestParam(required = false) Integer idPeriod, @RequestParam(required = false) Integer borrowerId) {
        return ResponseEntity.ok(outOfPocketService.getOutOfPockets(borrowingDateMin, borrowingDateMax, stateId, idPeriod, borrowerId));
    }

    @SecurityRequirement(name = "keycloak")
    @PostMapping
    public ResponseEntity<ApiResponse<OutOfPocketEntity>> addOutOfPocket(@RequestBody OutOfPocketDto body, @AuthenticationPrincipal Jwt jwt) {
        System.out.println("addCapitalContribution");
        return outOfPocketService.addOutOfPocket(jwt.getTokenValue(), body);
    }

    @SecurityRequirement(name = "keycloak")
    @PostMapping("cancel")
    public ResponseEntity<ApiResponse<OutOfPocketEntity>> cancelOutOfPocket(@AuthenticationPrincipal Jwt jwt, @RequestParam(name = "id") int idOutOfPocket) {
        return this.outOfPocketService.cancelOutOfPocket(jwt.getTokenValue(), idOutOfPocket);
    }
}
