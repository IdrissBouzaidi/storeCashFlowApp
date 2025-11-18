package com.idApps.storeCashFlowApi.models.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChargeDto {
    //charge table
    private Integer id;
    private String label;
    @JsonAlias({"id_charge_type", "idChargeType"})
    private Integer idChargeType;
    private Integer quantity;
    private BigDecimal cost;
    private BigDecimal total;
    private Boolean deleted;
    @JsonAlias({"id_state", "idState"})
    private Integer idState;
    @JsonAlias({"id_transaction", "idTransaction"})
    private Integer idTransaction;
    @JsonAlias({"consumed_by", "consumedBy"})
    private Integer consumedBy;

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
    @JsonAlias({"image_src", "imageSrc"})
    private String imageSrc;
    @JsonAlias({"id_transaction_type", "idTransactionType"})
    private Integer idTransactionType;
    @JsonAlias({"id_period", "idPeriod"})
    private Integer idPeriod;
    @JsonAlias({"executed_by", "executedBy"})
    private Integer executedBy;
}
