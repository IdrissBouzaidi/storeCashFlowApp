package com.idApps.storeCashFlowApi.controller;

import com.idApps.storeCashFlowApi.models.dto.NotConsInputDto;
import com.idApps.storeCashFlowApi.models.response.ApiResponse;
import com.idApps.storeCashFlowApi.persistence.entity.ConsInputEntity;
import com.idApps.storeCashFlowApi.persistence.entity.NotConsInputEntity;
import com.idApps.storeCashFlowApi.persistence.service.NotConsInputService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/notConsInputs")
public class NotConsInputController {

    @Autowired
    private NotConsInputService notConsInputService;

    @SecurityRequirement(name = "keycloak")
    @GetMapping
    public ResponseEntity<List<NotConsInputDto>> getNotConsInputs(@RequestParam(required = false) LocalDate transactionDateMin, @RequestParam(required = false) LocalDate transactionDateMax,
                                                                  @RequestParam(required = false) Integer periodId, @RequestParam(required = false) Integer executedById,
                                                                  @RequestParam(required = false) Integer notConsInputStateId) {
        return ResponseEntity.ok(this.notConsInputService.getNotConsInputs(transactionDateMin, transactionDateMax, periodId, executedById, notConsInputStateId));
    }

    @SecurityRequirement(name = "keycloak")
    @PostMapping
    public ResponseEntity<ApiResponse<NotConsInputEntity>> addNotConsInput(@AuthenticationPrincipal Jwt jwt, @RequestBody NotConsInputDto body) {
        return this.notConsInputService.addNotConsInput(jwt.getTokenValue(), body);
    }

    @SecurityRequirement(name = "keycloak")
    @PostMapping("cancel")
    public ResponseEntity<ApiResponse<NotConsInputEntity>> cancelNotConsumableInput(@AuthenticationPrincipal Jwt jwt, @RequestParam(name = "id") int idNotConsInput) {
        return this.notConsInputService.cancelNotConsInput(jwt.getTokenValue(), idNotConsInput);
    }

}
