package com.idApps.storeCashFlowApi.controller;

import com.idApps.storeCashFlowApi.models.dto.CustomerCreditDto;
import com.idApps.storeCashFlowApi.models.response.ApiResponse;
import com.idApps.storeCashFlowApi.persistence.entity.ConsInputEntity;
import com.idApps.storeCashFlowApi.persistence.entity.CustomerCreditEntity;
import com.idApps.storeCashFlowApi.persistence.service.CustomerCreditService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.sql.Date;
import java.util.List;

@RestController
@RequestMapping("api/v1/customerCredits")
public class CustomerCreditController {
    @Autowired
    private CustomerCreditService customerCreditService;

    @SecurityRequirement(name = "keycloak")
    @GetMapping
    public ResponseEntity<List<CustomerCreditDto>> getCustomerCredits(@RequestParam(required = false) Date creditDateMin, @RequestParam(required = false) Date creditDateMax, @RequestParam(required = false) Integer stateId, @RequestParam(required = false) Integer periodId, @RequestParam(required = false) Integer customerId) {
        return ResponseEntity.ok(this.customerCreditService.getCustomerCredits(creditDateMin, creditDateMax, stateId, periodId, customerId));
    }

    @SecurityRequirement(name = "keycloak")
    @PostMapping
    public ResponseEntity<ApiResponse<CustomerCreditEntity>> addCustomerCredit(@RequestBody CustomerCreditDto body, @AuthenticationPrincipal Jwt jwt) {
        return this.customerCreditService.addCustomerCredit(body, jwt.getTokenValue());
    }

    @SecurityRequirement(name = "keycloak")
    @PostMapping("cancel")
    public ResponseEntity<ApiResponse<CustomerCreditEntity>> cancelCustomerCredit(@AuthenticationPrincipal Jwt jwt, @RequestParam(name = "id") int idCustomerCredit) {
        return this.customerCreditService.cancelCustomerCredit(jwt.getTokenValue(), idCustomerCredit);
    }
}
