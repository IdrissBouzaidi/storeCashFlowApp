package com.idApps.storeCashFlowApi.persistence.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.idApps.storeCashFlowApi.help.constants.ChargeTransactionState;
import com.idApps.storeCashFlowApi.help.constants.FinancialPeriodState;
import com.idApps.storeCashFlowApi.help.constants.TransactionType;
import com.idApps.storeCashFlowApi.models.dto.ChargeDto;
import com.idApps.storeCashFlowApi.models.response.ApiResponse;
import com.idApps.storeCashFlowApi.persistence.entity.ChargeEntity;
import com.idApps.storeCashFlowApi.persistence.entity.ChargeTypeEntity;
import com.idApps.storeCashFlowApi.persistence.entity.FinancialPeriodEntity;
import com.idApps.storeCashFlowApi.persistence.entity.TransactionEntity;
import com.idApps.storeCashFlowApi.persistence.repository.ChargeRepository;
import com.idApps.storeCashFlowApi.persistence.repository.ChargeTypeRepository;
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
public class ChargeServiceImpl implements ChargeService {

    final private ChargeRepository chargeRepository;
    final private ChargeTypeRepository chargeTypeRepository;
    final private TransactionService transactionService;
    final private ObjectMapper objectMapper;
    final private CurrentUserService currentUserService;
    final private FinancialPeriodService financialPeriodService;

    @Override
    public List<ChargeDto> getCharges(Integer chargeTypeId, Date minTransactionDate, Date maxTransactionDate,
                                      Integer stateId, Integer periodId, Integer consumedBy) {
        List<Map<String, Object>> resultMapList = this.chargeRepository.getCharges(chargeTypeId, minTransactionDate,
                                        maxTransactionDate, stateId, periodId, consumedBy);
        return resultMapList.stream().map(item -> this.objectMapper.convertValue(item, ChargeDto.class)).toList();
    }

    @Transactional
    @Override
    public ResponseEntity<ApiResponse<ChargeDto>> addCharge(ChargeDto chargeDto, String userAccessToken) {
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

        TransactionEntity transactionEntity = this.objectMapper.convertValue(chargeDto, TransactionEntity.class);
        BigDecimal totalCost = chargeDto.getCost().multiply(BigDecimal.valueOf(chargeDto.getQuantity()));
        transactionEntity.setAmount(totalCost);
        transactionEntity.setAddingDate(Date.valueOf(LocalDate.now()));
        transactionEntity.setAddingTime(Time.valueOf(LocalTime.now()));
        transactionEntity.setIdTransactionType(TransactionType.CREATE_CHAR);
        transactionEntity.setIdPeriod(lastPeriodEntity.getId());
        transactionEntity.setExecutedBy(currentUserId);
        if(transactionEntity.getImageSrc() == null && chargeDto.getIdChargeType() != null) {
            ChargeTypeEntity chargeTypeEntity = this.chargeTypeRepository.getReferenceById(chargeDto.getIdChargeType());
            transactionEntity.setImageSrc(chargeTypeEntity.getImageSrc());
        }

        TransactionEntity lastSavedTransaction = this.transactionService.getLastTransaction();
        BigDecimal newProfitNet = lastSavedTransaction.getCurrentProfitNet().subtract(totalCost);
        BigDecimal newTotalExpenses = lastSavedTransaction.getTotalExpenses().add(totalCost);
        BigDecimal newCashRegisterBalance = lastSavedTransaction.getCashRegisterBalance().subtract(totalCost);

        transactionEntity.setCurrentCapital(lastSavedTransaction.getCurrentCapital());
        transactionEntity.setCurrentProfitGross(lastSavedTransaction.getCurrentProfitGross());
        transactionEntity.setCurrentProfitNet(newProfitNet);
        transactionEntity.setTotalExpenses(newTotalExpenses);
        transactionEntity.setTotalCustomerCredit(lastSavedTransaction.getTotalCustomerCredit());
        transactionEntity.setTotalExternalLoan(lastSavedTransaction.getTotalExternalLoan());
        transactionEntity.setTotalAdvance(lastSavedTransaction.getTotalAdvance());
        transactionEntity.setTotalConsumableInputs(lastSavedTransaction.getTotalConsumableInputs());
        transactionEntity.setTotalNonConsumableInputs(lastSavedTransaction.getTotalNonConsumableInputs());
        transactionEntity.setCashRegisterBalance(newCashRegisterBalance);
        transactionEntity.setTotalOutOfPocketExpenses(lastSavedTransaction.getTotalOutOfPocketExpenses());
        TransactionEntity transactionEntityResp = this.transactionService.save(transactionEntity);

        ChargeEntity chargeEntity = this.objectMapper.convertValue(chargeDto, ChargeEntity.class);
        chargeEntity.setIdTransaction(transactionEntityResp.getId());
        chargeEntity.setTotal(totalCost);
        chargeEntity.setIdState(ChargeTransactionState.CONSUMED);
        ChargeEntity chargeEntityResp = this.chargeRepository.save(chargeEntity);

        return ResponseEntity.ok(new ApiResponse<>(this.objectMapper.convertValue(chargeEntityResp, ChargeDto.class)));
    }

    @Transactional
    @Override
    public ResponseEntity<ApiResponse<ChargeEntity>> cancelCharge(String tokenValue, int id) {

        // 👤 Récupération de l'ID de l'utilisateur connecté
        int currentUserId = this.currentUserService.getUserId(tokenValue);

        // 🔍 Récupération de la charge correspondante à l'ID
        ChargeEntity chargeEntity = this.chargeRepository.findById(id).orElse(null);

        // 🧱 Vérification : existe-t-elle ?
        if (chargeEntity == null) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(new Exception("❌ No charge found for the provided ID.")));
        }

        // 🔗 Récupération de la transaction originale associée à cette charge
        Optional<TransactionEntity> originalTransactionEntityOptional =
                this.transactionService.findById(chargeEntity.getIdTransaction());
        TransactionEntity originalTransactionEntity = originalTransactionEntityOptional.orElse(null);

        // ⚠️ Vérification : transaction originale trouvée ?
        if (originalTransactionEntityOptional.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(new Exception("⚠️ No original transaction found for this charge.")));
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

        // 🧩 Vérification : état de la charge avant annulation
        if (chargeEntity.getIdState() != ChargeTransactionState.CONSUMED) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(new Exception("⚠️ The charge must be in 'CONSUMED' state to be canceled.")));
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

            // 💰 Une charge réduit normalement la trésorerie et augmente les dépenses
            // En annulant la charge, on fait donc l'inverse :
            BigDecimal newProfitNet = lastTransactionEntity.getCurrentProfitNet().subtract(transactionAmount);
            BigDecimal newTotalExpenses = lastTransactionEntity.getTotalExpenses().add(transactionAmount);
            BigDecimal newCashRegisterBalance = lastTransactionEntity.getCashRegisterBalance().subtract(transactionAmount);

            // ⚙️ Préparation de la nouvelle transaction (annulation)
            newTransactionEntity.setId(null);
            newTransactionEntity.setAmount(transactionAmount);
            newTransactionEntity.setAddingDate(Date.valueOf(LocalDate.now()));
            newTransactionEntity.setAddingTime(Time.valueOf(LocalTime.now()));
            newTransactionEntity.setTransactionDate(newTransactionEntity.getAddingDate());
            newTransactionEntity.setTransactionTime(newTransactionEntity.getAddingTime());
            newTransactionEntity.setIdTransactionType(TransactionType.CANCEL_CHAR);
            newTransactionEntity.setIdPeriod(lastPeriodEntity.getId());
            newTransactionEntity.setExecutedBy(currentUserId);
            newTransactionEntity.setOriginalTransactionId(originalTransactionEntity.getId());

            // 🔄 Synchronisation des totaux avec la dernière transaction
            newTransactionEntity.setCurrentCapital(lastTransactionEntity.getCurrentCapital());
            newTransactionEntity.setCurrentProfitGross(lastTransactionEntity.getCurrentProfitGross());
            newTransactionEntity.setCurrentProfitNet(newProfitNet);
            newTransactionEntity.setTotalExpenses(newTotalExpenses);
            newTransactionEntity.setTotalCustomerCredit(lastTransactionEntity.getTotalCustomerCredit());
            newTransactionEntity.setTotalExternalLoan(lastTransactionEntity.getTotalExternalLoan());
            newTransactionEntity.setTotalAdvance(lastTransactionEntity.getTotalAdvance());
            newTransactionEntity.setTotalConsumableInputs(lastTransactionEntity.getTotalConsumableInputs());
            newTransactionEntity.setTotalNonConsumableInputs(lastTransactionEntity.getTotalNonConsumableInputs());
            newTransactionEntity.setCashRegisterBalance(newCashRegisterBalance);
            newTransactionEntity.setTotalOutOfPocketExpenses(lastTransactionEntity.getTotalOutOfPocketExpenses());

            // 💾 Sauvegarde de la transaction d’annulation
            TransactionEntity transactionEntityResp = this.transactionService.save(newTransactionEntity);

            // 🏁 Mise à jour de l’état de la charge
            chargeEntity.setIdState(ChargeTransactionState.CANCELED);

            // ✅ Réponse de succès
            return ResponseEntity.ok(new ApiResponse<>(this.chargeRepository.save(chargeEntity)));
        }
        catch (Exception e) {
            // ❗ Gestion centralisée des erreurs inattendues
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            log.error("Error while canceling charge", e);

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(new Exception("💥 An unexpected error occurred while canceling the charge.")));
        }
    }
}
