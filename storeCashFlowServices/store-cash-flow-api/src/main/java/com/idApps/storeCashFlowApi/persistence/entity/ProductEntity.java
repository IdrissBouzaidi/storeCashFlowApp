package com.idApps.storeCashFlowApi.persistence.entity;

import jakarta.persistence.*;
import lombok.Data;
import jakarta.persistence.*;
import lombok.Data;
import java.sql.Date;
import java.sql.Time;

@Data
@Entity
@Table(name = "product")
public class ProductEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String label;

    @Column(name = "image_src")
    private String imageSrc;

    @Column(name = "state_id")
    private Integer stateId;

    @Column(name = "adding_date")
    private Date addingDate;

    @Column(name = "adding_time")
    private Time addingTime;

    private String details;

    @Column(name = "created_by")
    private Integer createdBy;

    @Column(name = "transaction_id")
    private Integer transactionId;
}
