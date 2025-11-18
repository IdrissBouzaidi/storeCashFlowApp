package com.idApps.storeCashFlowApi.persistence.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import org.hibernate.annotations.DynamicInsert;

import java.math.BigDecimal;
import java.util.Map;

@Data
@Entity
@DynamicInsert
@Table(name = "input")
public class InputEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String label;

    private BigDecimal cost;

    @Column(name = "initial_quantity")
    private Integer initialQuantity;

    @Column(name = "remaining_quantity")
    private Integer remainingQuantity;

    private BigDecimal total;

    @Column(name = "id_transaction")
    private Integer idTransaction;
}
