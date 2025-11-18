package com.idApps.storeCashFlowApi.persistence.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.DynamicInsert;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@DynamicInsert
@Data
@Table(name = "capital_contribution")
@Entity
public class CapitalContributionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String label;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "contribution_date")
    private LocalDate contributionDate;

    @Column(name = "contribution_time")
    private LocalTime contributionTime;

    @Column(name = "id_transaction")
    private Integer idTransaction;

    @Column(name = "id_state", nullable = false)
    private Integer idState;

    @Column(name = "contributor_id")
    private Integer contributorId;
}
