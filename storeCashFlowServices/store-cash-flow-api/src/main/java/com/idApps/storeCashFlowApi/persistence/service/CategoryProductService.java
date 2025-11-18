package com.idApps.storeCashFlowApi.persistence.service;

import com.idApps.storeCashFlowApi.persistence.entity.CategoryProductEntity;

import java.util.List;
import java.util.Optional;

public interface CategoryProductService {

    Optional<CategoryProductEntity> findById(Integer id);

    CategoryProductEntity save(CategoryProductEntity categoryProductEntity);

    List<CategoryProductEntity> saveAll(List<CategoryProductEntity> categoryProductEntityList);
}
