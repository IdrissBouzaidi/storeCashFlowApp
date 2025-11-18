package com.idApps.storeCashFlowApi.persistence.repository;

import com.idApps.storeCashFlowApi.persistence.entity.ProductTransactionStateEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductTransactionStateRepository extends JpaRepository<ProductTransactionStateEntity, Integer> {
}