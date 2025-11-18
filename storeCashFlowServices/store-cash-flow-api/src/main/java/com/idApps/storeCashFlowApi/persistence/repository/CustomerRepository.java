package com.idApps.storeCashFlowApi.persistence.repository;

import com.idApps.storeCashFlowApi.persistence.entity.CustomerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface CustomerRepository extends JpaRepository<CustomerEntity, Integer> {

    @Query(value = "SELECT id, first_name, last_name FROM customer;", nativeQuery = true)
    List<Map<String, Object>> getCustomersRefTable();
}
