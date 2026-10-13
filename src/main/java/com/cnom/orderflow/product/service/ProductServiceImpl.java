package com.cnom.orderflow.product.service;

import com.cnom.orderflow.exception.ProductNotFoundException;
import com.cnom.orderflow.product.dto.CreateProductRequest;
import com.cnom.orderflow.product.dto.ProductResponse;
import com.cnom.orderflow.product.entity.Product;
import com.cnom.orderflow.product.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    public ProductServiceImpl(ProductRepository productRepository){
        this.productRepository = productRepository;
    }

    @Override
    public ProductResponse createProduct(CreateProductRequest createProductRequest) {
        Product savedProduct = productRepository.save(
                new Product(
                        createProductRequest.name(),
                        createProductRequest.description(),
                        createProductRequest.price()
        ));
        return new ProductResponse(
          savedProduct.getId(),
          savedProduct.getName(),
          savedProduct.getDescription(),
          savedProduct.getPrice(),
          savedProduct.isActive(),
          savedProduct.getCreatedAt(),
          savedProduct.getUpdatedAt()
        );
    }

    @Override
    public boolean productExistsById(UUID productId) {
        return productRepository.existsById(productId);
    }

    @Override
    public ProductResponse getProductById(UUID productId) {
        Product productFromDb = productRepository
                .findById(productId).orElseThrow(() -> new ProductNotFoundException("Product not found"));
        return new ProductResponse(
                productFromDb.getId(),
                productFromDb.getName(),
                productFromDb.getDescription(),
                productFromDb.getPrice(),
                productFromDb.isActive(),
                productFromDb.getCreatedAt(),
                productFromDb.getUpdatedAt()
        );
    }


}
