package com.idApps.storeCashFlowApi.models.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TransactionDto {
    private Integer id;
    private String label;
    private BigDecimal amount;
    private String details;

    @JsonAlias({"adding_date", "addingDate"})
    private Date addingDate;

    @JsonAlias({"adding_time", "addingTime"})
    private Time addingTime;

    @JsonAlias({"transaction_date", "transactionDate"})
    private Date transactionDate;

    @JsonAlias({"transaction_time", "transactionTime"})
    private Time transactionTime;

    @JsonAlias({"image_src", "imageSrc"})
    private String imageSrc;

    @JsonAlias({"id_transaction_type", "idTransactionType"})
    private Integer idTransactionType;

    @JsonAlias({"id_period", "idPeriod"})
    private Integer idPeriod;

    @JsonAlias({"original_transaction_id", "originalTransactionId"})
    private Integer originalTransactionId;

    @JsonAlias({"executed_by", "executedBy"})
    private Integer executedBy;

    // --- Nouveaux champs ---
    @JsonAlias({"current_capital", "currentCapital"})
    private BigDecimal currentCapital;

    @JsonAlias({"current_profit_gross", "currentProfitGross"})
    private BigDecimal currentProfitGross;

    @JsonAlias({"current_profit_net", "currentProfitNet"})
    private BigDecimal currentProfitNet;

    @JsonAlias({"total_expenses", "totalExpenses"})
    private BigDecimal totalExpenses;

    @JsonAlias({"total_customer_credit", "totalCustomerCredit"})
    private BigDecimal totalCustomerCredit;

    @JsonAlias({"total_external_loan", "totalExternalLoan"})
    private BigDecimal totalExternalLoan;

    @JsonAlias({"total_advance", "totalAdvance"})
    private BigDecimal totalAdvance;

    @JsonAlias({"total_consumable_inputs", "totalConsumableInputs"})
    private BigDecimal totalConsumableInputs;

    @JsonAlias({"total_non_consumable_inputs", "totalNonConsumableInputs"})
    private BigDecimal totalNonConsumableInputs;

    @JsonAlias({"cash_register_balance", "cashRegisterBalance"})
    private BigDecimal cashRegisterBalance;

    @JsonAlias({"total_out_of_pocket_expenses", "totalOutOfPocketExpenses"})
    private BigDecimal totalOutOfPocketExpenses;
}