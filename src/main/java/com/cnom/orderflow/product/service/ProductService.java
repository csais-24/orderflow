package com.cnom.orderflow.product.service;

import com.cnom.orderflow.product.dto.CreateProductRequest;
import com.cnom.orderflow.product.dto.ProductResponse;

public interface ProductService {
    ProductResponse createProduct(CreateProductRequest createProductRequest);
}
