package com.idApps.storeCashFlowApi.persistence.repository;

import com.idApps.storeCashFlowApi.models.dto.FinancialPeriodDto;
import com.idApps.storeCashFlowApi.persistence.entity.FinancialPeriodEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public interface FinancialPeriodRepository extends JpaRepository<FinancialPeriodEntity, Integer> {

    @Query(value = """
            SELECT id, label FROM financial_period
            WHERE state_id!=4;
        """, nativeQuery = true)
    List<Map<String, Object>> getFinancialPeriodsRefTable();

    @Query(value = "SELECT id FROM financial_period WHERE state_id=2;", nativeQuery = true)
    Optional<Integer> getActivePeriodId();

    @Query(value = """
            SELECT * FROM financial_period
            WHERE (:startDateMin IS NULL OR start_date>=:startDateMin)
                AND (:startDateMax IS NULL OR start_date<=:startDateMax)
                AND (:endDateMin IS NULL OR end_date>=:endDateMin)
                AND (:endDateMax IS NULL OR end_date<=:endDateMax)
                AND ((:stateId IS NULL AND state_id!=4) OR state_id = :stateId);
            """, nativeQuery = true)
    List<Map<String, Object>> getFinancialPeriods(Date startDateMin, Date startDateMax, Date endDateMin, Date endDateMax, Integer stateId);

    @Query(value = """
                SELECT count(*)>0 FROM financial_period WHERE state_id = 2;
            """, nativeQuery = true)
    Long isSomePeriodInProgress();

    @Query(value = "SELECT * FROM financial_period WHERE state_id = 2;", nativeQuery = true)
    Optional<FinancialPeriodEntity> getActivePeriod();

    @Query(value = """
        SELECT * FROM financial_period
        WHERE state_id != 4
        ORDER BY id DESC LIMIT 1;
        """, nativeQuery = true)
    Optional<FinancialPeriodEntity> getLastPeriod();

    @Query(value = """
            SELECT EXISTS(
            	SELECT 1
            	FROM transaction t1
            	LEFT JOIN transaction_type tt1 ON t1.id_transaction_type = tt1.id
            	LEFT JOIN transaction_action_type tat1 ON tt1.action_type_id = tat1.id
            	LEFT JOIN transaction t2
            		ON t1.id = t2.original_transaction_id
            		AND t2.id_period = :id
            		AND t2.amount != 0
            		AND EXISTS (
            			SELECT 1 FROM transaction_type tt2
            			JOIN transaction_action_type tat2 ON tt2.action_type_id = tat2.id
            			WHERE tt2.id = t2.id_transaction_type
            			  AND tat2.id = 3
            		)
            	WHERE t1.amount != 0
            	  AND t1.id_period = :id
            	  AND tat1.id = 1
            	  AND t2.id IS NULL
            ) AS exists_any;
        """, nativeQuery = true)
    int isPeriodNotEmpty(int id);
}
