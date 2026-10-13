package com.cnom.orderflow.order.scheduler;

import com.cnom.orderflow.order.service.OrderService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class OrderExpirationScheduler {

    private final OrderService orderService;

    public OrderExpirationScheduler(OrderService orderService){
        this.orderService = orderService;
    }

    @Scheduled(fixedDelay = 5000)
    public void verifyExpiration(){
        orderService.expirePendingOrders();
    }
}
