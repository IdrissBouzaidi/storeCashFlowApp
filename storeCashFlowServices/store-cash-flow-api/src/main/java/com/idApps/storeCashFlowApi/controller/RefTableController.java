package com.idApps.storeCashFlowApi.controller;

import com.idApps.storeCashFlowApi.models.dto.RefTableDto;
import com.idApps.storeCashFlowApi.persistence.service.RefTableService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/refTables")
public class RefTableController {
    @Autowired
    private RefTableService refTableService;

    @SecurityRequirement(name = "keycloak")
    @GetMapping("transactionTypes")
    public ResponseEntity<List<RefTableDto>> getTransactionTypesRefTable() {
        return ResponseEntity.ok(this.refTableService.getTransactionTypesRefTable());
    }

    @SecurityRequirement(name = "keycloak")
    @GetMapping("financialPeriods")
    public ResponseEntity<List<RefTableDto>> getFinancialPeriodsRefTable() {
        return ResponseEntity.ok(this.refTableService.getFinancialPeriodsRefTable());
    }

    @SecurityRequirement(name = "keycloak")
    @GetMapping("periodStates")
    public ResponseEntity<List<RefTableDto>> getPeriodStatesRefTable() {
        return ResponseEntity.ok(this.refTableService.getPeriodStatesRefTable());
    }

    @SecurityRequirement(name = "keycloak")
    @GetMapping("users")
    public ResponseEntity<List<RefTableDto>> getUsersRefTable() {
        return ResponseEntity.ok(this.refTableService.getUsersRefTable());
    }

    @SecurityRequirement(name = "keycloak")
    @GetMapping("productsTransactionStates")
    public ResponseEntity<List<RefTableDto>> getProductsTransactionStatesRefTable() {
        return ResponseEntity.ok(this.refTableService.getProductsTransactionStatesRefTable());
    }
    @SecurityRequirement(name = "keycloak")
    @GetMapping("chargesTransactionStates")
    public ResponseEntity<List<RefTableDto>> getChargesTransactionStatesRefTable() {
        return ResponseEntity.ok(this.refTableService.getChargesTransactionStatesRefTable());
    }

    @SecurityRequirement(name = "keycloak")
    @GetMapping("capitalContributionTransactionStates")
    public ResponseEntity<List<RefTableDto>> getCapitalContributionTransactionStatesRefTable() {
        return ResponseEntity.ok(this.refTableService.getCapitalContributionTransactionStatesRefTable());
    }

    @SecurityRequirement(name = "keycloak")
    @GetMapping("products")
    public ResponseEntity<List<RefTableDto>> getProductsRefTable() {
        return ResponseEntity.ok(this.refTableService.getProductsRefTable());
    }

    @SecurityRequirement(name = "keycloak")
    @GetMapping("productStates")
    public ResponseEntity<List<RefTableDto>> getProductStatesRefTable() {
        return ResponseEntity.ok(this.refTableService.getProductStatesRefTable());
    }

    @SecurityRequirement(name = "keycloak")
    @GetMapping("categories")
    public ResponseEntity<List<RefTableDto>> getCategoriesRefTable() {
        return ResponseEntity.ok(this.refTableService.getCategoriesRefTable());
    }

    @SecurityRequirement(name = "keycloak")
    @GetMapping("categoryStates")
    public ResponseEntity<List<RefTableDto>> getCategoryStatesRefTable() {
        return ResponseEntity.ok(this.refTableService.getCategoryStatesRefTable());
    }

    @SecurityRequirement(name = "keycloak")
    @GetMapping("chargeTypes")
    public ResponseEntity<List<RefTableDto>> getChargeTypesRefTable() {
        return ResponseEntity.ok(this.refTableService.getChargeTypesRefTable());
    }

    @SecurityRequirement(name = "keycloak")
    @GetMapping("reusableInputs")
    public ResponseEntity<List<RefTableDto>> getReusableInputsRefTable() {
        return ResponseEntity.ok(this.refTableService.getReusableInputsRefTable());
    }

    @SecurityRequirement(name = "keycloak")
    @GetMapping("notConsInputStates")
    public ResponseEntity<List<RefTableDto>> getNotConsInputStatesRefTable() {
        return ResponseEntity.ok(this.refTableService.getNotConsInputStatesRefTable());
    }

    @SecurityRequirement(name = "keycloak")
    @GetMapping("advanceStates")
    public ResponseEntity<List<RefTableDto>> getAdvanceStatesRefTable() {
        return ResponseEntity.ok(this.refTableService.getAdvanceStatesRefTable());
    }

    @SecurityRequirement(name = "keycloak")
    @GetMapping("outOfPocketStates")
    public ResponseEntity<List<RefTableDto>> getOutOfPocketStatesRefTable() {
        return ResponseEntity.ok(this.refTableService.getOutOfPocketStatesRefTable());
    }
    @SecurityRequirement(name = "keycloak")
    @GetMapping("customers")
    public ResponseEntity<List<RefTableDto>> getCustomersRefTable() {
        return ResponseEntity.ok(this.refTableService.getCustomersRefTable());
    }

    @SecurityRequirement(name = "keycloak")
    @GetMapping("customerCreditStates")
    public ResponseEntity<List<RefTableDto>> getCustomerCreditStatesRefTable() {
        return ResponseEntity.ok(this.refTableService.getCustomerCreditStatesRefTable());
    }

    @SecurityRequirement(name = "keycloak")
    @GetMapping("externalLoanStates")
    public ResponseEntity<List<RefTableDto>> getExternalLoanStatesRefTable() {
        return ResponseEntity.ok(this.refTableService.getExternalLoanStatesRefTable());
    }
}