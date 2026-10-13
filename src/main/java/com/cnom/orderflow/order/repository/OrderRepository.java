package com.cnom.orderflow.order.repository;

import com.cnom.orderflow.order.entity.Order;
import com.cnom.orderflow.order.entity.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {
    Optional<Order> findByIdempotencyKey(UUID idempotencyKey);
    List<Order> findByStatusAndExpiresAtLessThanEqual(
            OrderStatus status,
            Instant instant
    );
}
