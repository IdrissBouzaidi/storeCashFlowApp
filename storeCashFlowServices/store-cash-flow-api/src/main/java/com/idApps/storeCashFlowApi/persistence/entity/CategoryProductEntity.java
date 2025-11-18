package com.idApps.storeCashFlowApi.persistence.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "category_product")
public class CategoryProductEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "category_id", nullable = false)
    private Integer categoryId;

    @Column(name = "product_id", nullable = false)
    private Integer productId;

    public CategoryProductEntity(Integer categoryId, Integer productId) {
        this.categoryId = categoryId;
        this.productId = productId;
    }
}