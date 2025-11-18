package com.idApps.storeCashFlowApi.persistence.repository;

import com.idApps.storeCashFlowApi.persistence.entity.TransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<TransactionEntity, Integer> {

    @Query(value = """
                SELECT
                    transaction.id, transaction.label, transaction.amount, transaction.adding_date,
                    transaction.adding_time, transaction.transaction_date, transaction.transaction_time,
                    transaction.details, transaction.image_src, transaction.id_transaction_type, transaction.id_period,
                    transaction.executed_by, transaction.current_capital, transaction.current_profit_gross,
                    transaction.current_profit_net, transaction.total_expenses, transaction.total_customer_credit,
                    transaction.total_external_loan, transaction.total_advance, transaction.total_consumable_inputs,
                    transaction.total_non_consumable_inputs, transaction.cash_register_balance, transaction.total_out_of_pocket_expenses
                FROM transaction
                WHERE
                    (:idTransactionType IS NULL OR transaction.id_transaction_type = :idTransactionType)
                AND (:idPeriod IS NULL OR transaction.id_period = :idPeriod)
                AND (:executedBy IS NULL OR transaction.executed_by = :executedBy)
                AND (:transactionDateMin IS NULL OR transaction.transaction_date >= :transactionDateMin)
                AND (:transactionDateMax IS NULL OR transaction.transaction_date <= :transactionDateMax);
            """, nativeQuery = true)
    List<Map<String, Object>> getTransactions(@Param("idTransactionType") Integer idTransactionType,
                                               @Param("idPeriod") Integer idPeriod, @Param("executedBy") Integer executedBy,
                                                @Param("transactionDateMin") LocalDate transactionDateMin, @Param("transactionDateMax") LocalDate transactionDateMax);

    @Query(value = "SELECT * FROM transaction ORDER BY id DESC LIMIT 1;", nativeQuery = true)
    Optional<TransactionEntity> getLastTransaction();
}
