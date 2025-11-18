package com.idApps.storeCashFlowApi.persistence.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.idApps.storeCashFlowApi.help.constants.FinancialPeriodState;
import com.idApps.storeCashFlowApi.help.constants.ProductTransactionState;
import com.idApps.storeCashFlowApi.help.constants.TransactionType;
import com.idApps.storeCashFlowApi.models.dto.ConsInputDto;
import com.idApps.storeCashFlowApi.models.response.ApiResponse;
import com.idApps.storeCashFlowApi.persistence.entity.ConsInputEntity;
import com.idApps.storeCashFlowApi.persistence.entity.FinancialPeriodEntity;
import com.idApps.storeCashFlowApi.persistence.entity.InputEntity;
import com.idApps.storeCashFlowApi.persistence.entity.TransactionEntity;
import com.idApps.storeCashFlowApi.persistence.repository.ConsumableInputRepository;
import com.idApps.storeCashFlowApi.persistence.repository.InputRepository;
import com.idApps.storeCashFlowApi.persistence.repository.TransactionRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
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
import java.util.*;

@Slf4j
@Data
@Service
public class ConsumableInputServiceImpl implements ConsumableInputService {

    final private ConsumableInputRepository consumableInputRepository;
    final private InputService inputService;
    final private TransactionService transactionService;
    final private CurrentUserService currentUserService;
    final private FinancialPeriodService financialPeriodService;
    final private ObjectMapper objectMapper;

    @PersistenceContext
    private EntityManager entityManager;

    public List<ConsInputDto> getConsInputs(Integer productId, Integer transactionTypeId, Integer transactionStateId, Integer periodId, Integer executedById, LocalDate transactionDateMin, LocalDate transactionDateMax) {
        List<Map<String, Object>> resultMapList = this.consumableInputRepository.getConsInputs(productId, transactionTypeId, transactionStateId, periodId, executedById, transactionDateMin, transactionDateMax);
        return resultMapList.stream().map(item -> this.objectMapper.convertValue(item, ConsInputDto.class)).toList();
    }

    @Override
    public ConsInputDto getConsInputById(Integer consInputId, List<String> fields) {
        Collections.replaceAll(fields, "imageSrc", "image_src");
        String selectedFields = String.join(", ", fields);
        String sql = String.format("""
                SELECT %s FROM consumable_input
                LEFT JOIN input ON consumable_input.id_input=input.id
                LEFT JOIN transaction on input.id_transaction=transaction.id
                WHERE consumable_input.id=:id;
                """, selectedFields);
        Query query = this.entityManager.createNativeQuery(sql, Map.class);
        query.setParameter("id", consInputId);
        return this.objectMapper.convertValue(query.getSingleResult(), ConsInputDto.class);
    }

    @Override
    public Optional<ConsInputEntity> findById(Integer consInputId) {
        return this.consumableInputRepository.findById(consInputId);
    }

    @Override
    public List<ConsInputDto> searchConsInputs(String consInputLabel) {
        List<Map<String, Object>> resultMapList = this.consumableInputRepository.searchConsInputs(consInputLabel);
        return resultMapList.stream().map(map -> this.objectMapper.convertValue(map, ConsInputDto.class)).toList();
    }

    @Transactional
    @Override
    public ResponseEntity<ApiResponse<ConsInputDto>> addConsInput(ConsInputDto constInputDto, String userAccessToken) {
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

        BigDecimal total = constInputDto.getCost().multiply(BigDecimal.valueOf(constInputDto.getInitialQuantity()));
        Date addingDate = Date.valueOf(LocalDate.now());
        Time addingTime = Time.valueOf(LocalTime.now());

        TransactionEntity transactionEntity = this.objectMapper.convertValue(constInputDto, TransactionEntity.class);
        transactionEntity.setAmount(total);
        transactionEntity.setAddingDate(addingDate);
        transactionEntity.setAddingTime(addingTime);
        transactionEntity.setExecutedBy(currentUserId);
        transactionEntity.setIdTransactionType(TransactionType.CREATE_CONS_INP);
        transactionEntity.setIdPeriod(lastPeriodEntity.getId());

        TransactionEntity lastSavedTransaction = this.transactionService.getLastTransaction();
        BigDecimal newTotalConsInuptsGross = lastSavedTransaction.getTotalConsumableInputs().add(total);
        BigDecimal newCashRegisterBalance = lastSavedTransaction.getCashRegisterBalance().subtract(total);

        transactionEntity.setCurrentCapital(lastSavedTransaction.getCurrentCapital());
        transactionEntity.setCurrentProfitGross(lastSavedTransaction.getCurrentProfitGross());
        transactionEntity.setCurrentProfitNet(lastSavedTransaction.getCurrentProfitNet());
        transactionEntity.setTotalExpenses(lastSavedTransaction.getTotalExpenses());
        transactionEntity.setTotalCustomerCredit(lastSavedTransaction.getTotalCustomerCredit());
        transactionEntity.setTotalExternalLoan(lastSavedTransaction.getTotalExternalLoan());
        transactionEntity.setTotalAdvance(lastSavedTransaction.getTotalAdvance());
        transactionEntity.setTotalConsumableInputs(newTotalConsInuptsGross);
        transactionEntity.setTotalNonConsumableInputs(lastSavedTransaction.getTotalNonConsumableInputs());
        transactionEntity.setCashRegisterBalance(newCashRegisterBalance);
        transactionEntity.setTotalOutOfPocketExpenses(lastSavedTransaction.getTotalOutOfPocketExpenses());
        TransactionEntity transactionEntityResp = this.transactionService.save(transactionEntity);
        System.out.println("transaction entity: " + transactionEntity);

        InputEntity inputEntity = this.objectMapper.convertValue(constInputDto, InputEntity.class);
        inputEntity.setTotal(total);
        inputEntity.setIdTransaction(transactionEntityResp.getId());
        inputEntity.setRemainingQuantity(constInputDto.getInitialQuantity());
        InputEntity inputEntityResp = this.inputService.save(inputEntity);
        System.out.println("input entity: " + inputEntity);

        ConsInputEntity consInputEntity = this.objectMapper.convertValue(constInputDto, ConsInputEntity.class);
        consInputEntity.setIdInput(inputEntityResp.getId());
        consInputEntity.setIdState(ProductTransactionState.AVAILABLE);
        System.out.println("consInputEntity: " + consInputEntity);
        ConsInputEntity consInputEntityResp = this.consumableInputRepository.save(consInputEntity);

        return ResponseEntity.ok(new ApiResponse(this.objectMapper.convertValue(consInputEntityResp, ConsInputDto.class)));
    }

    @Override
    public ConsInputEntity save(ConsInputEntity consInputEntity) {
        return this.consumableInputRepository.save(consInputEntity);
    }

    @Override
    public ResponseEntity<ApiResponse<List<ConsInputEntity>>> addConsInputList(List<ConsInputDto> consInputDtoList, String userAccessToken) {
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

        Date addingDate = Date.valueOf(LocalDate.now());
        Time addingTime = Time.valueOf(LocalTime.now());

        TransactionEntity lastSavedTransaction = this.transactionService.getLastTransaction();
        TransactionEntity previousTransaction = lastSavedTransaction;
        List<TransactionEntity> transactionEntityList = new ArrayList<>();
        List<InputEntity> inputEntityList = new ArrayList<>();
        List<ConsInputEntity> consInputEntityList = new ArrayList<>();
        for(ConsInputDto consInputDto: consInputDtoList) {
            BigDecimal total = consInputDto.getCost().multiply(BigDecimal.valueOf(consInputDto.getInitialQuantity()));

            TransactionEntity transactionEntity = this.objectMapper.convertValue(consInputDto, TransactionEntity.class);
            transactionEntity.setAmount(total);
            transactionEntity.setIdPeriod(lastPeriodEntity.getId());
            transactionEntity.setAddingDate(addingDate);
            transactionEntity.setAddingTime(addingTime);
            transactionEntity.setExecutedBy(currentUserId);
            transactionEntity.setIdTransactionType(TransactionType.CREATE_CONS_INP);

            BigDecimal newTotalConsInputsGross = previousTransaction.getTotalConsumableInputs().add(total);
            BigDecimal newCashRegisterBalance = previousTransaction.getCashRegisterBalance().subtract(total);
            transactionEntity.setCurrentCapital(previousTransaction.getCurrentCapital());
            transactionEntity.setCurrentProfitGross(previousTransaction.getCurrentProfitGross());
            transactionEntity.setCurrentProfitNet(previousTransaction.getCurrentProfitNet());
            transactionEntity.setTotalExpenses(previousTransaction.getTotalExpenses());
            transactionEntity.setTotalCustomerCredit(previousTransaction.getTotalCustomerCredit());
            transactionEntity.setTotalExternalLoan(previousTransaction.getTotalExternalLoan());
            transactionEntity.setTotalAdvance(previousTransaction.getTotalAdvance());
            transactionEntity.setTotalConsumableInputs(newTotalConsInputsGross);
            transactionEntity.setTotalNonConsumableInputs(previousTransaction.getTotalNonConsumableInputs());
            transactionEntity.setCashRegisterBalance(newCashRegisterBalance);
            transactionEntity.setTotalOutOfPocketExpenses(previousTransaction.getTotalOutOfPocketExpenses());
            transactionEntityList.add(transactionEntity);
            previousTransaction = transactionEntity;

            InputEntity inputEntity = this.objectMapper.convertValue(consInputDto, InputEntity.class);
            inputEntity.setTotal(total);
            inputEntity.setRemainingQuantity(consInputDto.getInitialQuantity());
            inputEntityList.add(inputEntity);

            ConsInputEntity consInputEntity = this.objectMapper.convertValue(consInputDto, ConsInputEntity.class);
            consInputEntity.setIdState(ProductTransactionState.AVAILABLE);
            consInputEntityList.add(consInputEntity);
        }


        List<TransactionEntity> transactionEntityRespList = this.transactionService.saveAll(transactionEntityList);
        System.out.println("transaction entity list: " + transactionEntityList);

        for(int i = 0; i<transactionEntityList.size(); i++) {
            TransactionEntity transactionEntityResp = transactionEntityRespList.get(i);
            InputEntity inputEntity = inputEntityList.get(i);
            inputEntity.setIdTransaction(transactionEntityResp.getId());
        }
        List<InputEntity> inputEntityListResp = this.inputService.saveAll(inputEntityList);
        System.out.println("input entity list: " + inputEntityList);

        for(int i = 0; i<transactionEntityList.size(); i++) {
            InputEntity inputEntityResp = inputEntityListResp.get(i);
            ConsInputEntity consInputEntity = consInputEntityList.get(i);
            consInputEntity.setIdInput(inputEntityResp.getId());
        }
        System.out.println("consInputEntityList: " + consInputEntityList);

        List<ConsInputEntity> consInputEntityListResp = this.consumableInputRepository.saveAll(consInputEntityList);

        return ResponseEntity.ok(new ApiResponse<>(consInputEntityListResp));
    }

    @Transactional
    @Override
    public ResponseEntity<ApiResponse<ConsInputEntity>> cancelConsInput(String tokenValue, int id) {

        // 👤 Récupération de l'utilisateur actuel
        int currentUserId = this.currentUserService.getUserId(tokenValue);

        // 🔍 Récupération de l'input consommable à annuler
        ConsInputEntity consInputEntity = this.consumableInputRepository.findById(id).orElse(null);
        InputEntity inputEntity = this.inputService.findById(consInputEntity.getIdInput()).orElse(null);

        // ⚠️ Vérification : existe-t-il ?
        if (consInputEntity == null) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(new Exception("❌ No consumable input found for the provided ID.")));
        }

        if(inputEntity == null) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(new Exception("❌ No input found for the provided ID.")));
        }

        // 🔗 Récupération de la transaction originale associée
        Optional<TransactionEntity> originalTransactionEntityOptional =
                this.transactionService.findById(inputEntity.getIdTransaction());
        TransactionEntity originalTransactionEntity = originalTransactionEntityOptional.orElse(null);

        // ⚠️ Vérification : transaction trouvée ?
        if (originalTransactionEntityOptional.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(new Exception("⚠️ No original transaction found for this consumable input.")));
        }

        // 📅 Récupération de la dernière période financière
        FinancialPeriodEntity lastPeriodEntity = this.financialPeriodService.getLastPeriod().orElse(null);

        // ⚠️ Vérification : période trouvée ?
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

        // 🧩 Vérification : état de l’input consommable
        if (consInputEntity.getIdState() != ProductTransactionState.AVAILABLE) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(new Exception("⚠️ The consumable input must be in available to be canceled.")));
        }

        try {
            // 📜 Récupération de la dernière transaction enregistrée
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

            // 🧮 Calcul des nouveaux totaux :
            // Un input consommable réduit normalement la trésorerie et augmente les inputs consommés,
            // donc l’annulation fera l’inverse.
            BigDecimal newTotalConsInuptsGross = lastTransactionEntity.getTotalConsumableInputs().add(transactionAmount);
            BigDecimal newCashRegisterBalance = lastTransactionEntity.getCashRegisterBalance().subtract(transactionAmount);

            // ⚙️ Configuration de la nouvelle transaction d’annulation
            newTransactionEntity.setId(null);
            newTransactionEntity.setAmount(transactionAmount);
            newTransactionEntity.setAddingDate(Date.valueOf(LocalDate.now()));
            newTransactionEntity.setAddingTime(Time.valueOf(LocalTime.now()));
            newTransactionEntity.setTransactionDate(newTransactionEntity.getAddingDate());
            newTransactionEntity.setTransactionTime(newTransactionEntity.getAddingTime());
            newTransactionEntity.setIdTransactionType(TransactionType.CANCEL_CONS_INP);
            newTransactionEntity.setIdPeriod(lastPeriodEntity.getId());
            newTransactionEntity.setExecutedBy(currentUserId);
            newTransactionEntity.setOriginalTransactionId(originalTransactionEntity.getId());

            // 🔄 Mise à jour des totaux
            newTransactionEntity.setCurrentCapital(lastTransactionEntity.getCurrentCapital());
            newTransactionEntity.setCurrentProfitGross(lastTransactionEntity.getCurrentProfitGross());
            newTransactionEntity.setCurrentProfitNet(lastTransactionEntity.getCurrentProfitNet());
            newTransactionEntity.setTotalExpenses(lastTransactionEntity.getTotalExpenses());
            newTransactionEntity.setTotalCustomerCredit(lastTransactionEntity.getTotalCustomerCredit());
            newTransactionEntity.setTotalExternalLoan(lastTransactionEntity.getTotalExternalLoan());
            newTransactionEntity.setTotalAdvance(lastTransactionEntity.getTotalAdvance());
            newTransactionEntity.setTotalConsumableInputs(newTotalConsInuptsGross);
            newTransactionEntity.setTotalNonConsumableInputs(lastTransactionEntity.getTotalNonConsumableInputs());
            newTransactionEntity.setCashRegisterBalance(newCashRegisterBalance);
            newTransactionEntity.setTotalOutOfPocketExpenses(lastTransactionEntity.getTotalOutOfPocketExpenses());

            // 💾 Sauvegarde de la nouvelle transaction
            TransactionEntity transactionEntityResp = this.transactionService.save(newTransactionEntity);

            // 🏁 Mise à jour de l’état de l’input consommable
            consInputEntity.setIdState(ProductTransactionState.CANCELED);

            // ✅ Réponse finale de succès
            return ResponseEntity.ok(new ApiResponse<>(this.consumableInputRepository.save(consInputEntity)));
        }
        catch (Exception e) {
            // ❗ Gestion des exceptions inattendues
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            log.error("Error while canceling consumable input", e);

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(new Exception("💥 An unexpected error occurred while canceling the consumable input.")));
        }
    }

}
