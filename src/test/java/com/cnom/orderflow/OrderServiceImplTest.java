package com.cnom.orderflow;

import com.cnom.orderflow.exception.IdempotencyKeyConflictException;
import com.cnom.orderflow.inventory.dto.InventoryResponse;
import com.cnom.orderflow.inventory.service.InventoryService;
import com.cnom.orderflow.order.dto.CreateOrderItemRequest;
import com.cnom.orderflow.order.dto.CreateOrderRequest;
import com.cnom.orderflow.order.dto.OrderResponse;
import com.cnom.orderflow.order.entity.Order;
import com.cnom.orderflow.order.entity.OrderItem;
import com.cnom.orderflow.order.entity.enums.OrderStatus;
import com.cnom.orderflow.order.repository.OrderRepository;
import com.cnom.orderflow.order.service.OrderServiceImpl;
import com.cnom.orderflow.orderexpiration.service.OrderExpirationService;
import com.cnom.orderflow.product.dto.ProductResponse;
import com.cnom.orderflow.product.service.ProductService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class OrderServiceImplTest {

    private OrderRepository orderRepository;
    private ProductService productService;
    private InventoryService inventoryService;
    private OrderServiceImpl orderService;
    private OrderExpirationService orderExpirationService;

    @BeforeEach
    void setUp(){
        orderRepository = Mockito.mock();
        productService = Mockito.mock();
        inventoryService = Mockito.mock();
        orderExpirationService = Mockito.mock();
        orderService = new OrderServiceImpl(orderRepository, productService, inventoryService, orderExpirationService);
    }

    @Test
    public void shouldCreateOrderSuccessfully(){
        // Given
        UUID fixedUserId = UUID.fromString("ffc2550a-924c-4fce-9d8a-162843c10333");
        UUID fixedProductId = UUID.fromString("acde070d-8c4c-4f0d-9d8a-162843c10333");
        UUID fixedIdempotencyKey = UUID.fromString("ee1e070d-8f4b-5a5c-9d1d-112843c10222");
        UUID fixedInventoryId = UUID.fromString("ee1e070d-8f4b-5a5c-9d1d-112843c10222");
        ArgumentCaptor<Order> argumentCaptor = ArgumentCaptor.forClass(Order.class);

        ProductResponse productResponseMock = new ProductResponse(
                fixedProductId,
                "keyboard",
                "mechanical",
                new BigDecimal("125.50"),
                true,
                Instant.now(),
                Instant.now()
                );
        InventoryResponse inventoryResponseMock = new InventoryResponse(
                fixedInventoryId,
                fixedProductId,
                10,
                2
        );
        CreateOrderRequest orderRequest = new CreateOrderRequest(
                List.of(
                        new CreateOrderItemRequest(fixedProductId, 5)
                )
        );

        Mockito.when(orderRepository.findByIdempotencyKey(Mockito.any(UUID.class)))
                .thenReturn(Optional.empty());
        Mockito.when(productService.getProductById(Mockito.any(UUID.class)))
                .thenReturn(productResponseMock);
        Mockito.when(inventoryService.getInventoryByProductId(Mockito.any(UUID.class)))
                .thenReturn(inventoryResponseMock);
        Mockito.when(orderRepository.save(Mockito.any(Order.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        // When
        OrderResponse orderResponse = orderService.createOrder(orderRequest, fixedIdempotencyKey, fixedUserId);
        // Then
        Mockito.verify(inventoryService).reserve(fixedInventoryId, 5);
        Mockito.verify(orderRepository).findByIdempotencyKey(fixedIdempotencyKey);
        Mockito.verify(productService).getProductById(fixedProductId);
        Mockito.verify(inventoryService).getInventoryByProductId(fixedProductId);
        Mockito.verify(orderRepository).save(argumentCaptor.capture());
        Order capturedOrder = argumentCaptor.getValue();

        Assertions.assertEquals(fixedUserId, orderResponse.userId());
        Assertions.assertEquals(fixedIdempotencyKey, capturedOrder.getIdempotencyKey());
        Assertions.assertEquals(OrderStatus.PENDING, orderResponse.orderStatus());
        Assertions.assertEquals(1, orderResponse.orderItems().size());

        OrderItem capturedItem = capturedOrder.getOrderItems().getFirst();

        Assertions.assertEquals(fixedProductId, capturedItem.getProductId());
        Assertions.assertEquals("keyboard", capturedItem.getProductName());
        Assertions.assertEquals(5, capturedItem.getQuantity());
        Assertions.assertEquals(new BigDecimal("627.50"),  orderResponse.subTotal());
    }

    @Test
    public void shouldReturnExistingOrderWhenIdempotencyKeyAlreadyExists(){
        UUID fixedUserId = UUID.fromString("ffc2550a-924c-4fce-9d8a-162843c10333");
        UUID fixedProductId = UUID.fromString("acde070d-8c4c-4f0d-9d8a-162843c10333");
        UUID fixedIdempotencyKey = UUID.fromString("ee1e070d-8f4b-5a5c-9d1d-112843c10222");

        CreateOrderRequest orderRequest = new CreateOrderRequest(
                List.of(
                        new CreateOrderItemRequest(fixedProductId, 5)
                )
        );

        Order existingOrder = new Order(
                fixedUserId,
                fixedIdempotencyKey,
                Instant.now().plusSeconds(60*8)
        );

        Mockito.when(orderRepository.findByIdempotencyKey(fixedIdempotencyKey))
                .thenReturn(Optional.of(existingOrder));

        OrderResponse orderResponse = orderService.createOrder(orderRequest, fixedIdempotencyKey, fixedUserId);

        Mockito.verify(orderRepository).findByIdempotencyKey(fixedIdempotencyKey);
        Assertions.assertEquals(existingOrder.getUserId(), orderResponse.userId());
        Assertions.assertEquals(existingOrder.getStatus(), orderResponse.orderStatus());
        Assertions.assertEquals(existingOrder.getOrderItems().size(), orderResponse.orderItems().size());

        Mockito.verify(productService, Mockito.never()).getProductById(Mockito.any(UUID.class));
        Mockito.verify(inventoryService, Mockito.never()).getInventoryByProductId(Mockito.any(UUID.class));
        Mockito.verify(inventoryService, Mockito.never()).reserve(Mockito.any(UUID.class),Mockito.anyInt());
        Mockito.verify(orderRepository, Mockito.never()).save(Mockito.any(Order.class));

    }

    @Test
    public void shouldThrowIdempotencyKeyConflictExceptionWhenDifferentUserIds(){
        UUID fixedIdempotencyKey = UUID.fromString("ee1e070d-8f4b-5a5c-9d1d-112843c10222");
        UUID fixedUserIdFromExistingOrder = UUID.fromString("ffc2550a-924c-4fce-9d8a-162843c10333");
        UUID fixedUserIdFromSecurityContext = UUID.fromString("ddc1230a-92ad-43ae-9d8a-162843c10444");
        UUID fixedProductId = UUID.fromString("acde070d-8c4c-4f0d-9d8a-162843c10333");

        CreateOrderRequest orderRequest = new CreateOrderRequest(
                List.of(
                        new CreateOrderItemRequest(fixedProductId, 5)
                )
        );
        Order existingOrder = new Order(
                fixedUserIdFromExistingOrder,
                fixedIdempotencyKey,
                Instant.now().plusSeconds(60*8)
        );

        Mockito.when(orderRepository.findByIdempotencyKey(Mockito.any(UUID.class)))
                .thenReturn(Optional.of(existingOrder));

        Assertions.assertThrows(
                IdempotencyKeyConflictException.class,
                () -> orderService.createOrder(orderRequest, fixedIdempotencyKey, fixedUserIdFromSecurityContext));

        Mockito.verify(orderRepository).findByIdempotencyKey(fixedIdempotencyKey);
        Mockito.verify(productService, Mockito.never()).getProductById(Mockito.any(UUID.class));
        Mockito.verify(inventoryService, Mockito.never()).getInventoryByProductId(Mockito.any(UUID.class));
        Mockito.verify(inventoryService, Mockito.never()).reserve(Mockito.any(UUID.class),Mockito.anyInt());
        Mockito.verify(orderRepository, Mockito.never()).save(Mockito.any(Order.class));

    }
}
