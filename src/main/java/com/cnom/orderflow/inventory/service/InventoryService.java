package com.cnom.orderflow.inventory.service;

import com.cnom.orderflow.inventory.dto.InventoryResponse;

import java.util.UUID;

public interface InventoryService {

    InventoryResponse createInventory(UUID productId);
    void reserve(UUID inventoryId, Integer quantity);
    void complete(UUID inventoryId, Integer quantity);
    void restore(UUID inventoryId, Integer quantity);
    void addStock(UUID inventoryId, Integer quantity);
    InventoryResponse getInventoryByProductId(UUID productId);
}
