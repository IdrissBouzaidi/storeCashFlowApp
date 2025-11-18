package com.idApps.storeCashFlowApi.persistence.repository;

import com.idApps.storeCashFlowApi.persistence.entity.NotConsInputEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Repository
public interface NotConsInputRepository extends JpaRepository<NotConsInputEntity, Integer> {

    @Query(value = """
            SELECT not_consumable_input.id, not_consumable_input.label, not_consumable_input.reusable_input_id, not_consumable_input.state_id, not_consumable_input.contributor,
                    input.cost, input.initial_quantity, input.remaining_quantity, input.total,
                    transaction.transaction_date, transaction.transaction_time, transaction.adding_date, transaction.adding_time,
                    transaction.details, transaction.id_transaction_type, transaction.id_period, transaction.executed_by, transaction.image_src
            FROM not_consumable_input
            LEFT JOIN input ON not_consumable_input.id_input=input.id
            LEFT JOIN transaction ON input.id_transaction=transaction.id
            WHERE (:transactionDateMin IS NULL OR transaction.transaction_date>=:transactionDateMin) AND
                    (:transactionDateMax IS NULL OR transaction.transaction_date<=:transactionDateMax) AND
                    (:periodId is NULL OR transaction.id_period=:periodId) AND
                    (:executedById IS NULL OR transaction.executed_by=:executedById) AND
                    (:notConsInputStateId IS NULL OR not_consumable_input.state_id=:notConsInputStateId)
            """, nativeQuery = true)
    List<Map<String, Object>> getNotconsInputs(@Param("transactionDateMin") LocalDate transactionDateMin, @Param("transactionDateMax") LocalDate transactionDateMax, @Param("periodId") Integer periodId, @Param("executedById") Integer executedById, @Param("notConsInputStateId") Integer notConsInputStateId);
}
