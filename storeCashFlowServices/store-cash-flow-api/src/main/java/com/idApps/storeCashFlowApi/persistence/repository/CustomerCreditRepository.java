package com.idApps.storeCashFlowApi.persistence.repository;

import com.idApps.storeCashFlowApi.persistence.entity.CustomerCreditEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.util.List;
import java.util.Map;

@Repository
public interface CustomerCreditRepository extends JpaRepository<CustomerCreditEntity, Integer> {

    @Query(value = """
            SELECT customer_credit.id, customer_credit.label, customer_credit.initial_amount, customer_credit.paid_amount, customer_credit.remaining_amount,
                    customer_credit.state_id, customer_credit.credit_date, customer_credit.credit_time, customer_credit.customer_id,
                    transaction.adding_date, transaction.adding_time, transaction.details, transaction.id_period, transaction.executed_by
                    FROM customer_credit
            LEFT JOIN transaction ON customer_credit.id_transaction = transaction.id
            WHERE (:creditDateMin IS NULL OR :creditDateMin<=customer_credit.credit_date) AND
                    (:creditDateMax IS NULL OR :creditDateMax>=customer_credit.credit_date) AND
                    (:stateId IS NULL OR :stateId=customer_credit.state_id) AND
                    (:periodId IS NULL OR :periodId=transaction.id_period) AND
                    (:customerId IS NULL OR :customerId=customer_credit.customer_id);
            """, nativeQuery = true)
    List<Map<String, Object>> getCustomerCredits(@Param("creditDateMin") Date creditDateMin, @Param("creditDateMax") Date creditDateMax, @Param("stateId") Integer stateId, @Param("periodId") Integer periodId, @Param("customerId") Integer customerId);

}
