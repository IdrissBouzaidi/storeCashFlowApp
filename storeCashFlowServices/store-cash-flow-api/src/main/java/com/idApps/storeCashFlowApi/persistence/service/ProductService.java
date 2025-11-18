package com.idApps.storeCashFlowApi.persistence.service;

import com.idApps.storeCashFlowApi.models.dto.ProductDto;
import com.idApps.storeCashFlowApi.models.response.ApiResponse;
import com.idApps.storeCashFlowApi.persistence.entity.ProductEntity;
import org.springframework.http.ResponseEntity;

import java.sql.Date;
import java.util.List;

public interface ProductService {

    ResponseEntity<ApiResponse<List<ProductDto>>> getProducts(Date creationDateMin, Date creationDateMax, Integer stateId, Integer creatorId, Integer categoryId);

    ResponseEntity<ApiResponse<ProductEntity>> addProduct(ProductDto productDto, String userAccessToken);

    ResponseEntity<ApiResponse<ProductEntity>> cancelProduct(Integer id, String userAccessToken);
}
