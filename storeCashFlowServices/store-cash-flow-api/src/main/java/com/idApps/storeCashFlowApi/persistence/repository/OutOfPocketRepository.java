package com.idApps.storeCashFlowApi.persistence.repository;

import com.idApps.storeCashFlowApi.persistence.entity.OutOfPocketEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.util.List;
import java.util.Map;

@Repository
public interface OutOfPocketRepository extends JpaRepository<OutOfPocketEntity, Integer> {

    @Query(value = """
            SELECT out_of_pocket.id, out_of_pocket.label, out_of_pocket.amount, out_of_pocket.id_transaction,
                out_of_pocket.state_id, out_of_pocket.borrowing_date, out_of_pocket.borrowing_time, out_of_pocket.borrower_id,
                transaction.adding_date, transaction.adding_time, transaction.details, transaction.id_period, transaction.executed_by
            FROM out_of_pocket
            JOIN transaction ON out_of_pocket.id_transaction = transaction.id
            WHERE (:borrowingDateMin IS NULL OR :borrowingDateMin <= borrowing_date) AND
                    (:borrowingDateMax IS NULL OR :borrowingDateMax >= borrowing_date) AND
                    (:stateId IS NULL OR :stateId = state_id) AND
                    (:idPeriod IS NULL OR :idPeriod = id_period) AND
                    (:borrowerId IS NULL OR :borrowerId = borrower_id);
            """, nativeQuery = true)
    List<Map<String, Object>> getOutOfPockets(@Param("borrowingDateMin") Date borrowingDateMin, @Param("borrowingDateMax") Date borrowingDateMax, @Param("stateId") Integer stateId, @Param("idPeriod") Integer idPeriod, @Param("borrowerId") Integer borrowerId);
}
