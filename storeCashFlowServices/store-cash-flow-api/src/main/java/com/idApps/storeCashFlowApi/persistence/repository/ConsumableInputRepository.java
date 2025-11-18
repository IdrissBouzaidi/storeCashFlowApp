package com.idApps.storeCashFlowApi.persistence.repository;

import com.idApps.storeCashFlowApi.persistence.entity.ConsInputEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Repository
public interface ConsumableInputRepository extends JpaRepository<ConsInputEntity, Integer> {


    @Query(value = """
            SELECT consumable_input.id AS id, consumable_input.label, consumable_input.id_product, consumable_input.id_state, consumable_input.receipt_src,
                    input.cost, input.initial_quantity, input.remaining_quantity, input.total,
                    transaction.adding_date, transaction.adding_time, transaction.transaction_date, transaction.transaction_time,
                    transaction.image_src, transaction.id_transaction_type, transaction.id_period, transaction.executed_by, product.label AS productLabel
                    FROM consumable_input
            LEFT JOIN input on consumable_input.id_input=input.id
            LEFT JOIN transaction on input.id_transaction=transaction.id
            LEFT JOIN product on consumable_input.id_product=product.id
            WHERE   (:productId IS NULL OR id_product = :productId)
                AND (:transactionTypeId IS NULL OR id_transaction_type = :transactionTypeId)
                AND (:transactionStateId IS NULL OR id_state = :transactionStateId)
                AND (:periodId IS NULL OR id_period = :periodId)
                AND (:executedById IS NULL OR executed_by = :executedById)
                AND (:transactionDateMin IS NULL OR transaction_date >= :transactionDateMin)
                AND (:transactionDateMax IS NULL OR transaction_date <= :transactionDateMax);
            """, nativeQuery = true)
    List<Map<String, Object>> getConsInputs(@Param("productId") Integer productId, @Param("transactionTypeId") Integer transactionTypeId, @Param("transactionStateId") Integer transactionStateId, @Param("periodId") Integer periodId, @Param("executedById") Integer executedById, @Param("transactionDateMin") LocalDate transactionDateMin, @Param("transactionDateMax") LocalDate transactionDateMax);

    /**
     * Cette méthode est pour la recherche sur les consumable inputs, càd on va utiliser like %...%
     */
    @Query(value = """
                SELECT consumable_input.id, consumable_input.label,
                        consumable_input.id_product, consumable_input.receipt_src,
                        input.cost, input.remaining_quantity FROM consumable_input
                LEFT JOIN input ON consumable_input.id_input=input.id
                LEFT JOIN product ON consumable_input.id_product=product.id
                WHERE remaining_quantity>0
                        AND (:label IS NULL OR LOWER(product.label) LIKE LOWER(CONCAT('%', :label, '%')));
            """, nativeQuery = true)
    List<Map<String, Object>> searchConsInputs(@Param("label") String consInputLabel);
}
