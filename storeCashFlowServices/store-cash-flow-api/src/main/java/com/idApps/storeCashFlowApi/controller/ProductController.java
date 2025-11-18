package com.idApps.storeCashFlowApi.controller;

import com.idApps.storeCashFlowApi.models.dto.ProductDto;
import com.idApps.storeCashFlowApi.models.response.ApiResponse;
import com.idApps.storeCashFlowApi.persistence.entity.ProductEntity;
import com.idApps.storeCashFlowApi.persistence.service.ProductService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.sql.Date;
import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    @GetMapping
    @SecurityRequirement(name = "keycloak")
    public ResponseEntity<ApiResponse<List<ProductDto>>> getProducts(@RequestParam(required = false) Date addingDateMin,
                                                        @RequestParam(required = false) Date addingDateMax,
                                                        @RequestParam(required = false) Integer stateId,
                                                        @RequestParam(required = false) Integer creatorId,
                                                        @RequestParam(required = false) Integer categoryId) {
        System.out.println("productController: getProducts");
        return this.productService.getProducts(addingDateMin, addingDateMax, stateId, creatorId, categoryId);
    }

    @PostMapping
    @SecurityRequirement(name = "keycloak")
    public ResponseEntity<ApiResponse<ProductEntity>> addProduct(@RequestBody ProductDto body, @AuthenticationPrincipal Jwt jwt) {
        return this.productService.addProduct(body, jwt.getTokenValue());
    }

    @PostMapping("cancel")
    @SecurityRequirement(name = "keycloak")
    public ResponseEntity<ApiResponse<ProductEntity>> cancelProduct(@RequestParam Integer id, @AuthenticationPrincipal Jwt jwt) {
        return this.productService.cancelProduct(id, jwt.getTokenValue());
    }

}
