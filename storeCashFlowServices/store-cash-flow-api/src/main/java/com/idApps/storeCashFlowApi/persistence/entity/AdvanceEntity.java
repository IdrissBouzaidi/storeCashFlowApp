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
@Table(name = "advance")
public class AdvanceEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String label;

    private BigDecimal amount;

    @Column(name = "id_transaction")
    private Integer idTransaction;

    @Column(name = "state_id")
    private Integer stateId;

    @Column(name = "advance_date")
    private Date advanceDate;

    @Column(name = "advance_time")
    private Time advanceTime;

    @Column(name = "taker_id")
    private Integer takerId;
}
