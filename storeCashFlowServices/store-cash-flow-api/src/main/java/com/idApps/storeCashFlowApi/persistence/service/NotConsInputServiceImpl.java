package com.idApps.storeCashFlowApi.persistence.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.idApps.storeCashFlowApi.help.constants.FinancialPeriodState;
import com.idApps.storeCashFlowApi.help.constants.NotConsInputState;
import com.idApps.storeCashFlowApi.help.constants.TransactionType;
import com.idApps.storeCashFlowApi.models.dto.NotConsInputDto;
import com.idApps.storeCashFlowApi.models.response.ApiResponse;
import com.idApps.storeCashFlowApi.persistence.entity.FinancialPeriodEntity;
import com.idApps.storeCashFlowApi.persistence.entity.InputEntity;
import com.idApps.storeCashFlowApi.persistence.entity.NotConsInputEntity;
import com.idApps.storeCashFlowApi.persistence.entity.TransactionEntity;
import com.idApps.storeCashFlowApi.persistence.repository.InputRepository;
import com.idApps.storeCashFlowApi.persistence.repository.NotConsInputRepository;
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
public class NotConsInputServiceImpl implements NotConsInputService{

    private final NotConsInputRepository notConsInputRepository;
    private final InputService inputService;
    private final TransactionService transactionService;
    private final CurrentUserService currentUserService;
    private final ObjectMapper objectMapper;
    private final FinancialPeriodService financialPeriodService;

    @Override
    public List<NotConsInputDto> getNotConsInputs(LocalDate transactionDateMin, LocalDate transactionDateMax, Integer periodId, Integer executedById, Integer notConsInputStateId) {
        return this.notConsInputRepository.getNotconsInputs(transactionDateMin, transactionDateMax, periodId, executedById, notConsInputStateId).stream().map(item -> this.objectMapper.convertValue(item, NotConsInputDto.class)).toList();
    }

    @Transactional
    @Override
    public ResponseEntity<ApiResponse<NotConsInputEntity>> addNotConsInput(String userAccessToken, NotConsInputDto notConsInputDto) {
        Integer currentUserId = this.currentUserService.getUserId(userAccessToken);
        BigDecimal total = notConsInputDto.getCost().multiply(BigDecimal.valueOf(notConsInputDto.getInitialQuantity()));

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

        TransactionEntity transactionEntity = this.objectMapper.convertValue(notConsInputDto, TransactionEntity.class);
        transactionEntity.setAddingDate(Date.valueOf(LocalDate.now()));
        transactionEntity.setAddingTime(Time.valueOf(LocalTime.now()));
        transactionEntity.setAmount(total);
        transactionEntity.setIdTransactionType(TransactionType.CREATE_NOT_CONS_INP);
        transactionEntity.setIdPeriod(lastPeriodEntity.getId());
        transactionEntity.setExecutedBy(currentUserId);

        TransactionEntity lastSavedTransaction = this.transactionService.getLastTransaction();
        BigDecimal newTotalNonConsumableInput = lastSavedTransaction.getTotalNonConsumableInputs().add(total);
        BigDecimal newCashRegisterBalance = lastSavedTransaction.getCashRegisterBalance().subtract(total);

        transactionEntity.setCurrentCapital(lastSavedTransaction.getCurrentCapital());
        transactionEntity.setCurrentProfitGross(lastSavedTransaction.getCurrentProfitGross());
        transactionEntity.setCurrentProfitNet(lastSavedTransaction.getCurrentProfitNet());
        transactionEntity.setTotalExpenses(lastSavedTransaction.getTotalExpenses());
        transactionEntity.setTotalCustomerCredit(lastSavedTransaction.getTotalCustomerCredit());
        transactionEntity.setTotalExternalLoan(lastSavedTransaction.getTotalExternalLoan());
        transactionEntity.setTotalAdvance(lastSavedTransaction.getTotalAdvance());
        transactionEntity.setTotalConsumableInputs(lastSavedTransaction.getTotalConsumableInputs());
        transactionEntity.setTotalNonConsumableInputs(newTotalNonConsumableInput);
        transactionEntity.setCashRegisterBalance(newCashRegisterBalance);
        transactionEntity.setTotalOutOfPocketExpenses(lastSavedTransaction.getTotalOutOfPocketExpenses());
        TransactionEntity transactionEntityResp = this.transactionService.save(transactionEntity);

        InputEntity inputEntity = this.objectMapper.convertValue(notConsInputDto, InputEntity.class);
        inputEntity.setTotal(total);
        inputEntity.setIdTransaction(transactionEntityResp.getId());
        InputEntity inputEntityResp = this.inputService.save(inputEntity);

        NotConsInputEntity notConsInputEntity = this.objectMapper.convertValue(notConsInputDto, NotConsInputEntity.class);
        notConsInputEntity.setStateId(NotConsInputState.BUYED);
        notConsInputEntity.setIdInput(inputEntityResp.getId());
        return ResponseEntity.ok(new ApiResponse<>(this.notConsInputRepository.save(notConsInputEntity)));
    }

    @Transactional
    @Override
    public ResponseEntity<ApiResponse<NotConsInputEntity>> cancelNotConsInput(String tokenValue, int id) {

        // 👤 Récupération de l'utilisateur connecté
        int currentUserId = this.currentUserService.getUserId(tokenValue);

        // 🔍 Récupération de l'intrant non consommable
        NotConsInputEntity notConsInputEntity = this.notConsInputRepository.findById(id).orElse(null);
        InputEntity inputEntity = this.inputService.findById(notConsInputEntity.getIdInput()).orElse(null);

        // ⚠️ Vérification d'existence
        if (notConsInputEntity == null) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(new Exception("❌ No non-consumable input found for the provided ID.")));
        }

        // 🔗 Récupération de la transaction originale liée
        Optional<TransactionEntity> originalTransactionEntityOptional =
                this.transactionService.findById(inputEntity.getIdTransaction());
        TransactionEntity originalTransactionEntity = originalTransactionEntityOptional.orElse(null);

        if (originalTransactionEntityOptional.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(new Exception("⚠️ No original transaction found for this input.")));
        }

        // 📅 Récupération de la dernière période financière
        FinancialPeriodEntity lastPeriodEntity = this.financialPeriodService.getLastPeriod().orElse(null);

        if (lastPeriodEntity == null) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(new Exception("⚠️ No financial period found in the system.")));
        }

        // 🚫 Vérification : la transaction appartient-elle à la dernière période ?
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

        // ❌ Vérification : la période est-elle annulée ?
        if (lastPeriodEntity.getStateId() == FinancialPeriodState.CANCELED) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(new Exception("🚫 The financial period has been canceled — no transactions can be added.")));
        }

        // 🧩 Vérification : état avant annulation
        if (notConsInputEntity.getStateId() == NotConsInputState.CANCELED) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(new Exception("⚠️ The non-consumable input must not be canceled.")));
        }

        try {
            // 📜 Récupération de la dernière transaction du système
            TransactionEntity lastTransactionEntity = this.transactionService.getLastTransaction();
            if (lastTransactionEntity == null) {
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(new ApiResponse<>(new Exception("⚠️ No previous transaction found in the system.")));
            }

            // 🧬 Copie de la transaction originale pour créer une transaction d'annulation
            TransactionEntity newTransactionEntity = this.objectMapper.convertValue(originalTransactionEntity, TransactionEntity.class);

            // 💰 Calcul du montant d’annulation (opposé du montant initial)
            BigDecimal transactionAmount = originalTransactionEntity.getAmount().multiply(BigDecimal.valueOf(-1));

            // 🧾 Effet comptable :
            // L’achat d’un intrant non consommable :
            //  - diminue la trésorerie
            //  - augmente le total des intrants non consommables
            // Donc, lors de l’annulation :
            //  - augmente la trésorerie
            //  - diminue le total des intrants non consommables
            BigDecimal newTotalNonConsumableInput = lastTransactionEntity.getTotalNonConsumableInputs().add(transactionAmount);
            BigDecimal newCashRegisterBalance = lastTransactionEntity.getCashRegisterBalance().subtract(transactionAmount);

            // ⚙️ Préparation de la nouvelle transaction (annulation)
            newTransactionEntity.setId(null);
            newTransactionEntity.setAmount(transactionAmount);
            newTransactionEntity.setAddingDate(Date.valueOf(LocalDate.now()));
            newTransactionEntity.setAddingTime(Time.valueOf(LocalTime.now()));
            newTransactionEntity.setTransactionDate(newTransactionEntity.getAddingDate());
            newTransactionEntity.setTransactionTime(newTransactionEntity.getAddingTime());
            newTransactionEntity.setIdTransactionType(TransactionType.CANCEL_NOT_CONS_INP);
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
            newTransactionEntity.setTotalAdvance(lastTransactionEntity.getTotalAdvance());
            newTransactionEntity.setTotalConsumableInputs(lastTransactionEntity.getTotalConsumableInputs());
            newTransactionEntity.setTotalNonConsumableInputs(newTotalNonConsumableInput);
            newTransactionEntity.setCashRegisterBalance(newCashRegisterBalance);
            newTransactionEntity.setTotalOutOfPocketExpenses(lastTransactionEntity.getTotalOutOfPocketExpenses());

            // 💾 Sauvegarde de la transaction d’annulation
            TransactionEntity transactionEntityResp = this.transactionService.save(newTransactionEntity);

            // 🏁 Mise à jour de l’état de l’intrant
            notConsInputEntity.setStateId(NotConsInputState.CANCELED);

            // ✅ Sauvegarde et réponse
            return ResponseEntity.ok(new ApiResponse<>(this.notConsInputRepository.save(notConsInputEntity)));
        }
        catch (Exception e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            log.error("Error while canceling non-consumable input", e);

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(new Exception("💥 An unexpected error occurred while canceling the non-consumable input.")));
        }
    }

}
