package com.idApps.storeCashFlowApi.persistence.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.idApps.storeCashFlowApi.help.constants.CapitalContributionState;
import com.idApps.storeCashFlowApi.help.constants.FinancialPeriodState;
import com.idApps.storeCashFlowApi.help.constants.TransactionType;
import com.idApps.storeCashFlowApi.models.dto.CapitalContributionDto;
import com.idApps.storeCashFlowApi.models.response.ApiResponse;
import com.idApps.storeCashFlowApi.persistence.entity.CapitalContributionEntity;
import com.idApps.storeCashFlowApi.persistence.entity.FinancialPeriodEntity;
import com.idApps.storeCashFlowApi.persistence.entity.TransactionEntity;
import com.idApps.storeCashFlowApi.persistence.repository.CapitalContributionRepository;
import com.idApps.storeCashFlowApi.persistence.repository.TransactionRepository;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Data
@Service
public class CapitalContributionServiceImpl implements CapitalContributionService {

    private final CapitalContributionRepository capitalContributionRepository;
    private final TransactionService transactionService;
    private final CurrentUserService currentUserService;
    private final ObjectMapper objectMapper;
    private final FinancialPeriodService financialPeriodService;

    @Override
    public List<CapitalContributionDto> getCapitalContributions(Date contributionDateMin, Date contributionDateMax, Integer capitalContributionStateId, Integer periodId, Integer contributorId) {
        List<Map<String, Object>> capitalContributionMapList = this.capitalContributionRepository.getCapitalContributions(contributionDateMin, contributionDateMax, capitalContributionStateId, periodId, contributorId);
        Object test = capitalContributionMapList.stream().map(item -> this.objectMapper.convertValue(item, CapitalContributionDto.class)).toList();
        return capitalContributionMapList.stream().map(item -> this.objectMapper.convertValue(item, CapitalContributionDto.class)).toList();
    }

    @Transactional
    @Override
    public ResponseEntity<ApiResponse<CapitalContributionEntity>> addCapitalContribution(CapitalContributionDto capitalContributionDto, String userAccessToken) {
        Integer currentUserId = this.currentUserService.getUserId(userAccessToken);

        // 📅 Récupération de la dernière période financière
        FinancialPeriodEntity lastPeriodEntity = this.financialPeriodService.getLastPeriod().orElse(null);

        // ⚠️ Vérification : période trouvée ?
        if (lastPeriodEntity == null) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(new Exception("⚠️ No financial period found in the system.")));
        }

        // 🔒 Vérification : période clôturée ?
        if (lastPeriodEntity.getStateId() != FinancialPeriodState.IN_PROG) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(new Exception("🔒 The financial period must be in progress.")));
        }

        TransactionEntity transactionEntity = this.objectMapper.convertValue(capitalContributionDto, TransactionEntity.class);
        transactionEntity.setAddingDate(Date.valueOf(LocalDate.now()));
        transactionEntity.setAddingTime(Time.valueOf(LocalTime.now()));
        transactionEntity.setTransactionDate(Date.valueOf(capitalContributionDto.getContributionDate()));
        transactionEntity.setTransactionTime(Time.valueOf(capitalContributionDto.getContributionTime()));
        transactionEntity.setIdTransactionType(TransactionType.CREATE_CONTR_CAPITAL);
        transactionEntity.setIdPeriod(lastPeriodEntity.getId());
        transactionEntity.setExecutedBy(currentUserId);

        TransactionEntity lastSavedTransaction = this.transactionService.getLastTransaction();
        BigDecimal newCurrentCapital = lastSavedTransaction.getCurrentCapital().add(capitalContributionDto.getAmount());
        BigDecimal newCashRegisterBalance = lastSavedTransaction.getCashRegisterBalance().add(capitalContributionDto.getAmount());
        transactionEntity.setCurrentCapital(newCurrentCapital);
        transactionEntity.setCurrentProfitGross(lastSavedTransaction.getCurrentProfitGross());
        transactionEntity.setCurrentProfitNet(lastSavedTransaction.getCurrentProfitNet());
        transactionEntity.setTotalExpenses(lastSavedTransaction.getTotalExpenses());
        transactionEntity.setTotalCustomerCredit(lastSavedTransaction.getTotalCustomerCredit());
        transactionEntity.setTotalExternalLoan(lastSavedTransaction.getTotalExternalLoan());
        transactionEntity.setTotalAdvance(lastSavedTransaction.getTotalAdvance());
        transactionEntity.setTotalConsumableInputs(lastSavedTransaction.getTotalConsumableInputs());
        transactionEntity.setTotalNonConsumableInputs(lastSavedTransaction.getTotalNonConsumableInputs());
        transactionEntity.setCashRegisterBalance(newCashRegisterBalance);
        transactionEntity.setTotalOutOfPocketExpenses(lastSavedTransaction.getTotalOutOfPocketExpenses());
        TransactionEntity transactionEntityResp = this.transactionService.save(transactionEntity);

        CapitalContributionEntity capitalContributionEntity = this.objectMapper.convertValue(capitalContributionDto, CapitalContributionEntity.class);
        capitalContributionEntity.setIdTransaction(transactionEntityResp.getId());
        capitalContributionEntity.setIdState(CapitalContributionState.CONTRIBUTED);

        return ResponseEntity.ok(new ApiResponse<>(this.capitalContributionRepository.save(capitalContributionEntity)));
    }

    @Transactional
    @Override
    public ResponseEntity<ApiResponse<CapitalContributionEntity>> cancelCapitalContribution(String tokenValue, int id) {

        // 👤 Récupération de l'ID de l'utilisateur connecté
        int currentUserId = this.currentUserService.getUserId(tokenValue);

        // 🔍 Récupération de la contribution de capital correspondante à l'ID
        CapitalContributionEntity capitalContributionEntity = this.capitalContributionRepository.findById(id).orElse(null);

        // 🧱 Vérification : existe-t-elle ?
        if (capitalContributionEntity == null) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(new Exception("❌ No capital contribution found for the provided ID.")));
        }

        // 🔗 Récupération de la transaction originale associée
        Optional<TransactionEntity> originalTransactionEntityOptional =
                this.transactionService.findById(capitalContributionEntity.getIdTransaction());
        TransactionEntity originalTransactionEntity = originalTransactionEntityOptional.orElse(null);

        // ⚠️ Vérification : transaction originale trouvée ?
        if (originalTransactionEntityOptional.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(new Exception("⚠️ No original transaction found for this capital contribution.")));
        }

        // 📅 Récupération de la dernière période financière
        FinancialPeriodEntity lastPeriodEntity = this.financialPeriodService.getLastPeriod().orElse(null);

        // 🕓 Vérification : période financière existante ?
        if (lastPeriodEntity == null) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(new Exception("⚠️ No financial period found in the system.")));
        }

        // 📊 Vérification : la transaction originale appartient-elle à la dernière période ?
        if (!originalTransactionEntity.getIdPeriod().equals(lastPeriodEntity.getId())) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(new Exception("🚫 The transaction belongs to a different financial period.")));
        }

        // 🔒 Vérification : la période est-elle clôturée ?
        if (lastPeriodEntity.getStateId() == FinancialPeriodState.CLOSED) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(new Exception("🔒 The financial period has already been closed.")));
        }

        // ❌ Vérification : la période a-t-elle été annulée ?
        if (lastPeriodEntity.getStateId() == FinancialPeriodState.CANCELED) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(new Exception("🚫 The financial period has been canceled — no transactions can be added.")));
        }

        // 🧩 Vérification : état de la contribution avant annulation
        if (capitalContributionEntity.getIdState() != CapitalContributionState.CONTRIBUTED) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(new Exception("⚠️ The capital contribution must be in 'CONTRIBUTED' state to be canceled.")));
        }

        try {
            // 📜 Récupération de la dernière transaction du système
            TransactionEntity lastTransactionEntity = this.transactionService.getLastTransaction();
            if (lastTransactionEntity == null) {
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(new ApiResponse<>(new Exception("⚠️ No previous transaction found in the system.")));
            }

            // 🧬 Copie de la transaction originale pour créer une transaction d’annulation
            TransactionEntity newTransactionEntity = this.objectMapper.convertValue(originalTransactionEntity, TransactionEntity.class);

            // ➕ Calcul des montants inversés
            BigDecimal transactionAmount = originalTransactionEntity.getAmount().multiply(BigDecimal.valueOf(-1));
            BigDecimal newCurrentCapital = lastTransactionEntity.getCurrentCapital().add(transactionAmount);
            BigDecimal newCashRegisterBalance = lastTransactionEntity.getCashRegisterBalance().add(transactionAmount);

            // ⚙️ Préparation de la nouvelle transaction (annulation)
            newTransactionEntity.setId(null);
            newTransactionEntity.setAmount(transactionAmount);
            newTransactionEntity.setAddingDate(Date.valueOf(LocalDate.now()));
            newTransactionEntity.setAddingTime(Time.valueOf(LocalTime.now()));
            newTransactionEntity.setTransactionDate(newTransactionEntity.getAddingDate());
            newTransactionEntity.setTransactionTime(newTransactionEntity.getAddingTime());
            newTransactionEntity.setIdTransactionType(TransactionType.CANCEL_CONTR_CAPITAL);
            newTransactionEntity.setIdPeriod(lastPeriodEntity.getId());
            newTransactionEntity.setExecutedBy(currentUserId);
            newTransactionEntity.setOriginalTransactionId(originalTransactionEntity.getId());

            // 🔄 Synchronisation des totaux avec la dernière transaction
            newTransactionEntity.setCurrentCapital(newCurrentCapital);
            newTransactionEntity.setCurrentProfitGross(lastTransactionEntity.getCurrentProfitGross());
            newTransactionEntity.setCurrentProfitNet(lastTransactionEntity.getCurrentProfitNet());
            newTransactionEntity.setTotalExpenses(lastTransactionEntity.getTotalExpenses());
            newTransactionEntity.setTotalCustomerCredit(lastTransactionEntity.getTotalCustomerCredit());
            newTransactionEntity.setTotalExternalLoan(lastTransactionEntity.getTotalExternalLoan());
            newTransactionEntity.setTotalAdvance(lastTransactionEntity.getTotalAdvance());
            newTransactionEntity.setTotalConsumableInputs(lastTransactionEntity.getTotalConsumableInputs());
            newTransactionEntity.setTotalNonConsumableInputs(lastTransactionEntity.getTotalNonConsumableInputs());
            newTransactionEntity.setCashRegisterBalance(newCashRegisterBalance);
            newTransactionEntity.setTotalOutOfPocketExpenses(lastTransactionEntity.getTotalOutOfPocketExpenses());

            // 💾 Sauvegarde de la transaction d’annulation
            TransactionEntity transactionEntityResp = this.transactionService.save(newTransactionEntity);

            // 🏁 Mise à jour de l’état de la contribution
            capitalContributionEntity.setIdState(CapitalContributionState.CANCELED);

            // ✅ Réponse de succès
            return ResponseEntity.ok(new ApiResponse<>(this.capitalContributionRepository.save(capitalContributionEntity)));
        }
        catch (Exception e) {
            // ❗ Gestion centralisée des erreurs inattendues
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            log.error("Error while canceling capital contribution", e);

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(new Exception("💥 An unexpected error occurred while canceling the capital contribution.")));
        }
    }
}
