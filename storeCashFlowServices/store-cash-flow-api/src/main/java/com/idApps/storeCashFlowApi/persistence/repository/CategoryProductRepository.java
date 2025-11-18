package com.idApps.storeCashFlowApi.persistence.repository;

import com.idApps.storeCashFlowApi.persistence.entity.AdvanceEntity;
import com.idApps.storeCashFlowApi.persistence.entity.CategoryProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Repository
public interface CategoryProductRepository extends JpaRepository<CategoryProductEntity, Integer> {
}
