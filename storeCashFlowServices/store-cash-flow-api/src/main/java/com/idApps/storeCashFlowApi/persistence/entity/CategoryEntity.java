package com.idApps.storeCashFlowApi.persistence.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.sql.Date;
import java.sql.Time;

@Data
@Entity
@Table(name = "category")
public class CategoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String label;

    private String details;

    @Column(name = "image_src")
    private String imageSrc;

    @Column(name = "adding_date")
    private Date addingDate;

    @Column(name = "adding_time")
    private Time addingTime;

    @Column(name = "created_by")
    private Integer createdBy;

    @Column(name = "state_id")
    private Integer stateId;
}
