package com.idApps.storeCashFlowApi.persistence.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.DynamicInsert;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;

@DynamicInsert
@Table(name = "financial_period")
@Entity
@Data
public class FinancialPeriodEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 100)
    private String label;

    @Column(name = "start_date", nullable = false)
    private Date startDate;

    @Column(name = "start_time", nullable = false)
    private Time startTime;

    @Column(name = "end_date")
    private Date endDate;

    @Column(name = "end_time")
    private Time endTime;

    @Column
    private Integer duration;

    @Column(length = 200)
    private String details;

    @Column(name = "state_id")
    private Integer stateId;

    @Column(name = "transaction_id")
    private Integer transactionId;

    // --- Colonnes ajoutées ---
    @Column(name = "current_capital")
    private BigDecimal currentCapital;

    @Column(name = "current_profit_gross")
    private BigDecimal currentProfitGross;

    @Column(name = "current_profit_net")
    private BigDecimal currentProfitNet;

    @Column(name = "total_expenses")
    private BigDecimal totalExpenses;

    @Column(name = "total_customer_credit")
    private BigDecimal totalCustomerCredit;

    @Column(name = "total_external_loan")
    private BigDecimal totalExternalLoan;

    @Column(name = "total_advance")
    private BigDecimal totalAdvance;

    @Column(name = "total_consumable_inputs")
    private BigDecimal totalConsumableInputs;

    @Column(name = "total_non_consumable_inputs")
    private BigDecimal totalNonConsumableInputs;

    @Column(name = "cash_register_balance")
    private BigDecimal cashRegisterBalance;

    @Column(name = "total_out_of_pocket_expenses")
    private BigDecimal totalOutOfPocketExpenses;
}
