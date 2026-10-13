package com.cnom.orderflow.order.dto;

import com.cnom.orderflow.order.entity.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OrderResponse(
    UUID id,
    UUID userId,
    List<OrderItemResponse> orderItems,
    OrderStatus orderStatus,
    Instant createdAt,
    Instant updatedAt,
    Instant expiresAt,
    BigDecimal subTotal
) {
}
