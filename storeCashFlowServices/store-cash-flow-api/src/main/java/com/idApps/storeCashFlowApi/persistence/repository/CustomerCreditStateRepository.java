package com.idApps.storeCashFlowApi.persistence.repository;

import com.idApps.storeCashFlowApi.persistence.entity.CustomerCreditStateEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerCreditStateRepository extends JpaRepository<CustomerCreditStateEntity, Integer> {

}