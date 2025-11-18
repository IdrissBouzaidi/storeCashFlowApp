package com.idApps.storeCashFlowApi.persistence.repository;

import com.idApps.storeCashFlowApi.persistence.entity.CapitalContributionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.util.List;
import java.util.Map;

@Repository
public interface CapitalContributionRepository extends JpaRepository<CapitalContributionEntity, Integer> {

    @Query(value = """
            SELECT capital_contribution.id, capital_contribution.label, capital_contribution.amount,
                capital_contribution.id_state, capital_contribution.id_transaction, capital_contribution.contributor_id,
                capital_contribution.contribution_date, capital_contribution.contribution_time,
                transaction.transaction_date, transaction.transaction_time, transaction.adding_date, transaction.adding_time,
                transaction.details, transaction.id_transaction_type, transaction.id_period, transaction.executed_by
            FROM capital_contribution
            LEFT JOIN transaction ON capital_contribution.id_transaction=transaction.id
            WHERE (transaction_date>=:contributionDateMin OR :contributionDateMin IS NULL) AND
                (transaction_date<=:contributionDateMax OR :contributionDateMax IS NULL) AND
                (capital_contribution.id_state=:capitalContributionStateId OR :capitalContributionStateId IS NULL) AND
                (transaction.id_period=:periodId OR :periodId IS NULL) AND
                (capital_contribution.contributor_id=:contributorId OR :contributorId IS NULL)
            ;
            """, nativeQuery = true)
    List<Map<String, Object>> getCapitalContributions(Date contributionDateMin, Date contributionDateMax, Integer capitalContributionStateId, Integer periodId, Integer contributorId);
}
