package com.idApps.storeCashFlowApi.models.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.Column;
import lombok.Data;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FinancialPeriodDto {

    private Integer id;

    private String label;

    @JsonAlias({"startDate", "start_date"})
    private Date startDate;

    @JsonAlias({"startTime", "start_time"})
    private Time startTime;

    @JsonAlias({"endDate", "end_date"})
    private Date endDate;

    @JsonAlias({"endTime", "end_time"})
    private Time endTime;

    private Integer duration;

    private String details;

    @JsonAlias({"stateId", "state_id"})
    private Integer stateId;

    @JsonAlias({"transaction_id", "transactionId"})
    private Integer transactionId;

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
