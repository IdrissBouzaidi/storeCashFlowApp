package com.idApps.storeCashFlowApi.persistence.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "charge_type")
@Data
public class ChargeTypeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String code;

    private String label;

    @Column(name = "image_src")
    private String imageSrc;
}
