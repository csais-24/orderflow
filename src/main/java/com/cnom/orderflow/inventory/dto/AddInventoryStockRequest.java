package com.cnom.orderflow.inventory.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record AddInventoryStockRequest(
        @NotNull
        @Min(1)
        Long quantity
) {
}
