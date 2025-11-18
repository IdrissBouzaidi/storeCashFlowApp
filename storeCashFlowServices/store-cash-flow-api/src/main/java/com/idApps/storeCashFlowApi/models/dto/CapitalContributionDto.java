package com.idApps.storeCashFlowApi.models.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CapitalContributionDto {

    //capital_contribution table
    private Integer id;
    private String label;
    private BigDecimal amount;
    @JsonAlias({"contribution_date", "contributionDate"})
    private LocalDate contributionDate;
    @JsonAlias({"contribution_time", "contributionTime"})
    private LocalTime contributionTime;
    @JsonAlias({"id_transaction", "idTransaction"})
    private Integer idTransaction;
    @JsonAlias({"id_state", "idState"})
    private Integer idState;
    @JsonAlias({"contributor_id", "contributorId"})
    private Integer contributorId;

    //Transaction table
    @JsonAlias({"adding_date", "addingDate"})
    private LocalDate addingDate;
    @JsonAlias({"adding_time", "addingTime"})
    private LocalTime addingTime;
    private String details;
    @JsonAlias({"id_transaction_type", "idTransactionType"})
    private Integer idTransactionType;
    @JsonAlias({"id_period", "idPeriod"})
    private Integer idPeriod;
    @JsonAlias({"executed_by", "executedBy"})
    private Integer executedBy;
}
