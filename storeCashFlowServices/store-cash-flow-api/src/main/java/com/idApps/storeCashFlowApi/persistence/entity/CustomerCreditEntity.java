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
@Table(name = "customer_credit")
public class CustomerCreditEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String label;

    @Column(name = "initial_amount")
    private BigDecimal initialAmount;

    @Column(name = "paid_amount")
    private BigDecimal paidAmount;

    @Column(name = "remaining_amount")
    private BigDecimal remainingAmount;

    @Column(name = "id_transaction")
    private Integer idTransaction;

    @Column(name = "state_id")
    private Integer stateId;

    @Column(name = "credit_date")
    private Date creditDate;

    @Column(name = "credit_time")
    private Time creditTime;

    @Column(name = "customer_id")
    private Integer customerId;
}
