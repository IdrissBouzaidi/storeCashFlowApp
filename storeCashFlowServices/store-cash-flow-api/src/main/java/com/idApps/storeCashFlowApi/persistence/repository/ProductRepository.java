package com.idApps.storeCashFlowApi.persistence.repository;

import com.idApps.storeCashFlowApi.persistence.entity.ProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.util.List;
import java.util.Map;

@Repository
public interface ProductRepository extends JpaRepository<ProductEntity, Integer> {

    @Query(value = """
            SELECT id, label FROM product;
            """, nativeQuery = true)
    List<Map<String, Object>> getProductsRefTable();

    @Query(value = """
            SELECT product.* FROM product
            WHERE (:addingDateMin IS NULL OR :addingDateMin<=adding_date) AND
                (:addingDateMax IS NULL OR :addingDateMax>=adding_date) AND
                (:stateId IS NULL OR :stateId = state_id) AND
                (:creatorId IS NULL OR :creatorId = created_by) AND
                (:categoryId IS NULL OR
                    EXISTS(
                        SELECT * FROM category_product WHERE category_id=:categoryId AND product_id=product.id
                    )
                );
            """, nativeQuery = true)
    List<Map<String, Object>> getProducts(Date addingDateMin, Date addingDateMax, Integer stateId, Integer creatorId, Integer categoryId);
}
