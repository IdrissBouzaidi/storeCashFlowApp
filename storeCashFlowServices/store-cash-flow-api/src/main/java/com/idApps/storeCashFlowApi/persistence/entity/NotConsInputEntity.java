package com.idApps.storeCashFlowApi.persistence.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.DynamicInsert;

@DynamicInsert
@Data
@Entity
@Table(name = "not_consumable_input")
public class NotConsInputEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String label;

    @Column(name = "state_id")
    private Integer stateId;

    @Column(name = "id_input")
    private Integer idInput;

    @Column(name = "reusable_input_id")
    private Integer reusableInputId;

    private Integer contributor;
}
