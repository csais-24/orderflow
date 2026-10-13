package com.cnom.orderflow.orderexpiration.service;

import java.util.UUID;

public interface OrderExpirationService {
    void expireOrder(UUID orderId);
}
