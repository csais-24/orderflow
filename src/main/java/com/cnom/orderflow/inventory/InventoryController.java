package com.cnom.orderflow.inventory;

import com.cnom.orderflow.inventory.dto.AddInventoryStockRequest;
import com.cnom.orderflow.inventory.dto.CreateInventoryRequest;
import com.cnom.orderflow.inventory.dto.InventoryResponse;
import com.cnom.orderflow.inventory.service.InventoryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping(path = "/api/v1/inventories")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @PostMapping
    public ResponseEntity<InventoryResponse> createInventory(
            @Valid @RequestBody CreateInventoryRequest createInventoryRequest){

        InventoryResponse inventoryResponse = inventoryService.createInventory(createInventoryRequest.productId());
        return new ResponseEntity<>(inventoryResponse, HttpStatus.CREATED);
    }

    @PatchMapping(path = "/{inventoryId}/stock")
    public ResponseEntity<Void> addInventoryStock(
            @PathVariable UUID inventoryId, @Valid @RequestBody AddInventoryStockRequest addInventoryStockQuantity){

        inventoryService.addStock(inventoryId, addInventoryStockQuantity.quantity());
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
