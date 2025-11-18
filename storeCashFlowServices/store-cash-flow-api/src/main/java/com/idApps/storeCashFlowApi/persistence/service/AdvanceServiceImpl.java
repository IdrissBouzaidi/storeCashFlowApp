package com.idApps.storeCashFlowApi.persistence.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.idApps.storeCashFlowApi.help.constants.AdvanceState;
import com.idApps.storeCashFlowApi.help.constants.FinancialPeriodState;
import com.idApps.storeCashFlowApi.help.constants.TransactionType;
import com.idApps.storeCashFlowApi.models.dto.AdvanceDto;
import com.idApps.storeCashFlowApi.models.response.ApiResponse;
import com.idApps.storeCashFlowApi.persistence.entity.AdvanceEntity;
import com.idApps.storeCashFlowApi.persistence.entity.FinancialPeriodEntity;
import com.idApps.storeCashFlowApi.persistence.entity.PeriodStateEntity;
import com.idApps.storeCashFlowApi.persistence.entity.TransactionEntity;
import com.idApps.storeCashFlowApi.persistence.repository.AdvanceRepository;
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
import java.util.Optional;

@Slf4j
@Data
@Service
public class AdvanceServiceImpl implements AdvanceService {

    private final CurrentUserService currentUserService;
    private final ObjectMapper objectMapper;
    private final AdvanceRepository advanceRepository;
    private final TransactionService transactionService;
    private final FinancialPeriodService financialPeriodService;

    @Override
    public List<AdvanceDto> getAdvances(Date advanceDateMin, Date advanceDateMax, Integer stateId, Integer periodId, Integer takerId) {
        return this.advanceRepository.getAdvances(advanceDateMin, advanceDateMax, stateId, periodId, takerId).stream().map(item -> this.objectMapper.convertValue(item, AdvanceDto.class)).toList();
    }

    @Transactional
    @Override
    public ResponseEntity<ApiResponse<AdvanceEntity>> addAdvance(String tokenValue, AdvanceDto advanceDto) {
        Integer currentUserId = this.currentUserService.getUserId(tokenValue);

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

        TransactionEntity transactionEntity = this.objectMapper.convertValue(advanceDto, TransactionEntity.class);
        transactionEntity.setTransactionDate(advanceDto.getAdvanceDate());
        transactionEntity.setTransactionTime(advanceDto.getAdvanceTime());
        transactionEntity.setAddingDate(Date.valueOf(LocalDate.now()));
        transactionEntity.setAddingTime(Time.valueOf(LocalTime.now()));
        transactionEntity.setIdTransactionType(TransactionType.CREATE_ADV);
        transactionEntity.setIdPeriod(lastPeriodEntity.getId());
        transactionEntity.setExecutedBy(currentUserId);

        TransactionEntity lastTransactionEntity = this.transactionService.getLastTransaction();
        BigDecimal newTotalAdvance = lastTransactionEntity.getTotalAdvance().add(advanceDto.getAmount());
        BigDecimal newCashRegisterBalance = lastTransactionEntity.getCashRegisterBalance().subtract(advanceDto.getAmount());
        transactionEntity.setCurrentCapital(lastTransactionEntity.getCurrentCapital());
        transactionEntity.setCurrentProfitGross(lastTransactionEntity.getCurrentProfitGross());
        transactionEntity.setCurrentProfitNet(lastTransactionEntity.getCurrentProfitNet());
        transactionEntity.setTotalExpenses(lastTransactionEntity.getTotalExpenses());
        transactionEntity.setTotalCustomerCredit(lastTransactionEntity.getTotalCustomerCredit());
        transactionEntity.setTotalExternalLoan(lastTransactionEntity.getTotalExternalLoan());
        transactionEntity.setTotalAdvance(newTotalAdvance);
        transactionEntity.setTotalConsumableInputs(lastTransactionEntity.getTotalConsumableInputs());
        transactionEntity.setTotalNonConsumableInputs(lastTransactionEntity.getTotalNonConsumableInputs());
        transactionEntity.setCashRegisterBalance(newCashRegisterBalance);
        transactionEntity.setTotalOutOfPocketExpenses(lastTransactionEntity.getTotalOutOfPocketExpenses());
        TransactionEntity transactionEntityResp = this.transactionService.save(transactionEntity);

        AdvanceEntity advanceEntity = this.objectMapper.convertValue(advanceDto, AdvanceEntity.class);
        advanceEntity.setIdTransaction(transactionEntityResp.getId());
        advanceEntity.setStateId(AdvanceState.ADVANCED);
        return ResponseEntity.ok(new ApiResponse<>(this.advanceRepository.save(advanceEntity)));
    }

    @Transactional
    @Override
    public ResponseEntity<ApiResponse<AdvanceEntity>> cancelAdvance(String tokenValue, int idAdvance) {
        // 👤 Récupération de l'ID de l'utilisateur connecté
        int currentUserId = this.currentUserService.getUserId(tokenValue);

        // 🔍 Récupération de l'entité Advance correspondante à l'id
        AdvanceEntity advanceEntity = this.advanceRepository.findById(idAdvance).orElse(null);

        // 🧱 Vérification : existe-t-elle ?
        if (advanceEntity == null) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(new Exception("❌ No advance transaction found for the provided ID.")));
        }

        // 🔗 Récupération de la transaction originale associée à cette avance
        Optional<TransactionEntity> originalTransactionEntityOptional =
                this.transactionService.findById(advanceEntity.getIdTransaction());
        TransactionEntity originalTransactionEntity = originalTransactionEntityOptional.orElse(null);

        // ⚠️ Vérification : transaction originale trouvée ?
        if (originalTransactionEntityOptional.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(new Exception("⚠️ No original transaction found for this advance.")));
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

        // 🧩 Vérification : état de l'avance avant annulation
        if (advanceEntity.getStateId() != AdvanceState.ADVANCED) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(new Exception("⚠️ The advance must be in 'ADVANCED' state to be canceled.")));
        }

        try {
            // 📜 Récupération de la dernière transaction dans le système
            TransactionEntity lastTransactionEntity = this.transactionService.getLastTransaction();
            if (lastTransactionEntity == null) {
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(new ApiResponse<>(new Exception("⚠️ No previous transaction found in the system.")));
            }

            // 🧬 Copie de la transaction originale pour créer une nouvelle transaction d'annulation
            TransactionEntity newTransactionEntity = this.objectMapper.convertValue(originalTransactionEntity, TransactionEntity.class);

            // ➕ Calcul des montants inversés
            BigDecimal transactionAmount = originalTransactionEntity.getAmount().multiply(BigDecimal.valueOf(-1));
            BigDecimal newTotalAdvance = lastTransactionEntity.getTotalAdvance().add(transactionAmount);
            BigDecimal newCashRegisterBalance = lastTransactionEntity.getCashRegisterBalance().subtract(transactionAmount);

            // ⚙️ Préparation de la nouvelle transaction (annulation)
            newTransactionEntity.setId(null);
            newTransactionEntity.setAmount(transactionAmount);
            newTransactionEntity.setAddingDate(Date.valueOf(LocalDate.now()));
            newTransactionEntity.setAddingTime(Time.valueOf(LocalTime.now()));
            newTransactionEntity.setTransactionDate(newTransactionEntity.getAddingDate());
            newTransactionEntity.setTransactionTime(newTransactionEntity.getAddingTime());
            newTransactionEntity.setIdTransactionType(newTransactionEntity.getIdTransactionType());
            newTransactionEntity.setIdTransactionType(TransactionType.CANCEL_ADV);
            newTransactionEntity.setIdPeriod(lastPeriodEntity.getId());
            newTransactionEntity.setExecutedBy(currentUserId);
            newTransactionEntity.setOriginalTransactionId(originalTransactionEntity.getId());

            // 🔄 Synchronisation des totaux avec la dernière transaction
            newTransactionEntity.setCurrentCapital(lastTransactionEntity.getCurrentCapital());
            newTransactionEntity.setCurrentProfitGross(lastTransactionEntity.getCurrentProfitGross());
            newTransactionEntity.setCurrentProfitNet(lastTransactionEntity.getCurrentProfitNet());
            newTransactionEntity.setTotalExpenses(lastTransactionEntity.getTotalExpenses());
            newTransactionEntity.setTotalCustomerCredit(lastTransactionEntity.getTotalCustomerCredit());
            newTransactionEntity.setTotalExternalLoan(lastTransactionEntity.getTotalExternalLoan());
            newTransactionEntity.setTotalAdvance(newTotalAdvance);
            newTransactionEntity.setTotalConsumableInputs(lastTransactionEntity.getTotalConsumableInputs());
            newTransactionEntity.setTotalNonConsumableInputs(lastTransactionEntity.getTotalNonConsumableInputs());
            newTransactionEntity.setCashRegisterBalance(newCashRegisterBalance);
            newTransactionEntity.setTotalOutOfPocketExpenses(lastTransactionEntity.getTotalOutOfPocketExpenses());

            // 💾 Sauvegarde de la transaction d'annulation
            TransactionEntity transactionEntityResp = this.transactionService.save(newTransactionEntity);

            // 🏁 Mise à jour de l'état de l'avance
            advanceEntity.setStateId(AdvanceState.CANCELED);

            // ✅ Réponse de succès
            return ResponseEntity.ok(new ApiResponse<>(this.advanceRepository.save(advanceEntity)));

        }
        catch (Exception e) {
            // ❗ Gestion centralisée des erreurs inattendues
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            log.error("Error while canceling advance transaction", e);

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(new Exception("💥 An unexpected error occurred while canceling the advance transaction.")));
        }
    }

}
