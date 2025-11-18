package com.idApps.storeCashFlowApi.models.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OutputDto {
    private Integer id;
    private String label;
    private Boolean deleted;
    @JsonAlias({"id_product", "idProduct"})
    private Integer idProduct;
    @JsonAlias({"product_label", "productLabel"})
    private String productLabel;

    @JsonAlias({"id_cons_input", "idConsInput"})
    private Integer idConsInput;
    private Integer quantity;

    @JsonAlias({"unit_cost", "unitCost"})
    private BigDecimal unitCost;
    @JsonAlias({"total_cost", "totalCost"})
    private BigDecimal totalCost;
    @JsonAlias({"unit_price", "unitPrice"})
    private BigDecimal unitPrice;
    @JsonAlias({"total_price", "totalPrice"})
    private BigDecimal totalPrice;
    @JsonAlias({"unit_profit", "unitProfit"})
    private BigDecimal unitProfit;
    @JsonAlias({"total_profit", "totalProfit"})
    private BigDecimal totalProfit;

    @JsonAlias({"id_transaction", "idTransaction"})
    private Integer idTransaction;
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
    @JsonAlias({"id_state", "idState"})
    private Integer idState;
    @JsonAlias({"id_period", "idPeriod"})
    private Integer idPeriod;
    @JsonAlias({"executed_by", "executedBy"})
    private Integer executedBy;
    @JsonAlias({"sold_by", "soldBy"})
    private Integer soldBy;
    private String details;
}
