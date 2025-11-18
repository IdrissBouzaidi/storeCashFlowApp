package com.idApps.storeCashFlowApi.persistence.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.idApps.storeCashFlowApi.help.constants.FinancialPeriodState;
import com.idApps.storeCashFlowApi.help.constants.TransactionType;
import com.idApps.storeCashFlowApi.models.dto.FinancialPeriodDto;
import com.idApps.storeCashFlowApi.models.response.ApiResponse;
import com.idApps.storeCashFlowApi.persistence.entity.FinancialPeriodEntity;
import com.idApps.storeCashFlowApi.persistence.entity.TransactionEntity;
import com.idApps.storeCashFlowApi.persistence.repository.FinancialPeriodRepository;
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
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Slf4j
@Data
@Service
public class FinancialPeriodServiceImpl implements FinancialPeriodService {

    private final FinancialPeriodRepository financialPeriodRepository;

    private final TransactionService transactionService;

    private final ObjectMapper objectMapper;

    private final CurrentUserService currentUserService;

    @Override
    public Integer getActivePeriodId() {
        return this.financialPeriodRepository.getActivePeriodId().get();
    }

    @Override
    public Boolean isSomePeriodInProgress() {
        return this.financialPeriodRepository.isSomePeriodInProgress() == 1;
    }

    @Transactional
    @Override
    public ResponseEntity<ApiResponse<FinancialPeriodEntity>> closeCurrentPeriod(String userAccessToken, Date endDate, Time endTime) {
        Integer currentUserId = this.currentUserService.getUserId(userAccessToken);

        FinancialPeriodEntity currentPeriodEntity = this.financialPeriodRepository.getActivePeriod().orElse(null);
        // ❌ Aucun exercice actif
        if (currentPeriodEntity == null) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(new Exception("No active financial period found to close.")));
        }

        // ✅ Si une date de fin est fournie
        if(endDate != null) {
            if(endTime == null)
                endTime = Time.valueOf(LocalTime.MIDNIGHT);
            LocalDateTime endDateTime = LocalDateTime.of(endDate.toLocalDate(), endTime.toLocalTime());
            // ❌ Cas où la date de fin dépasse la date actuelle
            if (endDateTime.isAfter(LocalDateTime.now())) {
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(new ApiResponse<>(new Exception("The end date of the period cannot be in the future.")));
            }
            // ✅ Cas normal
            else {
                currentPeriodEntity.setEndDate(endDate);
                currentPeriodEntity.setEndTime(endTime);
                // ✅ Tout est bon → on enregistre
            }
        }
        else {
            // ✅ Si aucune date/heure de fin n’est fournie, on prend la date actuelle
            currentPeriodEntity.setEndDate(Date.valueOf(LocalDate.now()));
            currentPeriodEntity.setEndTime(Time.valueOf(LocalTime.now()));
            // ✅ Tout est bon → on enregistre
        }

        try {
            TransactionEntity lastTransactionEntity = this.transactionService.getLastTransaction();
            TransactionEntity newTransactionEntity = new TransactionEntity();

            // ✅ Les nouvelles valeurs pour la période
            long duration = ChronoUnit.DAYS.between(currentPeriodEntity.getStartDate().toLocalDate(), currentPeriodEntity.getEndDate().toLocalDate());
            currentPeriodEntity.setDuration((int) duration);
            currentPeriodEntity.setStateId(FinancialPeriodState.CLOSED);

            currentPeriodEntity.setCurrentCapital(lastTransactionEntity.getCurrentCapital());
            currentPeriodEntity.setCurrentProfitGross(lastTransactionEntity.getCurrentProfitGross());
            currentPeriodEntity.setCurrentProfitNet(lastTransactionEntity.getCurrentProfitNet());
            currentPeriodEntity.setTotalExpenses(lastTransactionEntity.getTotalExpenses());
            currentPeriodEntity.setTotalCustomerCredit(lastTransactionEntity.getTotalCustomerCredit());
            currentPeriodEntity.setTotalExternalLoan(lastTransactionEntity.getTotalExternalLoan());
            currentPeriodEntity.setTotalAdvance(lastTransactionEntity.getTotalAdvance());
            currentPeriodEntity.setTotalConsumableInputs(lastTransactionEntity.getTotalConsumableInputs());
            currentPeriodEntity.setTotalNonConsumableInputs(lastTransactionEntity.getTotalNonConsumableInputs());
            currentPeriodEntity.setCashRegisterBalance(lastTransactionEntity.getCashRegisterBalance());
            currentPeriodEntity.setTotalOutOfPocketExpenses(lastTransactionEntity.getTotalOutOfPocketExpenses());

            TransactionService.setFinancialTotals(lastTransactionEntity, newTransactionEntity);
            newTransactionEntity.setLabel(currentPeriodEntity.getLabel());
            newTransactionEntity.setAmount(BigDecimal.ZERO);
            newTransactionEntity.setAddingDate(Date.valueOf(LocalDate.now()));
            newTransactionEntity.setAddingTime(Time.valueOf(LocalTime.now()));
            newTransactionEntity.setTransactionDate(currentPeriodEntity.getEndDate());
            newTransactionEntity.setTransactionTime(currentPeriodEntity.getEndTime());
            newTransactionEntity.setDetails(currentPeriodEntity.getDetails());
            newTransactionEntity.setOriginalTransactionId(currentPeriodEntity.getTransactionId());
            newTransactionEntity.setIdTransactionType(TransactionType.CLOSE_PERIOD);
            newTransactionEntity.setIdPeriod(currentPeriodEntity.getId());
            newTransactionEntity.setExecutedBy(currentUserId);
            TransactionEntity transactionEntityResp = this.transactionService.save(newTransactionEntity);

            FinancialPeriodEntity currentPeriodResp = this.financialPeriodRepository.save(currentPeriodEntity);
            return ResponseEntity.ok(new ApiResponse<>(currentPeriodResp));
        }
        catch (Exception e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();

            // Log du message d’erreur
            log.error("Error closing period", e);

            // ❌ Retour cohérent
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(new Exception("An unexpected error occurred while closing the period.")));
        }
    }

    @Override
    public Optional<FinancialPeriodEntity> getLastPeriod() {
        return this.financialPeriodRepository.getLastPeriod();
    }

    @Transactional
    @Override
    public ResponseEntity<ApiResponse<FinancialPeriodEntity>> cancel(String tokenValue, int id) {
        Integer currentUserId = this.currentUserService.getUserId(tokenValue);

        FinancialPeriodEntity financialPeriodEntity = this.financialPeriodRepository.findById(id).orElse(null);
        if (financialPeriodEntity == null) {
            // No financial period matches the given ID
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(new Exception("No financial period found with the specified ID.")));
        }

        if (financialPeriodEntity.getStateId() != FinancialPeriodState.IN_PROG) {
            // The period must be in progress to be canceled
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(new Exception("The financial period must be in progress to be canceled.")));
        }

        boolean periodIsNotEmpty = (this.financialPeriodRepository.isPeriodNotEmpty(id) == 1);
        if (periodIsNotEmpty) {
            // The period to be canceled must be empty
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(new Exception("A financial period containing transactions cannot be canceled.")));
        }

        TransactionEntity lastTransactionEntity = this.transactionService.getLastTransaction();
        // Ces montants doivent être égaux à 0, sinon les données ne sont pas synchronisées.
        if (lastTransactionEntity.getCurrentProfitGross().equals(BigDecimal.ZERO)
                || lastTransactionEntity.getCurrentProfitNet().equals(BigDecimal.ZERO)
                || lastTransactionEntity.getTotalExpenses().equals(BigDecimal.ZERO)) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(new Exception("Financial data inconsistency detected: profits or expenses should be zero before canceling.")));
        }

        try {
            TransactionEntity newTransactionEntity = new TransactionEntity();

            TransactionService.setFinancialTotals(lastTransactionEntity, newTransactionEntity);

            newTransactionEntity.setLabel(financialPeriodEntity.getLabel());
            newTransactionEntity.setAmount(BigDecimal.ZERO);
            newTransactionEntity.setAddingDate(Date.valueOf(LocalDate.now()));
            newTransactionEntity.setAddingTime(Time.valueOf(LocalTime.now()));
            newTransactionEntity.setTransactionDate(newTransactionEntity.getAddingDate());
            newTransactionEntity.setTransactionTime(newTransactionEntity.getAddingTime());
            newTransactionEntity.setDetails(financialPeriodEntity.getDetails());
            newTransactionEntity.setOriginalTransactionId(financialPeriodEntity.getTransactionId());
            newTransactionEntity.setIdTransactionType(TransactionType.CANCEL_PERIOD);
            newTransactionEntity.setIdPeriod(financialPeriodEntity.getId());
            newTransactionEntity.setExecutedBy(currentUserId);

            TransactionEntity transactionEntityResp = this.transactionService.save(newTransactionEntity);

            financialPeriodEntity.setStateId(FinancialPeriodState.CANCELED);
            return ResponseEntity.ok(new ApiResponse(this.financialPeriodRepository.save(financialPeriodEntity)));
        } catch (Exception e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();

            // Log the error message
            log.error("Error while canceling financial period", e);

            // Return a coherent error response
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(new Exception("An unexpected error occurred while canceling the financial period.")));
        }
    }

    @Override
    public ResponseEntity<ApiResponse<FinancialPeriodEntity>> reopen(String tokenValue, int id) {
        // 🔐 Récupérer l’ID de l’utilisateur courant à partir du token JWT
        Integer currentUserId = this.currentUserService.getUserId(tokenValue);

        // 🔎 Rechercher la période financière correspondant à l’ID passé en paramètre
        FinancialPeriodEntity financialPeriodEntity = this.financialPeriodRepository.findById(id).orElse(null);

        // ❌ Cas 1 : Aucune période ne correspond à l’ID fourni
        if (financialPeriodEntity == null) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(new Exception("No financial period corresponds to the specified ID.")));
        }

        // 🔍 Charger la transaction d’origine liée à cette période
        TransactionEntity originalTransactionEntity = this.transactionService
                .findById(financialPeriodEntity.getTransactionId())
                .orElse(null);

        // ❌ Cas 2 : Si aucune transaction originale n’est trouvée
        if (originalTransactionEntity == null) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(new Exception("Original transaction not found for this financial period.")));
        }

        // 🚫 Cas 3 : La période n’est pas dans l’état "CLOSED" (fermée)
        if (financialPeriodEntity.getStateId() != FinancialPeriodState.CLOSED) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(new Exception("The financial period must be closed before it can be reopened.")));
        }

        // ⏳ Cas 4 : Vérifie si la période est bien la dernière fermée
        FinancialPeriodEntity lastFinancialPeriod = this.financialPeriodRepository.getLastPeriod().orElse(null);
        if (lastFinancialPeriod == null || !financialPeriodEntity.getId().equals(lastFinancialPeriod.getId())) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(new Exception("Only the most recently closed financial period can be reopened.")));
        }

        // 📜 Récupérer la dernière transaction enregistrée
        TransactionEntity lastTransactionEntity = this.transactionService.getLastTransaction();

        // ⚖️ Cas 5 : Vérifier la cohérence des montants avant la réouverture

        try {
            // 🧾 Créer une nouvelle transaction pour marquer la réouverture
            TransactionEntity newTransactionEntity = new TransactionEntity();

            // 🧮 Copier les totaux financiers depuis la dernière transaction
            TransactionService.setFinancialTotals(lastTransactionEntity, newTransactionEntity);

            // 🏷️ Préparer les métadonnées de la transaction de réouverture
            newTransactionEntity.setLabel(financialPeriodEntity.getLabel());
            newTransactionEntity.setAmount(BigDecimal.ZERO);
            newTransactionEntity.setAddingDate(Date.valueOf(LocalDate.now()));
            newTransactionEntity.setAddingTime(Time.valueOf(LocalTime.now()));
            newTransactionEntity.setTransactionDate(newTransactionEntity.getAddingDate());
            newTransactionEntity.setTransactionTime(newTransactionEntity.getAddingTime());
            newTransactionEntity.setDetails(financialPeriodEntity.getDetails());
            newTransactionEntity.setOriginalTransactionId(originalTransactionEntity.getId());
            newTransactionEntity.setIdTransactionType(TransactionType.REOPEN_PERIOD);
            newTransactionEntity.setIdPeriod(financialPeriodEntity.getId());
            newTransactionEntity.setExecutedBy(currentUserId);

            // 💾 Enregistrer la nouvelle transaction
            TransactionEntity transactionEntityResp = this.transactionService.save(newTransactionEntity);

            // 🔄 Mettre à jour la période à l’état "IN_PROGRESS"
            financialPeriodEntity.setStateId(FinancialPeriodState.IN_PROG);

            financialPeriodEntity.setCurrentCapital(lastTransactionEntity.getCurrentCapital());
            financialPeriodEntity.setCurrentProfitGross(lastTransactionEntity.getCurrentProfitGross());
            financialPeriodEntity.setCurrentProfitNet(lastTransactionEntity.getCurrentProfitNet());
            financialPeriodEntity.setTotalExpenses(lastTransactionEntity.getTotalExpenses());
            financialPeriodEntity.setTotalCustomerCredit(lastTransactionEntity.getTotalCustomerCredit());
            financialPeriodEntity.setTotalExternalLoan(lastTransactionEntity.getTotalExternalLoan());
            financialPeriodEntity.setTotalAdvance(lastTransactionEntity.getTotalAdvance());
            financialPeriodEntity.setTotalConsumableInputs(lastTransactionEntity.getTotalConsumableInputs());
            financialPeriodEntity.setTotalNonConsumableInputs(lastTransactionEntity.getTotalNonConsumableInputs());
            financialPeriodEntity.setCashRegisterBalance(lastTransactionEntity.getCashRegisterBalance());
            financialPeriodEntity.setTotalOutOfPocketExpenses(lastTransactionEntity.getTotalOutOfPocketExpenses());

            // 🎯 Retourner la période mise à jour dans la réponse
            return ResponseEntity.ok(new ApiResponse<>(this.financialPeriodRepository.save(financialPeriodEntity)));

        } catch (Exception e) {
            // 🚨 Annuler la transaction si une erreur survient
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();

            // 🧾 Journaliser l’erreur dans les logs
            log.error("Error while reopening financial period", e);

            // ⚠️ Retourner une réponse d’erreur cohérente
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(new Exception("An unexpected error occurred while reopening the financial period.")));
        }
    }



    @Override
    public List<FinancialPeriodDto> getFinancialPeriods(Date startDateMin, Date startDateMax, Date endDateMin, Date endDateMax, Integer stateId) {
        return this.financialPeriodRepository.getFinancialPeriods(startDateMin, startDateMax, endDateMin, endDateMax, stateId).stream().map(item -> this.objectMapper.convertValue(item, FinancialPeriodDto.class)).toList();
    }

    @Transactional
    @Override
    public ResponseEntity<ApiResponse<FinancialPeriodEntity>> addFinancialPeriod(String userAccessToken, FinancialPeriodDto financialPeriodDto) {
        // 🔐 Récupération de l’utilisateur courant à partir du token JWT
        Integer currentUserId = this.currentUserService.getUserId(userAccessToken);

        // ⚙️ Initialisation de base pour une nouvelle période
        financialPeriodDto.setStateId(FinancialPeriodState.IN_PROG);
        financialPeriodDto.setEndDate(null);
        financialPeriodDto.setEndTime(null);

        Date newPeriodStartDate = financialPeriodDto.getStartDate();
        Time newPeriodStartTime = financialPeriodDto.getStartTime();

        // 🕛 Si la date de début existe mais pas l’heure → on met minuit par défaut
        if (newPeriodStartDate != null && newPeriodStartTime == null)
            financialPeriodDto.setStartTime(Time.valueOf(LocalTime.MIDNIGHT));

        // 🧮 Conversion en LocalDateTime pour faciliter les comparaisons temporelles
        LocalDateTime newPeriodStartDateTime =
                newPeriodStartDate != null
                        ? LocalDateTime.of(newPeriodStartDate.toLocalDate(), newPeriodStartTime.toLocalTime())
                        : null;

        // 🚫 Cas d’erreur : la date de début ne peut pas être dans le futur
        if (newPeriodStartDateTime != null && newPeriodStartDateTime.isAfter(LocalDateTime.now())) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(new Exception("The start date of the new period cannot be in the future.")));
        }

        // 🔎 Récupération de la dernière période enregistrée
        FinancialPeriodEntity lastPeriodEntity = this.financialPeriodRepository.getLastPeriod().orElse(null);
        // 🟢 Cas 1 : Aucune période existante — il s’agit de la première
        if (lastPeriodEntity == null) {
            if (newPeriodStartDate == null || newPeriodStartTime == null) {
                // ⚠️ Erreur : il faut absolument fournir une date et une heure pour la première période
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(new ApiResponse<>(new Exception("Start date and time must be provided for the first period.")));
            }
        }
        else {

            // 🟡 Cas 2 : Une période précédente existe déjà
            if (lastPeriodEntity.getStateId() != FinancialPeriodState.CLOSED) {
                // 🚫 Impossible de créer une nouvelle période si l’ancienne n’est pas clôturée
                return ResponseEntity
                        .status(HttpStatus.CONFLICT)
                        .body(new ApiResponse<>(new Exception("Cannot start a new period while the previous one is still active.")));
            }

            // 📅 On construit le LocalDateTime de fin de la dernière période
            LocalDateTime lastPeriodEndDateTime = LocalDateTime.of(
                    lastPeriodEntity.getEndDate().toLocalDate(),
                    lastPeriodEntity.getEndTime().toLocalTime()
            );

            // 🔧 Si aucune date de début n’est fournie, on commence juste après la fin de la précédente
            if (newPeriodStartDate == null) {
                financialPeriodDto.setStartDate(lastPeriodEntity.getEndDate());
                LocalDateTime lastPeriodEndDatePlusSecond = lastPeriodEndDateTime.plusSeconds(1);
                financialPeriodDto.setStartDate(Date.valueOf(lastPeriodEndDatePlusSecond.toLocalDate()));
                financialPeriodDto.setStartTime(Time.valueOf(lastPeriodEndDatePlusSecond.toLocalTime()));
                // ✅ La nouvelle période démarre juste après la fin de l’ancienne
            } else {
                // ⛔ Cas d’erreur : la nouvelle période ne peut pas commencer avant ou au même moment que la précédente
                if (lastPeriodEndDateTime.isAfter(newPeriodStartDateTime) || lastPeriodEndDateTime.equals(newPeriodStartDateTime)) {
                    return ResponseEntity
                            .status(HttpStatus.CONFLICT)
                            .body(new ApiResponse<>(new Exception("The start of the new period must be after the end of the previous one.")));
                }
            }
        }

        // 💰 Vérification de la cohérence financière avant création
        TransactionEntity lastTransactionEntity = this.transactionService.getLastTransaction();

        try {
            // 🧩 Conversion du DTO en entité persistable
            FinancialPeriodEntity financialPeriodEntity = this.objectMapper.convertValue(financialPeriodDto, FinancialPeriodEntity.class);

            // 🪣 Initialisation des valeurs financières à partir de la dernière transaction
            financialPeriodEntity.setCurrentCapital(lastTransactionEntity.getCurrentCapital());
            financialPeriodEntity.setCurrentProfitGross(lastTransactionEntity.getCurrentProfitGross());
            financialPeriodEntity.setCurrentProfitNet(lastTransactionEntity.getCurrentProfitNet());
            financialPeriodEntity.setTotalExpenses(lastTransactionEntity.getTotalExpenses());
            financialPeriodEntity.setTotalCustomerCredit(lastTransactionEntity.getTotalCustomerCredit());
            financialPeriodEntity.setTotalExternalLoan(lastTransactionEntity.getTotalExternalLoan());
            financialPeriodEntity.setTotalAdvance(lastTransactionEntity.getTotalAdvance());
            financialPeriodEntity.setTotalConsumableInputs(lastTransactionEntity.getTotalConsumableInputs());
            financialPeriodEntity.setTotalNonConsumableInputs(lastTransactionEntity.getTotalNonConsumableInputs());
            financialPeriodEntity.setCashRegisterBalance(lastTransactionEntity.getCashRegisterBalance());
            financialPeriodEntity.setTotalOutOfPocketExpenses(lastTransactionEntity.getTotalOutOfPocketExpenses());

            // 💾 Sauvegarde de la nouvelle période
            FinancialPeriodEntity financialPeriodEntityResp = this.financialPeriodRepository.save(financialPeriodEntity);

            // 🧮 Création de la transaction de création de période
            TransactionEntity newTransactionEntity = new TransactionEntity();
            TransactionService.setFinancialTotals(lastTransactionEntity, newTransactionEntity);
            newTransactionEntity.setCurrentProfitGross(BigDecimal.ZERO);
            newTransactionEntity.setCurrentProfitNet(BigDecimal.ZERO);
            newTransactionEntity.setTotalExpenses(BigDecimal.ZERO);

            newTransactionEntity.setLabel(financialPeriodDto.getLabel());
            newTransactionEntity.setAmount(BigDecimal.ZERO);
            newTransactionEntity.setAddingDate(Date.valueOf(LocalDate.now()));
            newTransactionEntity.setAddingTime(Time.valueOf(LocalTime.now()));
            newTransactionEntity.setTransactionDate(financialPeriodDto.getStartDate());
            newTransactionEntity.setTransactionTime(financialPeriodDto.getStartTime());
            newTransactionEntity.setDetails(financialPeriodDto.getDetails());
            newTransactionEntity.setOriginalTransactionId(financialPeriodDto.getTransactionId());
            newTransactionEntity.setIdTransactionType(TransactionType.CREATE_PERIOD);
            newTransactionEntity.setIdPeriod(financialPeriodEntityResp.getId());
            newTransactionEntity.setExecutedBy(currentUserId);

            // 💾 Enregistrement de la transaction associée
            TransactionEntity transactionEntityResp = this.transactionService.save(newTransactionEntity);

            financialPeriodEntityResp.setTransactionId(transactionEntityResp.getId());
            // ✅ Succès → Retour de la période créée
            return ResponseEntity.ok(new ApiResponse<>(financialPeriodEntityResp));
        }
        catch (Exception e) {
            // 🛑 En cas d’erreur → rollback complet
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            log.error("addFinancialPeriod error: " + e);

            // ⚠️ Retour d’une erreur serveur avec les détails
            return ResponseEntity.internalServerError().body(new ApiResponse<>(e));
        }
    }

}
