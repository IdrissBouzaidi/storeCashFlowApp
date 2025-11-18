package com.idApps.storeCashFlowApi.persistence.entity;


import jakarta.persistence.*;
import org.hibernate.annotations.DynamicInsert;

@Entity
@Table(name = "reusable_not_consumable_input")
@DynamicInsert
public class ReusableNotConsInputEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String label;
    private String details;
}
