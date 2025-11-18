package com.idApps.storeCashFlowApi.persistence.repository;

import com.idApps.storeCashFlowApi.persistence.entity.ChargeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.util.List;
import java.util.Map;

@Repository
public interface ChargeRepository extends JpaRepository<ChargeEntity, Integer> {

    @Query(value = """
            SELECT charge.id, charge.label, charge.id_charge_type, charge.quantity, charge.cost,
                charge.total, charge.deleted, charge.id_state, charge.id_transaction, charge.consumed_by,
                transaction.adding_date, transaction.adding_time, transaction.transaction_date, transaction.transaction_time,
                transaction.details, transaction.image_src, transaction.id_transaction_type, transaction.id_period, transaction.executed_by
            FROM charge
            LEFT JOIN transaction ON charge.id_transaction=transaction.id
            WHERE (:chargeTypeId IS NULL OR charge.id_charge_type=:chargeTypeId) AND
                (:minTransactionDate IS NULL OR transaction.transaction_date>=:minTransactionDate) AND
                (:maxTransactionDate IS NULL OR transaction.transaction_date<=:maxTransactionDate) AND
                (:stateId IS NULL OR charge.id_state=:stateId) AND
                (:periodId IS NULL OR transaction.id_period=:periodId) AND
                (:consumedBy IS NULL OR charge.consumed_by=:consumedBy);
            """, nativeQuery = true)
    List<Map<String, Object>> getCharges(Integer chargeTypeId, Date minTransactionDate, Date maxTransactionDate,
                                         Integer stateId, Integer periodId, Integer consumedBy);
}
