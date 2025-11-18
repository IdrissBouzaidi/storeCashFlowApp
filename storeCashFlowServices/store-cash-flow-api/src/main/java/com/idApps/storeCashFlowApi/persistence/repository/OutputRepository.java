package com.idApps.storeCashFlowApi.persistence.repository;

import com.idApps.storeCashFlowApi.persistence.entity.OutputEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public interface OutputRepository extends JpaRepository<OutputEntity, Integer> {

    @Query(value = """
            SELECT output.id, output.label, output.id_product, product.label AS productLabel, output.quantity,
            	output.unit_cost, output.total_cost, output.unit_price, output.total_price, output.unit_profit, output.total_profit,
                output.id_state, output.id_period, output.sold_by,
                transaction.adding_date, transaction.adding_time, transaction.transaction_date, transaction.transaction_time,
                transaction.executed_by, transaction.image_src, transaction.id_transaction_type
            FROM output
            LEFT JOIN transaction ON output.id_transaction=transaction.id
            LEFT JOIN product on output.id_product=product.id
            WHERE
            	(:productId IS NULL OR output.id_product=:productId)
            AND (:transactionDateMin IS NULL OR transaction.transaction_date>=:transactionDateMin)
            AND (:transactionDateMax IS NULL OR transaction.transaction_date<=:transactionDateMax)
            AND (:transactionTypeId IS NULL OR transaction.id_transaction_type=:transactionTypeId)
            AND (:transactionStateId IS NULL OR output.id_state=:transactionStateId)
            AND (:periodId IS NULL OR output.id_period=:periodId)
            AND (:soldById Is NULL OR output.sold_by=:soldById);
            """, nativeQuery = true)
    List<Map<String, Object>> getOutputs(@Param("productId") Integer productId, @Param("transactionTypeId") Integer transactionTypeId,
                                         @Param("transactionStateId") Integer transactionStateId, @Param("periodId") Integer periodId,
                                         @Param("soldById") Integer soldById, @Param("transactionDateMin") LocalDate transactionDateMin,
                                         @Param("transactionDateMax") LocalDate transactionDateMax);

}
