package com.idApps.storeCashFlowApi.persistence.repository;

import com.idApps.storeCashFlowApi.persistence.entity.NotConsumableInputStateEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotConsumableInputStateRepository extends JpaRepository<NotConsumableInputStateEntity, Integer> {
}
