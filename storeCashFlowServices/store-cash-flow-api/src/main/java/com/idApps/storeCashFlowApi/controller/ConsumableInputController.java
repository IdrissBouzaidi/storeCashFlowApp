package com.idApps.storeCashFlowApi.controller;

import com.idApps.storeCashFlowApi.models.dto.ConsInputDto;
import com.idApps.storeCashFlowApi.models.dto.OutputDto;
import com.idApps.storeCashFlowApi.models.response.ApiResponse;
import com.idApps.storeCashFlowApi.persistence.entity.ChargeEntity;
import com.idApps.storeCashFlowApi.persistence.entity.ConsInputEntity;
import com.idApps.storeCashFlowApi.persistence.entity.OutputEntity;
import com.idApps.storeCashFlowApi.persistence.service.ConsumableInputService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/consInputs")
public class ConsumableInputController {

    @Autowired
    private ConsumableInputService consumableInputService;

    @SecurityRequirement(name = "keycloak")
    @GetMapping
    public ResponseEntity<List<ConsInputDto>> getConsInputs(@RequestParam(required = false) Integer productId, @RequestParam(required = false) Integer transactionTypeId,
                                                            @RequestParam(required = false) Integer transactionStateId, @RequestParam(required = false) Integer periodId, @RequestParam(required = false) Integer executedById,
                                                            @RequestParam(required = false) LocalDate transactionDateMin, @RequestParam(required = false) LocalDate transactionDateMax) {
        return ResponseEntity.ok(this.consumableInputService.getConsInputs(productId, transactionTypeId, transactionStateId, periodId, executedById, transactionDateMin, transactionDateMax));
    }

    @SecurityRequirement(name = "keycloak")
    @GetMapping("search")
    public  ResponseEntity<List<ConsInputDto>> searchConsInputs(@RequestParam(required = false) String consInputLabel) {
        return ResponseEntity.ok(this.consumableInputService.searchConsInputs(consInputLabel));
    }

    @SecurityRequirement(name = "keycloak")
    @PostMapping
    public ResponseEntity<ApiResponse<ConsInputDto>> addCoonsInput(@AuthenticationPrincipal Jwt jwt, @RequestBody ConsInputDto body) {
        System.out.println("body is: " + body);
        return this.consumableInputService.addConsInput(body, jwt.getTokenValue());
    }

    @PostMapping("/list")
    @SecurityRequirement(name = "keycloak")
    public ResponseEntity<ApiResponse<List<ConsInputEntity>>> addConsInputList(@RequestBody List<ConsInputDto> body, @AuthenticationPrincipal Jwt jwt) {
        return this.consumableInputService.addConsInputList(body, jwt.getTokenValue());
    }

    @SecurityRequirement(name = "keycloak")
    @PostMapping("cancel")
    public ResponseEntity<ApiResponse<ConsInputEntity>> cancelConsumableInput(@AuthenticationPrincipal Jwt jwt, @RequestParam(name = "id") int idConsInput) {
        return this.consumableInputService.cancelConsInput(jwt.getTokenValue(), idConsInput);
    }

}
