package com.idApps.storeCashFlowApi.persistence.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.idApps.storeCashFlowApi.models.dto.TransactionDto;
import com.idApps.storeCashFlowApi.persistence.entity.TransactionEntity;
import com.idApps.storeCashFlowApi.persistence.repository.TransactionRepository;
import jakarta.persistence.Column;
import lombok.Data;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Data
@Service
public class TransactionServiceImpl implements TransactionService {
    private final TransactionRepository transactionRepository;
    private final CurrentUserService currentUserService;
    private final ObjectMapper objectMapper;


    @Override
    public List<TransactionDto> getTransactions(Integer idTransactionType, Integer idPeriod, Integer executedBy, LocalDate transactionDateMin, LocalDate transactionDateMax) {
        List<Map<String, Object>> transactionMapList = this.transactionRepository.getTransactions(idTransactionType, idPeriod, executedBy, transactionDateMin, transactionDateMax);
        return transactionMapList.stream().map(map -> this.objectMapper.convertValue(map, TransactionDto.class)).toList();
    }

    @Override
    public TransactionEntity getLastTransaction() {
        Optional<TransactionEntity> lastTransactionEntity = this.transactionRepository.getLastTransaction();
        if(lastTransactionEntity.isEmpty()) {
            //La première transaction à effectuer dans la base de données.
            TransactionEntity newTransactionEntity = new TransactionEntity();
            newTransactionEntity.setCurrentCapital(BigDecimal.ZERO);
            newTransactionEntity.setCurrentProfitGross(BigDecimal.ZERO);
            newTransactionEntity.setCurrentProfitNet(BigDecimal.ZERO);
            newTransactionEntity.setTotalExpenses(BigDecimal.ZERO);
            newTransactionEntity.setTotalCustomerCredit(BigDecimal.ZERO);
            newTransactionEntity.setTotalExternalLoan(BigDecimal.ZERO);
            newTransactionEntity.setTotalAdvance(BigDecimal.ZERO);
            newTransactionEntity.setTotalConsumableInputs(BigDecimal.ZERO);
            newTransactionEntity.setTotalNonConsumableInputs(BigDecimal.ZERO);
            newTransactionEntity.setCashRegisterBalance(BigDecimal.ZERO);
            newTransactionEntity.setTotalOutOfPocketExpenses(BigDecimal.ZERO);
            return newTransactionEntity;
        }
        //Le cas où il y a déjà une transaction dans la table.
        return lastTransactionEntity.get();
    }

    @Override
    public TransactionEntity save(TransactionEntity transactionEntity) {
        return this.transactionRepository.save(transactionEntity);
    }

    @Override
    public Optional<TransactionEntity> findById(int id) {
        return this.transactionRepository.findById(id);
    }

    @Override
    public List<TransactionEntity> saveAll(List<TransactionEntity> transactionEntityList) {
        return this.transactionRepository.saveAll(transactionEntityList);
    }
}
