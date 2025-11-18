package com.idApps.storeCashFlowApi.persistence.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.idApps.storeCashFlowApi.help.constants.CustomerCreditState;
import com.idApps.storeCashFlowApi.help.constants.FinancialPeriodState;
import com.idApps.storeCashFlowApi.help.constants.TransactionType;
import com.idApps.storeCashFlowApi.models.dto.CustomerCreditDto;
import com.idApps.storeCashFlowApi.models.response.ApiResponse;
import com.idApps.storeCashFlowApi.persistence.entity.CustomerCreditEntity;
import com.idApps.storeCashFlowApi.persistence.entity.FinancialPeriodEntity;
import com.idApps.storeCashFlowApi.persistence.entity.TransactionEntity;
import com.idApps.storeCashFlowApi.persistence.repository.CustomerCreditRepository;
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
public class CustomerCreditServiceImpl implements CustomerCreditService {
    private final ObjectMapper objectMapper;
    private final CurrentUserService currentUserService;
    private final CustomerCreditRepository customerCreditRepository;
    private final TransactionService transactionService;
    private final FinancialPeriodService financialPeriodService;

    @Override
    public List<CustomerCreditDto> getCustomerCredits(Date creditDateMin, Date creditDateMax, Integer stateId, Integer periodId, Integer customerId) {
        return this.customerCreditRepository.getCustomerCredits(creditDateMin, creditDateMax, stateId, periodId, customerId)
                .stream().map(item -> this.objectMapper.convertValue(item, CustomerCreditDto.class)).toList();
    }

    @Transactional
    @Override
    public ResponseEntity<ApiResponse<CustomerCreditEntity>> addCustomerCredit(CustomerCreditDto customerCreditDto, String tokenValue) {
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

        TransactionEntity transactionEntity = this.objectMapper.convertValue(customerCreditDto, TransactionEntity.class);
        transactionEntity.setTransactionDate(customerCreditDto.getCreditDate());
        transactionEntity.setTransactionTime(customerCreditDto.getCreditTime());
        transactionEntity.setAddingDate(Date.valueOf(LocalDate.now()));
        transactionEntity.setAddingTime(Time.valueOf(LocalTime.now()));
        transactionEntity.setIdTransactionType(TransactionType.CREATE_CUSTOMER_CREDIT);
        transactionEntity.setIdPeriod(lastPeriodEntity.getId());
        transactionEntity.setAmount(customerCreditDto.getInitialAmount());
        transactionEntity.setExecutedBy(currentUserId);

        TransactionEntity lastSavedTransaction = this.transactionService.getLastTransaction();
        BigDecimal newTotalCustomerCredit = lastSavedTransaction.getTotalCustomerCredit().add(customerCreditDto.getInitialAmount());
        BigDecimal newCashRegisterBalance = lastSavedTransaction.getCashRegisterBalance().subtract(customerCreditDto.getInitialAmount());
        transactionEntity.setCurrentCapital(lastSavedTransaction.getCurrentCapital());
        transactionEntity.setCurrentProfitGross(lastSavedTransaction.getCurrentProfitGross());
        transactionEntity.setCurrentProfitNet(lastSavedTransaction.getCurrentProfitNet());
        transactionEntity.setTotalExpenses(lastSavedTransaction.getTotalExpenses());
        transactionEntity.setTotalCustomerCredit(newTotalCustomerCredit);
        transactionEntity.setTotalExternalLoan(lastSavedTransaction.getTotalExternalLoan());
        transactionEntity.setTotalAdvance(lastSavedTransaction.getTotalAdvance());
        transactionEntity.setTotalConsumableInputs(lastSavedTransaction.getTotalConsumableInputs());
        transactionEntity.setTotalNonConsumableInputs(lastSavedTransaction.getTotalNonConsumableInputs());
        transactionEntity.setCashRegisterBalance(newCashRegisterBalance);
        transactionEntity.setTotalOutOfPocketExpenses(lastSavedTransaction.getTotalOutOfPocketExpenses());
        TransactionEntity transactionEntityResp = this.transactionService.save(transactionEntity);

        CustomerCreditEntity customerCreditEntity = this.objectMapper.convertValue(customerCreditDto, CustomerCreditEntity.class);
        customerCreditEntity.setIdTransaction(transactionEntityResp.getId());
        customerCreditEntity.setPaidAmount(BigDecimal.valueOf(0));
        customerCreditEntity.setRemainingAmount(customerCreditDto.getInitialAmount());
        customerCreditEntity.setStateId(CustomerCreditState.CREDITED);
        return ResponseEntity.ok(new ApiResponse(this.customerCreditRepository.save(customerCreditEntity)));
    }

    @Transactional
    @Override
    public ResponseEntity<ApiResponse<CustomerCreditEntity>> cancelCustomerCredit(String tokenValue, int id) {

        // 👤 Récupération de l'ID de l'utilisateur connecté
        int currentUserId = this.currentUserService.getUserId(tokenValue);

        // 🔍 Récupération du crédit client à annuler
        CustomerCreditEntity customerCreditEntity = this.customerCreditRepository.findById(id).orElse(null);

        // ⚠️ Vérification : crédit client trouvé ?
        if (customerCreditEntity == null) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(new Exception("❌ No customer credit found for the provided ID.")));
        }

        // 🔗 Récupération de la transaction originale associée à ce crédit
        Optional<TransactionEntity> originalTransactionEntityOptional =
                this.transactionService.findById(customerCreditEntity.getIdTransaction());
        TransactionEntity originalTransactionEntity = originalTransactionEntityOptional.orElse(null);

        // ⚠️ Vérification : transaction originale trouvée ?
        if (originalTransactionEntityOptional.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(new Exception("⚠️ No original transaction found for this customer credit.")));
        }

        // 📅 Récupération de la dernière période financière
        FinancialPeriodEntity lastPeriodEntity = this.financialPeriodService.getLastPeriod().orElse(null);

        // ⚠️ Vérification : période existante ?
        if (lastPeriodEntity == null) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(new Exception("⚠️ No financial period found in the system.")));
        }

        // 📊 Vérification : la transaction appartient-elle à la dernière période ?
        if (!originalTransactionEntity.getIdPeriod().equals(lastPeriodEntity.getId())) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(new Exception("🚫 The transaction belongs to a different financial period.")));
        }

        // 🔒 Vérification : période clôturée ?
        if (lastPeriodEntity.getStateId() == FinancialPeriodState.CLOSED) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(new Exception("🔒 The financial period has already been closed.")));
        }

        // ❌ Vérification : période annulée ?
        if (lastPeriodEntity.getStateId() == FinancialPeriodState.CANCELED) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(new Exception("🚫 The financial period has been canceled — no transactions can be added.")));
        }

        // 🧩 Vérification : état du crédit client avant annulation
        if (customerCreditEntity.getStateId() == CustomerCreditState.CANCELED) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(new Exception("⚠️ The customer credit must not be canceled to be canceled.")));
        }

        try {
            // 📜 Récupération de la dernière transaction
            TransactionEntity lastTransactionEntity = this.transactionService.getLastTransaction();
            if (lastTransactionEntity == null) {
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(new ApiResponse<>(new Exception("⚠️ No previous transaction found in the system.")));
            }

            // 🧬 Copie de la transaction originale
            TransactionEntity newTransactionEntity = this.objectMapper.convertValue(originalTransactionEntity, TransactionEntity.class);

            // ➕ Inversion du montant
            BigDecimal transactionAmount = originalTransactionEntity.getAmount().multiply(BigDecimal.valueOf(-1));

            // 💰 Un crédit client augmente normalement le total des crédits et réduit la trésorerie.
            // En annulant ce crédit, on fait donc l'inverse :
            BigDecimal newTotalCustomerCredit = lastTransactionEntity.getTotalCustomerCredit().add(transactionAmount);
            BigDecimal newCashRegisterBalance = lastTransactionEntity.getCashRegisterBalance().subtract(transactionAmount);

            // ⚙️ Configuration de la nouvelle transaction (annulation)
            newTransactionEntity.setId(null);
            newTransactionEntity.setAmount(transactionAmount);
            newTransactionEntity.setAddingDate(Date.valueOf(LocalDate.now()));
            newTransactionEntity.setAddingTime(Time.valueOf(LocalTime.now()));
            newTransactionEntity.setTransactionDate(newTransactionEntity.getAddingDate());
            newTransactionEntity.setTransactionTime(newTransactionEntity.getAddingTime());
            newTransactionEntity.setIdTransactionType(TransactionType.CANCEL_CUSTOMER_CREDIT);
            newTransactionEntity.setIdPeriod(lastPeriodEntity.getId());
            newTransactionEntity.setExecutedBy(currentUserId);
            newTransactionEntity.setOriginalTransactionId(originalTransactionEntity.getId());

            // 🔄 Synchronisation des totaux avec la dernière transaction
            newTransactionEntity.setCurrentCapital(lastTransactionEntity.getCurrentCapital());
            newTransactionEntity.setCurrentProfitGross(lastTransactionEntity.getCurrentProfitGross());
            newTransactionEntity.setCurrentProfitNet(lastTransactionEntity.getCurrentProfitNet());
            newTransactionEntity.setTotalExpenses(lastTransactionEntity.getTotalExpenses());
            newTransactionEntity.setTotalCustomerCredit(newTotalCustomerCredit);
            newTransactionEntity.setTotalExternalLoan(lastTransactionEntity.getTotalExternalLoan());
            newTransactionEntity.setTotalAdvance(lastTransactionEntity.getTotalAdvance());
            newTransactionEntity.setTotalConsumableInputs(lastTransactionEntity.getTotalConsumableInputs());
            newTransactionEntity.setTotalNonConsumableInputs(lastTransactionEntity.getTotalNonConsumableInputs());
            newTransactionEntity.setCashRegisterBalance(newCashRegisterBalance);
            newTransactionEntity.setTotalOutOfPocketExpenses(lastTransactionEntity.getTotalOutOfPocketExpenses());

            // 💾 Sauvegarde de la transaction d’annulation
            TransactionEntity transactionEntityResp = this.transactionService.save(newTransactionEntity);

            // 🏁 Mise à jour de l’état du crédit client
            customerCreditEntity.setStateId(CustomerCreditState.CANCELED);

            // ✅ Réponse finale
            return ResponseEntity.ok(new ApiResponse<>(this.customerCreditRepository.save(customerCreditEntity)));
        }
        catch (Exception e) {
            // ❗ Gestion centralisée des exceptions
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            log.error("Error while canceling customer credit", e);

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(new Exception("💥 An unexpected error occurred while canceling the customer credit.")));
        }
    }

}
