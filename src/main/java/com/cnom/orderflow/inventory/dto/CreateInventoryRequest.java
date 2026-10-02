package com.cnom.orderflow.inventory.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateInventoryRequest(
        @NotNull
        UUID productId
) {
}
