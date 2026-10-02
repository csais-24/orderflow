package com.cnom.orderflow.inventory.service;

import com.cnom.orderflow.inventory.dto.InventoryResponse;

import java.util.UUID;

public interface InventoryService {

    InventoryResponse createInventory(UUID productId);
    void reserve(UUID inventoryId, Long quantity);
    void complete(UUID inventoryId, Long quantity);
    void restore(UUID inventoryId, Long quantity);
    void addStock(UUID inventoryId, Long quantity);
}
