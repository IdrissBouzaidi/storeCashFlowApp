package com.idApps.storeCashFlowApi.persistence.repository;

import com.idApps.storeCashFlowApi.persistence.entity.ExternalLoanEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.util.List;
import java.util.Map;

@Repository
public interface ExternalLoanRepository extends JpaRepository<ExternalLoanEntity, Integer> {

    @Query(value = """
                SELECT external_loan.id, external_loan.label, external_loan.initial_amount, external_loan.paid_amount, external_loan.remaining_amount,
                        external_loan.state_id, external_loan.loan_date, external_loan.loan_time, external_loan.creditor_id,
                        transaction.adding_date, transaction.adding_time, transaction.details, transaction.id_period, transaction.executed_by
                        FROM external_loan
                LEFT JOIN transaction ON external_loan.id_transaction = transaction.id
                WHERE (:loanDateMin IS NULL OR :loanDateMin<=external_loan.loan_date) AND
                        (:loanDateMax IS NULL OR :loanDateMax>=external_loan.loan_date) AND
                        (:stateId IS NULL OR :stateId=external_loan.state_id) AND
                        (:periodId IS NULL OR :periodId=transaction.id_period) AND
                        (:creditorId IS NULL OR :creditorId=external_loan.creditor_id);
            """, nativeQuery = true)
    List<Map<String, Object>> getExternalLoans(Date loanDateMin, Date loanDateMax, Integer stateId, Integer periodId, Integer creditorId);
}