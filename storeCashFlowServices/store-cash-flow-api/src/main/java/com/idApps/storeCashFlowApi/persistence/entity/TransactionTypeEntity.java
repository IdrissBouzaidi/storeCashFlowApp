package com.idApps.storeCashFlowApi.persistence.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Table(name = "transaction_type")
@Entity
@Data
public class TransactionTypeEntity {
    @Id
    @GeneratedValue
    private Integer id;

    private String code;

    private String label;
}
