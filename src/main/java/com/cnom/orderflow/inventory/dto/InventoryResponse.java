package com.cnom.orderflow.inventory.dto;

import java.util.UUID;

public record InventoryResponse(
        UUID id,
        UUID productId,
        Long available,
        Long reserved
) {
}
