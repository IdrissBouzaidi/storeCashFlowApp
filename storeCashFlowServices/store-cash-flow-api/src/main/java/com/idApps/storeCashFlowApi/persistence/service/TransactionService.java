package com.idApps.storeCashFlowApi.persistence.service;

import com.idApps.storeCashFlowApi.models.dto.TransactionDto;
import com.idApps.storeCashFlowApi.persistence.entity.TransactionEntity;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TransactionService {
    List<TransactionDto> getTransactions(Integer idTransactionType, Integer idPeriod, Integer executedBy,
                                         LocalDate transactionDateMin, LocalDate transactionDateMax);

    TransactionEntity getLastTransaction();

    TransactionEntity save(TransactionEntity transactionEntity);

    Optional<TransactionEntity> findById(int id);

    List<TransactionEntity> saveAll(List<TransactionEntity> transactionEntityList);

    static void setFinancialTotals(TransactionEntity originInstance, TransactionEntity destinationInstance) {

        destinationInstance.setCurrentCapital(originInstance.getCurrentCapital());
        destinationInstance.setCurrentProfitGross(originInstance.getCurrentProfitGross());
        destinationInstance.setCurrentProfitNet(originInstance.getCurrentProfitNet());
        destinationInstance.setTotalExpenses(originInstance.getTotalExpenses());
        destinationInstance.setTotalCustomerCredit(originInstance.getTotalCustomerCredit());
        destinationInstance.setTotalExternalLoan(originInstance.getTotalExternalLoan());
        destinationInstance.setTotalAdvance(originInstance.getTotalAdvance());
        destinationInstance.setTotalConsumableInputs(originInstance.getTotalConsumableInputs());
        destinationInstance.setTotalNonConsumableInputs(originInstance.getTotalNonConsumableInputs());
        destinationInstance.setCashRegisterBalance(originInstance.getCashRegisterBalance());
        destinationInstance.setTotalOutOfPocketExpenses(originInstance.getTotalOutOfPocketExpenses());
    }
}
