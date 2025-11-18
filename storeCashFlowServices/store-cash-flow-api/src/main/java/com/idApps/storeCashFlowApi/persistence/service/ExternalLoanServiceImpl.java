package com.idApps.storeCashFlowApi.persistence.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.idApps.storeCashFlowApi.help.constants.ExternalLoanState;
import com.idApps.storeCashFlowApi.help.constants.FinancialPeriodState;
import com.idApps.storeCashFlowApi.help.constants.TransactionType;
import com.idApps.storeCashFlowApi.models.dto.ExternalLoanDto;
import com.idApps.storeCashFlowApi.models.response.ApiResponse;
import com.idApps.storeCashFlowApi.persistence.entity.ExternalLoanEntity;
import com.idApps.storeCashFlowApi.persistence.entity.FinancialPeriodEntity;
import com.idApps.storeCashFlowApi.persistence.entity.TransactionEntity;
import com.idApps.storeCashFlowApi.persistence.repository.ExternalLoanRepository;
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
public class ExternalLoanServiceImpl implements ExternalLoanService {

    private final ObjectMapper objectMapper;
    private final CurrentUserService currentUserService;
    private final ExternalLoanRepository externalLoanRepository;
    private final TransactionService transactionService;
    private final FinancialPeriodService financialPeriodService;

    @Override
    public List<ExternalLoanDto> getExternalLoans(Date loanDateMin, Date loanDateMax, Integer stateId, Integer periodId, Integer creditorId) {
        return this.externalLoanRepository.getExternalLoans(loanDateMin, loanDateMax, stateId, periodId, creditorId)
                .stream().map(item -> this.objectMapper.convertValue(item, ExternalLoanDto.class)).toList();
    }

    @Transactional
    @Override
    public ResponseEntity<ApiResponse<ExternalLoanEntity>> addExternalLoan(ExternalLoanDto externalLoanDto, String tokenValue) {
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

        TransactionEntity transactionEntity = this.objectMapper.convertValue(externalLoanDto, TransactionEntity.class);
        transactionEntity.setTransactionDate(externalLoanDto.getLoanDate());
        transactionEntity.setTransactionTime(externalLoanDto.getLoanTime());
        transactionEntity.setAddingDate(Date.valueOf(LocalDate.now()));
        transactionEntity.setAddingTime(Time.valueOf(LocalTime.now()));
        transactionEntity.setIdTransactionType(TransactionType.CREATE_EXTERNAL_LOAN);
        transactionEntity.setIdPeriod(lastPeriodEntity.getId());
        transactionEntity.setAmount(externalLoanDto.getInitialAmount());
        transactionEntity.setExecutedBy(currentUserId);

        TransactionEntity lastSavedTransaction = this.transactionService.getLastTransaction();
        BigDecimal newExternalLoan = lastSavedTransaction.getTotalExternalLoan().add(externalLoanDto.getInitialAmount());
        BigDecimal newRegisterBalance = lastSavedTransaction.getCashRegisterBalance().add(externalLoanDto.getInitialAmount());
        transactionEntity.setCurrentCapital(lastSavedTransaction.getCurrentCapital());
        transactionEntity.setCurrentProfitGross(lastSavedTransaction.getCurrentProfitGross());
        transactionEntity.setCurrentProfitNet(lastSavedTransaction.getCurrentProfitNet());
        transactionEntity.setTotalExpenses(lastSavedTransaction.getTotalExpenses());
        transactionEntity.setTotalCustomerCredit(lastSavedTransaction.getTotalCustomerCredit());
        transactionEntity.setTotalExternalLoan(newExternalLoan);
        transactionEntity.setTotalAdvance(lastSavedTransaction.getTotalAdvance());
        transactionEntity.setTotalConsumableInputs(lastSavedTransaction.getTotalConsumableInputs());
        transactionEntity.setTotalNonConsumableInputs(lastSavedTransaction.getTotalNonConsumableInputs());
        transactionEntity.setCashRegisterBalance(newRegisterBalance);
        transactionEntity.setTotalOutOfPocketExpenses(lastSavedTransaction.getTotalOutOfPocketExpenses());
        TransactionEntity transactionEntityResp = this.transactionService.save(transactionEntity);

        ExternalLoanEntity externalLoanEntity = this.objectMapper.convertValue(externalLoanDto, ExternalLoanEntity.class);
        externalLoanEntity.setIdTransaction(transactionEntityResp.getId());
        externalLoanEntity.setPaidAmount(BigDecimal.valueOf(0));
        externalLoanEntity.setRemainingAmount(externalLoanDto.getInitialAmount());
        externalLoanEntity.setStateId(ExternalLoanState.BORROWED);
        return ResponseEntity.ok(new ApiResponse<>(this.externalLoanRepository.save(externalLoanEntity)));
    }

    @Transactional
    @Override
    public ResponseEntity<ApiResponse<ExternalLoanEntity>> cancelExternalLoan(String tokenValue, int id) {

        // 👤 Récupération de l'ID de l'utilisateur connecté
        int currentUserId = this.currentUserService.getUserId(tokenValue);

        // 🔍 Récupération du prêt externe correspondant à l'ID
        ExternalLoanEntity externalLoanEntity = this.externalLoanRepository.findById(id).orElse(null);

        // ⚠️ Vérification : existe-t-il ?
        if (externalLoanEntity == null) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(new Exception("❌ No external loan found for the provided ID.")));
        }

        // 🔗 Récupération de la transaction originale associée
        Optional<TransactionEntity> originalTransactionEntityOptional =
                this.transactionService.findById(externalLoanEntity.getIdTransaction());
        TransactionEntity originalTransactionEntity = originalTransactionEntityOptional.orElse(null);

        // ⚠️ Vérification : transaction originale trouvée ?
        if (originalTransactionEntityOptional.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(new Exception("⚠️ No original transaction found for this external loan.")));
        }

        // 📅 Récupération de la dernière période financière
        FinancialPeriodEntity lastPeriodEntity = this.financialPeriodService.getLastPeriod().orElse(null);

        // ⚠️ Vérification : période existante ?
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

        // 🧩 Vérification : état du prêt avant annulation
        if (externalLoanEntity.getStateId() == ExternalLoanState.CANCELED) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(new Exception("⚠️ The external loan must not be canceled to be canceled.")));
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

            // 💰 Un prêt externe augmente normalement la trésorerie et le total des prêts externes.
            // En annulant ce prêt, on fait donc l’inverse :
            BigDecimal newExternalLoan = lastTransactionEntity.getTotalExternalLoan().add(transactionAmount);
            BigDecimal newRegisterBalance = lastTransactionEntity.getCashRegisterBalance().add(transactionAmount);

            // ⚙️ Préparation de la nouvelle transaction (annulation)
            newTransactionEntity.setId(null);
            newTransactionEntity.setAmount(transactionAmount);
            newTransactionEntity.setAddingDate(Date.valueOf(LocalDate.now()));
            newTransactionEntity.setAddingTime(Time.valueOf(LocalTime.now()));
            newTransactionEntity.setTransactionDate(newTransactionEntity.getAddingDate());
            newTransactionEntity.setTransactionTime(newTransactionEntity.getAddingTime());
            newTransactionEntity.setIdTransactionType(TransactionType.CANCEL_EXTERNAL_LOAN);
            newTransactionEntity.setIdPeriod(lastPeriodEntity.getId());
            newTransactionEntity.setExecutedBy(currentUserId);
            newTransactionEntity.setOriginalTransactionId(originalTransactionEntity.getId());

            // 🔄 Synchronisation des totaux avec la dernière transaction
            newTransactionEntity.setCurrentCapital(lastTransactionEntity.getCurrentCapital());
            newTransactionEntity.setCurrentProfitGross(lastTransactionEntity.getCurrentProfitGross());
            newTransactionEntity.setCurrentProfitNet(lastTransactionEntity.getCurrentProfitNet());
            newTransactionEntity.setTotalExpenses(lastTransactionEntity.getTotalExpenses());
            newTransactionEntity.setTotalCustomerCredit(lastTransactionEntity.getTotalCustomerCredit());
            newTransactionEntity.setTotalExternalLoan(newExternalLoan);
            newTransactionEntity.setTotalAdvance(lastTransactionEntity.getTotalAdvance());
            newTransactionEntity.setTotalConsumableInputs(lastTransactionEntity.getTotalConsumableInputs());
            newTransactionEntity.setTotalNonConsumableInputs(lastTransactionEntity.getTotalNonConsumableInputs());
            newTransactionEntity.setCashRegisterBalance(newRegisterBalance);
            newTransactionEntity.setTotalOutOfPocketExpenses(lastTransactionEntity.getTotalOutOfPocketExpenses());

            // 💾 Sauvegarde de la transaction d’annulation
            TransactionEntity transactionEntityResp = this.transactionService.save(newTransactionEntity);

            // 🏁 Mise à jour de l’état du prêt externe
            externalLoanEntity.setStateId(ExternalLoanState.CANCELED);

            // ✅ Réponse de succès
            return ResponseEntity.ok(new ApiResponse<>(this.externalLoanRepository.save(externalLoanEntity)));
        }
        catch (Exception e) {
            // ❗ Gestion centralisée des erreurs inattendues
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            log.error("Error while canceling external loan", e);

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(new Exception("💥 An unexpected error occurred while canceling the external loan.")));
        }
    }

}
