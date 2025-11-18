package com.idApps.storeCashFlowApi.persistence.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "charge_transaction_state")
@Data
public class ChargeTransactionStateEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String code;

    private String label;
}
