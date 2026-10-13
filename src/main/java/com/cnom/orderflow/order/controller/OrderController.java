package com.cnom.orderflow.order.controller;

import com.cnom.orderflow.order.dto.CreateOrderRequest;
import com.cnom.orderflow.order.dto.OrderResponse;
import com.cnom.orderflow.order.service.OrderService;
import com.cnom.orderflow.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping(path = "/api/v1/orders")
public class OrderController {

    private final OrderService orderService;
    private final UserService userService;

    public OrderController(OrderService orderService, UserService userService){

        this.orderService = orderService;
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(
            @Valid @RequestBody CreateOrderRequest createOrderRequest,
            @RequestHeader(value = "Idempotency-Key") UUID idempotencyKey,
            Authentication authentication
    ) {
        String userEmail = authentication.getName();
        UUID userId = userService.getUserByEmail(userEmail).id();
        OrderResponse orderResponse = orderService.createOrder(createOrderRequest, idempotencyKey, userId);
        return new ResponseEntity<>(orderResponse, HttpStatus.CREATED);
    }
}
