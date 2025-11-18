package com.idApps.storeCashFlowApi.persistence.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.DynamicInsert;

import java.math.BigDecimal;

@Data
@DynamicInsert
@Table(name = "charge")
@Entity
public class ChargeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String label;

    private Integer quantity;

    private BigDecimal cost;

    private BigDecimal total;

    private Boolean deleted;

    @Column(name = "id_state")
    private Integer idState;

    @Column(name = "id_transaction")
    private Integer idTransaction;

    @Column(name = "id_charge_type")
    private Integer idChargeType;
    @Column(name = "consumed_by")
    private Integer consumedBy;
}
