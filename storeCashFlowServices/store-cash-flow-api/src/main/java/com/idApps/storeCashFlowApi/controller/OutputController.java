package com.idApps.storeCashFlowApi.controller;

import com.idApps.storeCashFlowApi.models.dto.OutputDto;
import com.idApps.storeCashFlowApi.models.response.ApiResponse;
import com.idApps.storeCashFlowApi.persistence.entity.ConsInputEntity;
import com.idApps.storeCashFlowApi.persistence.entity.OutputEntity;
import com.idApps.storeCashFlowApi.persistence.service.OutputService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/outputs")
public class OutputController {

    @Autowired
    private OutputService outputService;

    @GetMapping
    @SecurityRequirement(name = "keycloak")
    public ResponseEntity<List<OutputDto>> getOutputs(@RequestParam(required = false) Integer productId, @RequestParam(required = false) Integer transactionTypeId,
                                                      @RequestParam(required = false) Integer transactionStateId, @RequestParam(required = false) Integer periodId,
                                                      @RequestParam(required = false) Integer soldById, @RequestParam(required = false) LocalDate transactionDateMin,
                                                      @RequestParam(required = false) LocalDate transactionDateMax) {
        System.out.println("gOutpurController: getOutputs");
        List<OutputDto> outputList = this.outputService.getOutputs(productId, transactionTypeId, transactionStateId, periodId, soldById, transactionDateMin, transactionDateMax);
        return ResponseEntity.ok(outputList);
    }

    @PostMapping
    @SecurityRequirement(name = "keycloak")
    public ResponseEntity<ApiResponse<OutputEntity>> addOutput(@RequestBody OutputDto body, @AuthenticationPrincipal Jwt jwt) {
        return this.outputService.addOutput(body, jwt.getTokenValue());
    }

    @SecurityRequirement(name = "keycloak")
    @PostMapping("cancel")
    public ResponseEntity<ApiResponse<OutputEntity>> cancelOutput(@AuthenticationPrincipal Jwt jwt, @RequestParam(name = "id") int idOutput) {
        return this.outputService.cancelOutput(jwt.getTokenValue(), idOutput);
    }
}
