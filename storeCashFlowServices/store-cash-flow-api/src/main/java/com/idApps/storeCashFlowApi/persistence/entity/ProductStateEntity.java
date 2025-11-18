package com.idApps.storeCashFlowApi.persistence.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "product_state")
@Data
public class ProductStateEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String code;

    private String label;
}
