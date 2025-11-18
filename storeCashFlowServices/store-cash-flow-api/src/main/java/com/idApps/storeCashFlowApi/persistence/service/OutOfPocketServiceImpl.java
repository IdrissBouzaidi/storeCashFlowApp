package com.idApps.storeCashFlowApi.persistence.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.idApps.storeCashFlowApi.help.constants.FinancialPeriodState;
import com.idApps.storeCashFlowApi.help.constants.OutOfPocketState;
import com.idApps.storeCashFlowApi.help.constants.TransactionType;
import com.idApps.storeCashFlowApi.models.dto.OutOfPocketDto;
import com.idApps.storeCashFlowApi.models.response.ApiResponse;
import com.idApps.storeCashFlowApi.persistence.entity.FinancialPeriodEntity;
import com.idApps.storeCashFlowApi.persistence.entity.OutOfPocketEntity;
import com.idApps.storeCashFlowApi.persistence.entity.TransactionEntity;
import com.idApps.storeCashFlowApi.persistence.repository.OutOfPocketRepository;
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
public class OutOfPocketServiceImpl implements OutOfPocketService {

    private final CurrentUserService currentUserService;
    private final ObjectMapper objectMapper;
    private final OutOfPocketRepository outOfPocketRepository;
    private final TransactionService transactionService;
    private final FinancialPeriodService financialPeriodService;

    @Override
    public List<OutOfPocketDto> getOutOfPockets(Date borrowingDateMin, Date borrowingDateMax, Integer stateId, Integer idPeriod, Integer borrowerId) {
        List<Map<String, Object>> mapList = this.outOfPocketRepository.getOutOfPockets(borrowingDateMin, borrowingDateMax, stateId, idPeriod, borrowerId);
        return mapList.stream().map(item -> this.objectMapper.convertValue(item, OutOfPocketDto.class)).toList();
    }

    @Transactional
    @Override
    public ResponseEntity<ApiResponse<OutOfPocketEntity>> addOutOfPocket(String currentUserToken, OutOfPocketDto outOfPocketDto) {
        Integer currentUserId = this.currentUserService.getUserId(currentUserToken);


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

        TransactionEntity transactionEntity = this.objectMapper.convertValue(outOfPocketDto, TransactionEntity.class);
        transactionEntity.setAddingDate(Date.valueOf(LocalDate.now()));
        transactionEntity.setAddingTime(Time.valueOf(LocalTime.now()));
        transactionEntity.setTransactionDate(outOfPocketDto.getBorrowingDate());
        transactionEntity.setTransactionTime(outOfPocketDto.getBorrowingTime());
        transactionEntity.setExecutedBy(currentUserId);
        transactionEntity.setIdTransactionType(TransactionType.CREATE_OUT_POCKET);
        transactionEntity.setIdPeriod(lastPeriodEntity.getId());

        TransactionEntity lastSavedTransaction = this.transactionService.getLastTransaction();
        BigDecimal newCashRegisterBalance = lastSavedTransaction.getCashRegisterBalance().add(outOfPocketDto.getAmount());
        BigDecimal newTotalOutOfPocketExpenses = lastSavedTransaction.getTotalOutOfPocketExpenses().add(outOfPocketDto.getAmount());
        transactionEntity.setCurrentCapital(lastSavedTransaction.getCurrentCapital());
        transactionEntity.setCurrentProfitGross(lastSavedTransaction.getCurrentProfitGross());
        transactionEntity.setCurrentProfitNet(lastSavedTransaction.getCurrentProfitNet());
        transactionEntity.setTotalExpenses(lastSavedTransaction.getTotalExpenses());
        transactionEntity.setTotalCustomerCredit(lastSavedTransaction.getTotalCustomerCredit());
        transactionEntity.setTotalExternalLoan(lastSavedTransaction.getTotalExternalLoan());
        transactionEntity.setTotalAdvance(lastSavedTransaction.getTotalAdvance());
        transactionEntity.setTotalConsumableInputs(lastSavedTransaction.getTotalConsumableInputs());
        transactionEntity.setTotalNonConsumableInputs(lastSavedTransaction.getTotalNonConsumableInputs());
        transactionEntity.setCashRegisterBalance(newCashRegisterBalance);
        transactionEntity.setTotalOutOfPocketExpenses(newTotalOutOfPocketExpenses);
        TransactionEntity transactionEntityResp = this.transactionService.save(transactionEntity);

        OutOfPocketEntity outOfPocketEntity = this.objectMapper.convertValue(outOfPocketDto, OutOfPocketEntity.class);
        outOfPocketEntity.setIdTransaction(transactionEntityResp.getId());
        outOfPocketEntity.setStateId(OutOfPocketState.BORROWED);
        return ResponseEntity.ok(new ApiResponse<>(this.outOfPocketRepository.save(outOfPocketEntity)));
    }

    @Transactional
    @Override
    public ResponseEntity<ApiResponse<OutOfPocketEntity>> cancelOutOfPocket(String tokenValue, int id) {

        // 👤 Récupération de l'utilisateur connecté
        int currentUserId = this.currentUserService.getUserId(tokenValue);

        // 🔍 Récupération de la dépense "out of pocket" à annuler
        OutOfPocketEntity outOfPocketEntity = this.outOfPocketRepository.findById(id).orElse(null);

        // ⚠️ Vérification d'existence
        if (outOfPocketEntity == null) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(new Exception("❌ No out-of-pocket expense found for the provided ID.")));
        }

        // 🔗 Récupération de la transaction originale liée à cette dépense
        Optional<TransactionEntity> originalTransactionEntityOptional =
                this.transactionService.findById(outOfPocketEntity.getIdTransaction());
        TransactionEntity originalTransactionEntity = originalTransactionEntityOptional.orElse(null);

        if (originalTransactionEntityOptional.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(new Exception("⚠️ No original transaction found for this expense.")));
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

        // 🧩 Vérification : état de la dépense avant annulation
        if (outOfPocketEntity.getStateId() == OutOfPocketState.CANCELED) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(new Exception("⚠️ The out-of-pocket expense must not be canceled to be canceled.")));
        }

        try {
            // 📜 Récupération de la dernière transaction du système
            TransactionEntity lastTransactionEntity = this.transactionService.getLastTransaction();
            if (lastTransactionEntity == null) {
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(new ApiResponse<>(new Exception("⚠️ No previous transaction found in the system.")));
            }

            // 🧬 Copie de la transaction originale pour créer la transaction d'annulation
            TransactionEntity newTransactionEntity = this.objectMapper.convertValue(originalTransactionEntity, TransactionEntity.class);

            // 💰 Calcul du montant inversé
            BigDecimal transactionAmount = originalTransactionEntity.getAmount().multiply(BigDecimal.valueOf(-1));

            // 🧾 Logique comptable :
            // Une dépense "out of pocket" :
            //   - réduit la trésorerie
            //   - augmente le total des dépenses personnelles
            // En annulant cette dépense :
            //   - on augmente la trésorerie
            //   - on diminue le total des dépenses personnelles
            BigDecimal newCashRegisterBalance = lastTransactionEntity.getCashRegisterBalance().add(transactionAmount);
            BigDecimal newTotalOutOfPocketExpenses = lastTransactionEntity.getTotalOutOfPocketExpenses().add(transactionAmount);

            // ⚙️ Préparation de la nouvelle transaction d'annulation
            newTransactionEntity.setId(null);
            newTransactionEntity.setAmount(transactionAmount);
            newTransactionEntity.setAddingDate(Date.valueOf(LocalDate.now()));
            newTransactionEntity.setAddingTime(Time.valueOf(LocalTime.now()));
            newTransactionEntity.setTransactionDate(newTransactionEntity.getAddingDate());
            newTransactionEntity.setTransactionTime(newTransactionEntity.getAddingTime());
            newTransactionEntity.setIdTransactionType(TransactionType.CANCEL_OUT_POCKET);
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
            newTransactionEntity.setTotalNonConsumableInputs(lastTransactionEntity.getTotalNonConsumableInputs());
            newTransactionEntity.setTotalOutOfPocketExpenses(newTotalOutOfPocketExpenses);
            newTransactionEntity.setCashRegisterBalance(newCashRegisterBalance);

            // 💾 Sauvegarde de la transaction d'annulation
            TransactionEntity transactionEntityResp = this.transactionService.save(newTransactionEntity);

            // 🏁 Mise à jour de l'état de la dépense
            outOfPocketEntity.setStateId(OutOfPocketState.CANCELED);

            // ✅ Réponse de succès
            return ResponseEntity.ok(new ApiResponse<>(this.outOfPocketRepository.save(outOfPocketEntity)));
        }
        catch (Exception e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            log.error("Error while canceling out-of-pocket expense", e);

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(new Exception("💥 An unexpected error occurred while canceling the out-of-pocket expense.")));
        }
    }

}
