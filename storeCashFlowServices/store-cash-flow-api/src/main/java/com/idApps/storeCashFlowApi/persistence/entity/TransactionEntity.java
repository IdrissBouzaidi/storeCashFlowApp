package com.idApps.storeCashFlowApi.persistence.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.DynamicInsert;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;

@Data
@Entity
@DynamicInsert
@Table(name = "transaction")
public class TransactionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String label;

    private BigDecimal amount;

    @Column(name = "adding_date")
    private Date addingDate;

    @Column(name = "adding_time")
    private Time addingTime;

    @Column(name = "transaction_date")
    private Date transactionDate;

    @Column(name = "transaction_time")
    private Time transactionTime;

    private String details;

    @Column(name = "image_src")
    private String imageSrc;

    @Column(name = "id_transaction_type")
    private Integer idTransactionType;

    @Column(name = "id_period")
    private Integer idPeriod;

    @Column(name = "original_transaction_id")
    private Integer originalTransactionId;

    @Column(name = "executed_by")
    private Integer executedBy;

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