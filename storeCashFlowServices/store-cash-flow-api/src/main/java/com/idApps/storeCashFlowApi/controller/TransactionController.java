package com.idApps.storeCashFlowApi.controller;

import com.idApps.storeCashFlowApi.models.dto.TransactionDto;
import com.idApps.storeCashFlowApi.persistence.service.TransactionService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/transactions")
public class TransactionController {
    @Autowired
    private TransactionService transactionService;

    @SecurityRequirement(name = "keycloak")
    @GetMapping
    public ResponseEntity<List<TransactionDto>> getTransactions(@RequestParam(required = false) Integer idTransactionType, @RequestParam(required = false) Integer idPeriod,
                                                                @RequestParam(required = false) Integer executedBy, @RequestParam(required = false) LocalDate transactionDateMin,
                                                                @RequestParam(required = false) LocalDate transactionDateMax) {
        return ResponseEntity.ok(this.transactionService.getTransactions(idTransactionType, idPeriod, executedBy, transactionDateMin, transactionDateMax));
    }
}
