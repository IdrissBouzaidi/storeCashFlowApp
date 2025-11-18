package com.idApps.storeCashFlowApi.models.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.Column;
import lombok.Data;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class NotConsInputDto {

    // not_consumable_input table
    private Integer id;
    private String label;
    @JsonAlias({"id_input", "idInput"})
    private Integer idInput;
    @JsonAlias({"state_id", "stateId"})
    private Integer stateId;
    @JsonAlias({"reusable_input_id", "reusableInputId"})
    private Integer reusableInputId;
    private Integer contributor;

    //input table
    private BigDecimal cost;

    @JsonAlias({"initialQuantity", "initial_quantity"})
    private Integer initialQuantity;

    @JsonAlias({"remainingQuantity", "remaining_quantity"})
    private Integer remainingQuantity;

    private BigDecimal total;
    @JsonAlias({"id_transaction", "idTransaction"})
    private Integer idTransaction;

    //transaction table
    @JsonAlias({"adding_date", "addingDate"})
    private Date addingDate;
    @JsonAlias({"adding_time", "addingTime"})
    private Time addingTime;
    @JsonAlias({"transaction_date", "transactionDate"})
    private Date transactionDate;
    @JsonAlias({"transaction_time", "transactionTime"})
    private Time transactionTime;
    private String details;
    @JsonAlias({"id_period", "idPeriod"})
    private Integer idPeriod;
    @JsonAlias({"executed_by", "executedBy"})
    private Integer executedBy;
    @JsonAlias({"image_src", "imageSrc"})
    private String imageSrc;
}
