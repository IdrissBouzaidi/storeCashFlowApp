package com.idApps.storeCashFlowApi.persistence.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.DynamicInsert;

import java.math.BigDecimal;

@Data
@DynamicInsert
@Entity
@Table(name = "output")
public class OutputEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String label;

    private Integer quantity;

    @Column(name = "unit_cost")
    private BigDecimal unitCost;

    @Column(name = "total_cost")
    private BigDecimal totalCost;

    @Column(name = "unit_price")
    private BigDecimal unitPrice;

    @Column(name = "total_price")
    private BigDecimal totalPrice;

    @Column(name = "unit_profit")
    private BigDecimal unitProfit;

    @Column(name = "total_profit")
    private BigDecimal totalProfit;

    @Column(name = "sold_by")
    private Integer soldBy;

    private Boolean deleted;

    @Column(name = "id_transaction")
    private Integer idTransaction;

    @Column(name = "id_period")
    private Integer idPeriod;

    @Column(name = "id_product")
    private Integer idProduct;

    @Column(name = "id_cons_input")
    private Integer idConsInput;

    @Column(name = "id_state")
    private Integer idState;
}
