package com.cnom.orderflow.inventory.service;

import com.cnom.orderflow.exception.*;
import com.cnom.orderflow.inventory.dto.InventoryResponse;
import com.cnom.orderflow.inventory.entity.Inventory;
import com.cnom.orderflow.inventory.repository.InventoryRepository;
import com.cnom.orderflow.product.service.ProductService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class InventoryServiceImpl implements InventoryService{

    private final ProductService productService;
    private final InventoryRepository inventoryRepository;

    public InventoryServiceImpl(ProductService productService, InventoryRepository inventoryRepository) {
        this.productService = productService;
        this.inventoryRepository = inventoryRepository;
    }

    @Override
    public InventoryResponse createInventory(UUID productId) {
        if (!productService.productExistsById(productId)) {
            throw new ProductNotFoundException("product not found");
        }

        if (inventoryRepository.existsByProductId(productId)) {
            throw new InventoryAlreadyExistsException("product already in the inventory");
        }

        Inventory savedInventory = inventoryRepository.save(new Inventory(productId, 0L, 0L));
        return new InventoryResponse(
                savedInventory.getId(),
                savedInventory.getProductId(),
                savedInventory.getAvailable(),
                savedInventory.getReserved());
    }

    @Override
    public void reserve(UUID inventoryId, Long quantity) {
        if (quantity <= 0){
            throw new IllegalQuantityException("Amount must be positive");
        }

        Inventory inventoryFromDb = inventoryRepository
                .findById(inventoryId).orElseThrow(() -> new InventoryNotFoundException("inventory not found"));
        if (inventoryFromDb.getAvailable() < quantity) {
            throw new InsufficientInventoryException("not enough stock on inventory");
        }

        inventoryFromDb.setAvailable(inventoryFromDb.getAvailable() - quantity);
        inventoryFromDb.setReserved(inventoryFromDb.getReserved() + quantity);
    }

    @Override
    public void complete(UUID inventoryId, Long quantity) {
        if (quantity <= 0){
            throw new IllegalQuantityException("Amount must be positive");
        }
        Inventory inventoryFromDb = inventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new InventoryNotFoundException("inventory not found"));

        if (inventoryFromDb.getReserved() < quantity) {
            throw new InsufficientInventoryException("not enough stock on inventory");
        }
        inventoryFromDb.setReserved(inventoryFromDb.getReserved() - quantity);
    }

    @Override
    public void restore(UUID inventoryId, Long quantity) {
        if (quantity <= 0){
            throw new IllegalQuantityException("Amount must be positive");
        }
        Inventory inventoryFromDb = inventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new InventoryNotFoundException("inventory not found"));

        if (inventoryFromDb.getReserved() < quantity) {
            throw new InsufficientInventoryException("not enough stock on inventory");
        }

        inventoryFromDb.setAvailable(inventoryFromDb.getAvailable() + quantity);
        inventoryFromDb.setReserved(inventoryFromDb.getReserved() - quantity);
    }

    @Transactional
    @Override
    public void addStock(UUID inventoryId, Long quantity) {
        if (quantity <= 0){
            throw new IllegalQuantityException("Amount must be positive");
        }
        Inventory inventoryFromDb = inventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new InventoryNotFoundException("inventory not found"));
        inventoryFromDb.setAvailable(inventoryFromDb.getAvailable() + quantity);
    }
}
