package com.idApps.storeCashFlowApi.persistence.repository;

import com.idApps.storeCashFlowApi.persistence.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Integer> {

    @Query(value = """
            SELECT id, first_name as firstName, last_name as lastName FROM user
            """, nativeQuery = true)
    List<Map<String, Object>> getUsersRefTable();
}
