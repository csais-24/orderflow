package com.cnom.orderflow.user.dto;

import java.time.Instant;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String email,
        Instant createdAt,
        Instant updatedAt,
        boolean active
) {
}
