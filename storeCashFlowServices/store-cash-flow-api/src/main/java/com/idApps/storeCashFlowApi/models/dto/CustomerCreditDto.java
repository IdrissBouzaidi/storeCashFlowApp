package com.idApps.storeCashFlowApi.models.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CustomerCreditDto {

    //Customer table
    private Integer id;
    private String label;
    @JsonAlias({"initial_amount", "initialAmount"})
    private BigDecimal initialAmount;
    @JsonAlias({"paid_amount", "paidAmount"})
    private BigDecimal paidAmount;
    @JsonAlias({"remaining_amount", "remainingAmount"})
    private BigDecimal remainingAmount;
    @JsonAlias({"id_transaction", "idTransaction"})
    private Integer idTransaction;
    @JsonAlias({"state_id", "stateId"})
    private Integer stateId;
    @JsonAlias({"credit_date", "creditDate"})
    private Date creditDate;
    @JsonAlias({"credit_time", "creditTime"})
    private Time creditTime;
    @JsonAlias({"customer_id", "customerId"})
    private Integer customerId;

    //Transaction table
    @JsonAlias({"adding_date", "addingDate"})
    private Date addingDate;
    @JsonAlias({"adding_time", "addingTime"})
    private Time addingTime;
    private String details;
    @JsonAlias({"id_period", "idPeriod"})
    private Integer idPeriod;
    @JsonAlias({"executed_by", "executedBy"})
    private Integer executedBy;
}
