package com.cnom.orderflow.orderexpiration.service;

import com.cnom.orderflow.exception.OrderNotFoundException;
import com.cnom.orderflow.inventory.service.InventoryService;
import com.cnom.orderflow.order.entity.Order;
import com.cnom.orderflow.order.entity.OrderItem;
import com.cnom.orderflow.order.entity.enums.OrderStatus;
import com.cnom.orderflow.order.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class OrderExpirationServiceImpl implements OrderExpirationService{

    private final OrderRepository orderRepository;
    private final InventoryService inventoryService;

    public OrderExpirationServiceImpl(OrderRepository orderRepository, InventoryService inventoryService){
        this.orderRepository = orderRepository;
        this.inventoryService = inventoryService;
    }

    @Transactional
    @Override
    public void expireOrder(UUID orderId) {
        Optional<Order> orderFromDb = orderRepository.findById(orderId);
        if (orderFromDb.isEmpty()){
            throw new OrderNotFoundException("Order not found");
        }

        if (!orderFromDb.get().getStatus().equals(OrderStatus.PENDING)){
            return;
        }

        Instant now = Instant.now();
        Instant orderExpiresAt = orderFromDb.get().getExpiresAt();

        if (orderExpiresAt.isAfter(now)) {
            return;
        }

        for (OrderItem orderItem : orderFromDb.get().getOrderItems()){
            UUID inventoryId = inventoryService
                    .getInventoryByProductId(orderItem.getProductId()).id();
            inventoryService.restore(inventoryId, orderItem.getQuantity());
        }

        orderFromDb.get().setStatus(OrderStatus.EXPIRED);
    }
}
