package com.idApps.storeCashFlowApi.persistence.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.idApps.storeCashFlowApi.help.constants.FinancialPeriodState;
import com.idApps.storeCashFlowApi.help.constants.ProductTransactionState;
import com.idApps.storeCashFlowApi.help.constants.TransactionType;
import com.idApps.storeCashFlowApi.models.dto.ConsInputDto;
import com.idApps.storeCashFlowApi.models.dto.OutputDto;
import com.idApps.storeCashFlowApi.models.response.ApiResponse;
import com.idApps.storeCashFlowApi.persistence.entity.*;
import com.idApps.storeCashFlowApi.persistence.repository.OutputRepository;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Data
@Service
public class OutputServiceImpl implements OutputService {

    private final OutputRepository outputRepository;
    private final TransactionService transactionService;
    private final CurrentUserService currentUserService;
    private final InputService inputService;
    private final ConsumableInputService consumableInputService;
    private final ObjectMapper objectMapper;
    private final FinancialPeriodService financialPeriodService;

    @Override
    public List<OutputDto> getOutputs(Integer productId, Integer transactionTypeId, Integer transactionStateId, Integer periodId,
                                      Integer soldById, LocalDate transactionDateMin, LocalDate transactionDateMax) {
        List<Map<String, Object>> outputMapList = this.outputRepository.getOutputs(productId, transactionTypeId, transactionStateId, periodId, soldById, transactionDateMin, transactionDateMax);
        outputMapList.forEach(item -> System.out.println(item.values()));
        return outputMapList.stream().map(map -> objectMapper.convertValue(map, OutputDto.class)).toList();
    }

    @Transactional
    @Override
    public ResponseEntity<ApiResponse<OutputEntity>> addOutput(OutputDto outputDto, String userAccessToken) {
        ConsInputDto consInputDto = this.consumableInputService.getConsInputById(outputDto.getIdConsInput(), new ArrayList<>(List.of("consumable_input.id", "imageSrc", "cost", "remaining_quantity")));
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

        TransactionEntity transactionEntity = this.objectMapper.convertValue(outputDto, TransactionEntity.class);
        BigDecimal totalPrice = outputDto.getUnitPrice().multiply(BigDecimal.valueOf(outputDto.getQuantity()));
        BigDecimal totalCost = consInputDto.getCost().multiply(BigDecimal.valueOf(outputDto.getQuantity()));
        BigDecimal totalProfit = totalPrice.subtract(totalCost);

        //L'ajout de la ligne de la nouvelle transaction
        transactionEntity.setAmount(totalPrice);
        transactionEntity.setAddingDate(Date.valueOf(LocalDate.now()));
        transactionEntity.setAddingTime(Time.valueOf(LocalTime.now()));
        transactionEntity.setImageSrc(consInputDto.getImageSrc());
        transactionEntity.setIdTransactionType(TransactionType.CREATE_OUT);
        transactionEntity.setIdPeriod(lastPeriodEntity.getId());
        transactionEntity.setExecutedBy(currentUserId);

        //Les valeurs de la nouvelle transaction qui sont déduits de la dernière transaction
        TransactionEntity lastSavedTransaction = this.transactionService.getLastTransaction();
        BigDecimal newTotalProfitGross = lastSavedTransaction.getCurrentProfitGross().add(totalProfit);
        BigDecimal newTotalProfitNet = lastSavedTransaction.getCurrentProfitNet().add(totalProfit);
        BigDecimal newCashRegisterBalance = lastSavedTransaction.getCashRegisterBalance().add(totalPrice);
        BigDecimal newTotalConsumableInputs = lastSavedTransaction.getTotalConsumableInputs().subtract(totalCost);
        transactionEntity.setCurrentCapital(lastSavedTransaction.getCurrentCapital());
        transactionEntity.setCurrentProfitGross(newTotalProfitGross);
        transactionEntity.setCurrentProfitNet(newTotalProfitNet);
        transactionEntity.setTotalExpenses(lastSavedTransaction.getTotalExpenses());
        transactionEntity.setTotalCustomerCredit(lastSavedTransaction.getTotalCustomerCredit());
        transactionEntity.setTotalExternalLoan(lastSavedTransaction.getTotalExternalLoan());
        transactionEntity.setTotalAdvance(lastSavedTransaction.getTotalAdvance());
        transactionEntity.setTotalConsumableInputs(newTotalConsumableInputs);
        transactionEntity.setTotalNonConsumableInputs(lastSavedTransaction.getTotalNonConsumableInputs());
        transactionEntity.setCashRegisterBalance(newCashRegisterBalance);
        transactionEntity.setTotalOutOfPocketExpenses(lastSavedTransaction.getTotalOutOfPocketExpenses());
        TransactionEntity transactionEntityRest = this.transactionService.save(transactionEntity);

        //La modification de l'input correspondant.
        switch (consInputDto.getRemainingQuantity().compareTo(outputDto.getQuantity())) {
            case -1:
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    new ApiResponse<>(new Exception("Selected quantity is higher than remaining quantity"))
                );
            default:
                ConsInputEntity modifiedConsInputEntity = this.consumableInputService.findById(consInputDto.getId()).get();

                //Input modification
                InputEntity modifiedInputEntity = this.inputService.findById(modifiedConsInputEntity.getIdInput()).get();
                Integer newRemainingQuantity = modifiedInputEntity.getRemainingQuantity() - outputDto.getQuantity();
                modifiedInputEntity.setRemainingQuantity(newRemainingQuantity);

                //Const input line modification.
                Integer inputStateId = newRemainingQuantity == 0? ProductTransactionState.SOLD: ProductTransactionState.AVAILABLE;
                modifiedConsInputEntity.setIdState(inputStateId);
                this.consumableInputService.save(modifiedConsInputEntity);
                break;
        }

        //L'ajout de la ligne de la nouvelle output.
        OutputEntity outputEntity = this.objectMapper.convertValue(outputDto, OutputEntity.class);
        outputEntity.setUnitCost(consInputDto.getCost());
        outputEntity.setTotalCost(totalCost);
        outputEntity.setTotalPrice(totalPrice);
        outputEntity.setUnitProfit(outputEntity.getUnitPrice().subtract(outputEntity.getUnitCost()));
        outputEntity.setTotalProfit(totalProfit);
        outputEntity.setIdPeriod(lastPeriodEntity.getId());
        outputEntity.setIdTransaction(transactionEntityRest.getId());
        OutputEntity outputEntityResp = this.outputRepository.save(outputEntity);
        return ResponseEntity.ok(new ApiResponse<>(outputEntityResp));
    }

    @Transactional
    @Override
    public ResponseEntity<ApiResponse<OutputEntity>> cancelOutput(String tokenValue, int id) {

        // 👤 1️⃣ Récupération de l'utilisateur connecté
        int currentUserId = this.currentUserService.getUserId(tokenValue);

        // 🔍 2️⃣ Récupération du output correspondant à l'ID
        OutputEntity outputEntity = this.outputRepository.findById(id).orElse(null);
        ConsInputEntity consInputEntity = this.consumableInputService.findById(outputEntity.getIdConsInput()).orElse(null);
        InputEntity inputEntity = this.inputService.findById(consInputEntity.getIdInput()).orElse(null);

        // 🧱 3️⃣ Vérification : existe-t-il ?
        if (outputEntity == null) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(new Exception("❌ No output found for the provided ID.")));
        }

        // 🔗 4️⃣ Récupération de la transaction originale associée
        Optional<TransactionEntity> originalTransactionEntityOptional =
                this.transactionService.findById(outputEntity.getIdTransaction());
        TransactionEntity originalTransactionEntity = originalTransactionEntityOptional.orElse(null);

        // ⚠️ Vérification : transaction originale trouvée ?
        if (originalTransactionEntityOptional.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(new Exception("⚠️ No original transaction found for this output.")));
        }

        // 📅 5️⃣ Récupération de la dernière période financière
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

        // ❌ Vérification : la période est-elle annulée ?
        if (lastPeriodEntity.getStateId() == FinancialPeriodState.CANCELED) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(new Exception("🚫 The financial period has been canceled — no transactions can be added.")));
        }

        // 🧩 Vérification : état de la sortie avant annulation
        if (outputEntity.getIdState() != ProductTransactionState.SOLD) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(new Exception("⚠️ The output must be in 'SOLD' state to be canceled.")));
        }

        try {
            // 📜 6️⃣ Récupération de la dernière transaction du système
            TransactionEntity lastTransactionEntity = this.transactionService.getLastTransaction();
            if (lastTransactionEntity == null) {
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(new ApiResponse<>(new Exception("⚠️ No previous transaction found in the system.")));
            }

            // 🧬 7️⃣ Copie de la transaction originale pour créer la transaction d’annulation
            TransactionEntity newTransactionEntity = this.objectMapper.convertValue(originalTransactionEntity, TransactionEntity.class);

            // 💰 8️⃣ Calcul du montant inversé
            BigDecimal totalPrice = originalTransactionEntity.getAmount().multiply(BigDecimal.valueOf(-1));
            BigDecimal totalCost = inputEntity.getCost().multiply(BigDecimal.valueOf(outputEntity.getQuantity()))
                                                .multiply(BigDecimal.valueOf(-1));
            BigDecimal totalProfit = totalPrice.subtract(totalCost);
            // 📈 Logique comptable :
            // - Une vente (output) augmente capital, trésorerie et bénéfices
            // - Son annulation les diminue

            BigDecimal newTotalProfitGross = lastTransactionEntity.getCurrentProfitGross().add(totalProfit);
            BigDecimal newTotalProfitNet = lastTransactionEntity.getCurrentProfitNet().add(totalProfit);
            BigDecimal newCashRegisterBalance = lastTransactionEntity.getCashRegisterBalance().add(totalPrice);
            BigDecimal newTotalConsumableInputs = lastTransactionEntity.getTotalConsumableInputs().subtract(totalCost);

            // ⚙️ 9️⃣ Préparation de la nouvelle transaction (annulation)
            newTransactionEntity.setId(null);
            newTransactionEntity.setAmount(totalPrice);
            newTransactionEntity.setAddingDate(Date.valueOf(LocalDate.now()));
            newTransactionEntity.setAddingTime(Time.valueOf(LocalTime.now()));
            newTransactionEntity.setTransactionDate(newTransactionEntity.getAddingDate());
            newTransactionEntity.setTransactionTime(newTransactionEntity.getAddingTime());
            newTransactionEntity.setIdTransactionType(TransactionType.CANCEL_OUT);
            newTransactionEntity.setIdPeriod(lastPeriodEntity.getId());
            newTransactionEntity.setExecutedBy(currentUserId);
            newTransactionEntity.setOriginalTransactionId(originalTransactionEntity.getId());

            // 🔄 🔁 Synchronisation des totaux
            newTransactionEntity.setCurrentCapital(lastTransactionEntity.getCurrentCapital());
            newTransactionEntity.setCurrentProfitGross(newTotalProfitGross);
            newTransactionEntity.setCurrentProfitNet(newTotalProfitNet);
            newTransactionEntity.setTotalExpenses(lastTransactionEntity.getTotalExpenses());
            newTransactionEntity.setTotalCustomerCredit(lastTransactionEntity.getTotalCustomerCredit());
            newTransactionEntity.setTotalExternalLoan(lastTransactionEntity.getTotalExternalLoan());
            newTransactionEntity.setTotalAdvance(lastTransactionEntity.getTotalAdvance());
            newTransactionEntity.setTotalConsumableInputs(newTotalConsumableInputs);
            newTransactionEntity.setTotalNonConsumableInputs(lastTransactionEntity.getTotalNonConsumableInputs());
            newTransactionEntity.setTotalOutOfPocketExpenses(lastTransactionEntity.getTotalOutOfPocketExpenses());
            newTransactionEntity.setCashRegisterBalance(newCashRegisterBalance);

            // 💾 🔟 Sauvegarde de la transaction d’annulation
            TransactionEntity transactionEntityResp = this.transactionService.save(newTransactionEntity);

            // 🏁 1️⃣1️⃣ Mise à jour de l’état de la sortie
            outputEntity.setIdState(ProductTransactionState.CANCELED);

            // Mettre à jour l'état de l'entrée
            consInputEntity.setIdState(ProductTransactionState.AVAILABLE);

            // ✅ 1️⃣2️⃣ Réponse de succès
            return ResponseEntity.ok(new ApiResponse<>(this.outputRepository.save(outputEntity)));
        }
        catch (Exception e) {
            // ❗ Gestion centralisée des erreurs inattendues
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            log.error("Error while canceling output", e);

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(new Exception("💥 An unexpected error occurred while canceling the output.")));
        }
    }

}
