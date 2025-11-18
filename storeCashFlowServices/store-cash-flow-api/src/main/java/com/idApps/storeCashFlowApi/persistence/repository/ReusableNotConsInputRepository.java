package com.idApps.storeCashFlowApi.persistence.repository;

import com.idApps.storeCashFlowApi.persistence.entity.ReusableNotConsInputEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface ReusableNotConsInputRepository extends JpaRepository<ReusableNotConsInputEntity, Integer> {

    @Query(value = """
            SELECT id, label FROM reusable_not_consumable_input
            """, nativeQuery = true)
    List<Map<String, Object>> getReusableInputsRefTbale();
}
