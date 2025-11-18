package com.idApps.storeCashFlowApi.persistence.repository;

import com.idApps.storeCashFlowApi.persistence.entity.OutOfPocketStateEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OutOfPocketStateRepository extends JpaRepository<OutOfPocketStateEntity, Integer> {
}
