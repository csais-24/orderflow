package com.cnom.orderflow.order.service;

import com.cnom.orderflow.order.dto.CreateOrderRequest;
import com.cnom.orderflow.order.dto.OrderResponse;

import java.util.UUID;

public interface OrderService {
    //create order
    OrderResponse createOrder(CreateOrderRequest createOrderRequest, UUID idempotencyKey, UUID userId);
    void expirePendingOrders();
}
