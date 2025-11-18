package com.idApps.storeCashFlowApi.persistence.repository;

import com.idApps.storeCashFlowApi.persistence.entity.AdvanceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Repository
public interface AdvanceRepository extends JpaRepository<AdvanceEntity, Integer> {

    @Query(value = """
            SELECT advance.id, advance.label, advance.amount, advance.state_id, advance.advance_date, advance.advance_time, advance.taker_id,
                    transaction.adding_date, transaction.adding_time, transaction.details, transaction.id_period, transaction.executed_by
            FROM advance
            LEFT JOIN transaction ON advance.id_transaction = transaction.id
            WHERE (:advanceDateMin IS NULL OR :advanceDateMin <= advance.advance_date) AND
                (:advanceDateMax IS NULL OR :advanceDateMax >= advance.advance_date) AND
                (:stateId IS NULL OR :stateId = advance.state_id) AND
                (:periodId IS NULL OR :periodId = transaction.id_period) AND
                (:takerId IS NULL OR :takerId = advance.taker_id);
            """, nativeQuery = true)
    List<Map<String, Object>> getAdvances(@Param("advanceDateMin") Date advanceDateMin, @Param("advanceDateMax") Date advanceDateMax, @Param("stateId") Integer stateId, @Param("periodId") Integer periodId, @Param("takerId") Integer takerId);
}
