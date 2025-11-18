package com.idApps.storeCashFlowApi.persistence.service;

import com.idApps.storeCashFlowApi.persistence.entity.CategoryProductEntity;
import com.idApps.storeCashFlowApi.persistence.repository.CategoryProductRepository;
import lombok.Data;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Data
@Service
public class CategoryProductServiceImpl implements CategoryProductService {

    private final CategoryProductRepository categoryProductRepository;


    @Override
    public Optional<CategoryProductEntity> findById(Integer id) {
        return this.categoryProductRepository.findById(id);
    }

    @Override
    public CategoryProductEntity save(CategoryProductEntity categoryProductEntity) {
        return this.categoryProductRepository.save(categoryProductEntity);
    }

    @Override
    public List<CategoryProductEntity> saveAll(List<CategoryProductEntity> categoryProductEntityList) {
        return this.categoryProductRepository.saveAll(categoryProductEntityList);
    }
}
