package com.cnom.orderflow.product.controller;

import com.cnom.orderflow.product.dto.CreateProductRequest;
import com.cnom.orderflow.product.dto.ProductResponse;
import com.cnom.orderflow.product.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api/v1/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService){
        this.productService = productService;
    }

    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody CreateProductRequest createProductRequest) {
        ProductResponse createdProduct = productService.createProduct(createProductRequest);
        return new ResponseEntity<>(createdProduct, HttpStatus.CREATED);
    }
}
