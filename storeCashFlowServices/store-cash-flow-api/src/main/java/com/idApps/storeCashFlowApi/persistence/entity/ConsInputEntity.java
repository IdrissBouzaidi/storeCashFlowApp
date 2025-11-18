package com.idApps.storeCashFlowApi.persistence.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import org.hibernate.annotations.DynamicInsert;

import java.util.Map;

@Data
@Entity
@DynamicInsert
@Table(name = "consumable_input")
public class ConsInputEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String label;

    @Column(name = "id_product")
    private Integer idProduct;

    @Column(name = "id_input")
    private Integer idInput;

    @Column(name = "id_state")
    private Integer idState;

    @Column(name = "receipt_src")
    private String receiptSrc;

}
