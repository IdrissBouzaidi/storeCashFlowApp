package com.idApps.storeCashFlowApi.models.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OutOfPocketDto {

    //out_of_pocket table
    private Integer id;
    private String label;
    private BigDecimal amount;
    @JsonAlias({"id_transaction", "idTransaction"})
    private Integer idTransaction;
    @JsonAlias({"state_id", "stateId"})
    private Integer stateId;
    @JsonAlias({"borrowing_date", "borrowingDate"})
    private Date borrowingDate;
    @JsonAlias({"borrowing_time", "borrowingTime"})
    private Time borrowingTime;
    @JsonAlias({"borrower_id", "borrowerId"})
    private Integer borrowerId;

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
