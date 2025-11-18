package com.idApps.storeCashFlowApi.persistence.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.idApps.storeCashFlowApi.help.constants.FinancialPeriodState;
import com.idApps.storeCashFlowApi.help.constants.ProductState;
import com.idApps.storeCashFlowApi.help.constants.TransactionType;
import com.idApps.storeCashFlowApi.models.dto.ProductDto;
import com.idApps.storeCashFlowApi.models.response.ApiResponse;
import com.idApps.storeCashFlowApi.persistence.entity.CategoryProductEntity;
import com.idApps.storeCashFlowApi.persistence.entity.FinancialPeriodEntity;
import com.idApps.storeCashFlowApi.persistence.entity.ProductEntity;
import com.idApps.storeCashFlowApi.persistence.entity.TransactionEntity;
import com.idApps.storeCashFlowApi.persistence.repository.ProductRepository;
import com.idApps.storeCashFlowApi.persistence.service.*;
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

@Slf4j
@Data
@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CurrentUserService currentUserService;
    private final ObjectMapper objectMapper;
    private final CategoryProductService categoryProductService;
    private final TransactionService transactionService;
    private final FinancialPeriodService financialPeriodService;

    @Override
    public ResponseEntity<ApiResponse<List<ProductDto>>> getProducts(Date addingDateMin, Date addingDateMax, Integer stateId, Integer creatorId, Integer categoryId) {
        try {
            List<Map<String, Object>> productsMapList = this.productRepository.getProducts(addingDateMin, addingDateMax, stateId, creatorId, categoryId);
            List<ProductDto> productDtoList = productsMapList.stream().map(item -> this.objectMapper.convertValue(item, ProductDto.class)).toList();
            return ResponseEntity.ok(new ApiResponse<>(productDtoList));
        }
        catch (Exception e) {
            log.error("Error while getting products: " + e);
            return ResponseEntity.internalServerError().body(new ApiResponse<>(e));
        }

    }

    @Transactional
    @Override
    public ResponseEntity<ApiResponse<ProductEntity>> addProduct(ProductDto productDto, String userAccessToken) {
        try {
            // 👤 Récupération de l'utilisateur actuel
            int currentUserId = this.currentUserService.getUserId(userAccessToken);

            // 📅 5️⃣ Récupération de la dernière période financière
            FinancialPeriodEntity lastPeriodEntity = this.financialPeriodService.getLastPeriod().orElse(null);

            ProductEntity newProductEntity = this.objectMapper.convertValue(productDto, ProductEntity.class);
            newProductEntity.setStateId(ProductState.ACTIVE);
            newProductEntity.setAddingDate(Date.valueOf(LocalDate.now()));
            newProductEntity.setAddingTime(Time.valueOf(LocalTime.now()));
            newProductEntity.setCreatedBy(currentUserId);

            TransactionEntity newTransactionEntity = new TransactionEntity();
            TransactionEntity lastTransactionEntity = this.transactionService.getLastTransaction();
            TransactionService.setFinancialTotals(lastTransactionEntity, newTransactionEntity);
            newTransactionEntity.setLabel(newProductEntity.getLabel());
            newTransactionEntity.setAmount(BigDecimal.ZERO);
            newTransactionEntity.setAddingDate(newProductEntity.getAddingDate());
            newTransactionEntity.setAddingTime(newProductEntity.getAddingTime());
            newTransactionEntity.setTransactionDate(newProductEntity.getAddingDate());
            newTransactionEntity.setTransactionTime(newProductEntity.getAddingTime());
            newTransactionEntity.setDetails(newProductEntity.getDetails());
            newTransactionEntity.setImageSrc(newProductEntity.getImageSrc());
            newTransactionEntity.setIdTransactionType(TransactionType.CREATE_PRODUCT);
            newTransactionEntity.setExecutedBy(currentUserId);
            if(lastPeriodEntity != null && lastPeriodEntity.getStateId() == FinancialPeriodState.IN_PROG)
                newTransactionEntity.setIdPeriod(lastPeriodEntity.getId());
            TransactionEntity newTransactionEntityResp = this.transactionService.save(newTransactionEntity);

            newProductEntity.setTransactionId(newTransactionEntityResp.getId());
            ProductEntity productEntityResp = this.productRepository.save(newProductEntity);

            List<CategoryProductEntity> categoryProductEntityList = productDto.getCategoryIdList().stream().map(categoryId -> new CategoryProductEntity(categoryId, productEntityResp.getId())).toList();
            if(!categoryProductEntityList.isEmpty())
                this.categoryProductService.saveAll(categoryProductEntityList);


            return ResponseEntity.ok(new ApiResponse<>(productEntityResp));
        } catch (Exception e) {
            // ❗ Gestion centralisée des erreurs inattendues
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            log.error("Error while canceling output", e);

            return ResponseEntity.internalServerError().body(new ApiResponse<>(e));
        }
    }

    @Override
    public ResponseEntity<ApiResponse<ProductEntity>> cancelProduct(Integer id, String userAccessToken) {
        try {
            // 👤 Récupération de l'utilisateur actuel
            int currentUserId = this.currentUserService.getUserId(userAccessToken);

            // 🔍 Recherche du produit à annuler
            ProductEntity currentProductEntity = this.productRepository.findById(id).orElse(null);
            if (currentProductEntity == null) {
                // ⚠️ Aucun produit correspondant à cet ID
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(new ApiResponse<>(new Exception("⚠️ Aucun produit trouvé avec l'identifiant spécifié.")));
            }

            // 🚫 Vérification de l'état du produit
            if (!currentProductEntity.getStateId().equals(ProductState.ACTIVE)) {
                // ⚠️ Seuls les produits vendus peuvent être annulés
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(new ApiResponse<>(new Exception("⚠️ Le produit doit être dans l'état 'SOLD' pour être annulé.")));
            }

            // 📄 Récupération des transactions associées
            TransactionEntity lastTransactionEntity = this.transactionService.getLastTransaction();
            TransactionEntity originalTransactionEntity = this.transactionService.findById(currentProductEntity.getTransactionId()).orElse(null);
            if (originalTransactionEntity == null) {
                // ⚠️ Aucune transaction d'origine pour ce produit
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(new ApiResponse<>(new Exception("⚠️ Aucune transaction d'origine trouvée pour ce produit.")));
            }

            // 🧬 Duplication de la transaction d'origine
            TransactionEntity newTransactionEntity = this.objectMapper.convertValue(originalTransactionEntity, TransactionEntity.class);
            newTransactionEntity.setId(null);
            // 🧮 Mise à jour des totaux financiers
            TransactionService.setFinancialTotals(lastTransactionEntity, newTransactionEntity);

            // 📆 Mise à jour des informations temporelles
            newTransactionEntity.setTransactionDate(Date.valueOf(LocalDate.now()));
            newTransactionEntity.setTransactionTime(Time.valueOf(LocalTime.now()));
            newTransactionEntity.setAddingDate(newTransactionEntity.getTransactionDate());
            newTransactionEntity.setAddingTime(newTransactionEntity.getTransactionTime());
            newTransactionEntity.setIdTransactionType(TransactionType.CANCEL_PRODUCT);
            newTransactionEntity.setOriginalTransactionId(originalTransactionEntity.getId());
            newTransactionEntity.setExecutedBy(currentUserId);

            // 💾 Sauvegarde de la nouvelle transaction d'annulation
            TransactionEntity newTransactionEntityResp = this.transactionService.save(newTransactionEntity);

            // 🛑 Changement de l’état du produit en "CANCELED"
            currentProductEntity.setStateId(ProductState.CANCELED);
            currentProductEntity.setTransactionId(newTransactionEntityResp.getId());
            ProductEntity currentProductEntityResp = this.productRepository.save(currentProductEntity);

            // ✅ Retour succès
            return ResponseEntity.ok(new ApiResponse<>(currentProductEntityResp));
        } catch (Exception e) {
            // ❗ Gestion centralisée des erreurs inattendues
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            log.error("❌ Erreur lors de l'annulation du produit", e);

            return ResponseEntity
                    .internalServerError()
                    .body(new ApiResponse<>(e));
        }
    }

}
