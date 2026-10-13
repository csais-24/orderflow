package com.cnom.orderflow.order.service;

import com.cnom.orderflow.exception.IdempotencyKeyConflictException;
import com.cnom.orderflow.inventory.dto.InventoryResponse;
import com.cnom.orderflow.inventory.service.InventoryService;
import com.cnom.orderflow.order.dto.CreateOrderItemRequest;
import com.cnom.orderflow.order.dto.CreateOrderRequest;
import com.cnom.orderflow.order.dto.OrderItemResponse;
import com.cnom.orderflow.order.dto.OrderResponse;
import com.cnom.orderflow.order.entity.Order;
import com.cnom.orderflow.order.entity.OrderItem;
import com.cnom.orderflow.order.entity.enums.OrderStatus;
import com.cnom.orderflow.order.repository.OrderRepository;
import com.cnom.orderflow.orderexpiration.service.OrderExpirationService;
import com.cnom.orderflow.product.dto.ProductResponse;
import com.cnom.orderflow.product.service.ProductService;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class OrderServiceImpl implements OrderService{

    private final OrderRepository orderRepository;
    private final ProductService productService;
    private final InventoryService inventoryService;
    private final OrderExpirationService orderExpirationService;
    private static final Logger logger = LoggerFactory.getLogger(OrderServiceImpl.class);

    public OrderServiceImpl(
            OrderRepository orderRepository,
            ProductService productService,
            InventoryService inventoryService,
            OrderExpirationService orderExpirationService) {
        this.orderRepository = orderRepository;
        this.productService = productService;
        this.inventoryService = inventoryService;
        this.orderExpirationService = orderExpirationService;
    }

    @Transactional
    @Override
    public OrderResponse createOrder(CreateOrderRequest createOrderRequest, UUID idempotencyKey, UUID userId) {
        // verify if idempotencyKey already exists
        Optional<Order> orderFromIdempotencyKey = orderRepository.findByIdempotencyKey(idempotencyKey);
        if (orderFromIdempotencyKey.isPresent()) {
            if (!orderFromIdempotencyKey.get().getUserId().equals(userId)) {
                throw new IdempotencyKeyConflictException("Invalid user ID for this resource");
            }
            return toOrderResponse(orderFromIdempotencyKey.get());
        }
        // Else
        Order orderToSave = new Order(
                        userId,
                        idempotencyKey,
                        Instant.now().plusSeconds(15*60)
                );

        addOrderItems(orderToSave, createOrderRequest.orderItems());
        orderToSave = orderRepository.save(orderToSave);

        return toOrderResponse(orderToSave);
    }

    @Override
    public void expirePendingOrders() {
        List<Order> expiredOrders = orderRepository
                .findByStatusAndExpiresAtLessThanEqual(OrderStatus.PENDING, Instant.now());

        for(Order expiredOrder : expiredOrders){
            try {
                orderExpirationService.expireOrder(expiredOrder.getId());
            } catch (RuntimeException ex) {
                logger.warn("Failed to expire order with id: {}", expiredOrder.getId(), ex);
            }
        }
    }

    private void addOrderItems(Order order, List<CreateOrderItemRequest> orderItemsRequest){
        for (CreateOrderItemRequest orderItemRequest: orderItemsRequest) {
            ProductResponse productResponse = productService.getProductById(orderItemRequest.productId());
            InventoryResponse inventoryResponse = inventoryService.getInventoryByProductId(productResponse.id());
            inventoryService.reserve(inventoryResponse.id(), orderItemRequest.quantity());

            order.addOrderItem(
                    new OrderItem(
                            productResponse.id(),
                            productResponse.name(),
                            orderItemRequest.quantity(),
                            productResponse.price()
                    )
            );
        }
    }

    private BigDecimal getSubTotal(Order order) {
        return order.getOrderItems()
                .stream()
                .map(orderItem ->
                        orderItem.getUnitPrice().multiply(BigDecimal.valueOf(orderItem.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private OrderResponse toOrderResponse(Order order) {
        return new OrderResponse(
                order.getId(),
                order.getUserId(),
                order
                        .getOrderItems()
                        .stream()
                        .map(orderItem -> new OrderItemResponse(
                                orderItem.getId(),
                                orderItem.getProductId(),
                                orderItem.getProductName(),
                                orderItem.getQuantity(),
                                orderItem.getUnitPrice()
                        ))
                        .toList(),
                order.getStatus(),
                order.getCreatedAt(),
                order.getUpdatedAt(),
                order.getExpiresAt(),
                getSubTotal(order)
        );
    }
}

