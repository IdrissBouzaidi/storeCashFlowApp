package com.idApps.storeCashFlowApi.models.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.Column;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ConsInputDto {
    private Integer id;
    private String label;


    @JsonAlias({"receiptSrc", "receipt_src"})
    private String receiptSrc;

    @JsonAlias({"idProduct", "id_product"})
    private Integer idProduct;
    @JsonAlias({"productLabel", "product_label"})
    private String productLabel;

    @JsonAlias({"idInput", "id_input"})
    private Integer idInput;
    private BigDecimal cost;

    @JsonAlias({"initialQuantity", "initial_quantity"})
    private Integer initialQuantity;

    @JsonAlias({"remainingQuantity", "remaining_quantity"})
    private Integer remainingQuantity;
    private BigDecimal total;

    @JsonAlias({"idTransaction", "id_transaction"})
    private Integer idTransaction;
    @JsonAlias({"addingDate", "adding_date"})
    private LocalDate addingDate;
    @JsonAlias({"addingTime", "adding_time"})
    private LocalTime addingTime;
    @JsonAlias({"transactionDate", "transaction_date"})
    private LocalDate transactionDate;
    @JsonAlias({"transactionTime", "transaction_time"})
    private LocalTime transactionTime;

    @JsonAlias({"imageSrc", "image_src"})
    private String imageSrc;
    @JsonAlias({"idTransactionType", "id_transaction_type"})
    private Integer idTransactionType;
    @JsonAlias({"idState", "id_state"})
    private Integer idState;
    @JsonAlias({"idPeriod", "id_period"})
    private Integer idPeriod;
    @JsonAlias({"executedBy", "executed_by"})
    private Integer executedBy;
    private String details;
}
