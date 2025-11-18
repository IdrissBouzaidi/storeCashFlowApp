package com.idApps.storeCashFlowApi.persistence.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.DynamicInsert;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;

@DynamicInsert
@Data
@Entity
@Table(name = "out_of_pocket")
public class OutOfPocketEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String label;

    private BigDecimal amount;

    @Column(name = "id_transaction")
    private Integer idTransaction;

    @Column(name = "state_id")
    private Integer stateId;

    @Column(name = "borrowing_date")
    private Date borrowingDate;

    @Column(name = "borrowing_time")
    private Time borrowingTime;

    @Column(name = "borrower_id")
    private Integer borrowerId;
}
