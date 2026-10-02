package com.cnom.orderflow.product.service;

import com.cnom.orderflow.product.dto.CreateProductRequest;
import com.cnom.orderflow.product.dto.ProductResponse;

import java.util.Optional;
import java.util.UUID;

public interface ProductService {
    ProductResponse createProduct(CreateProductRequest createProductRequest);
    boolean productExistsById(UUID productId);
}
