package com.z.inventory_service.feignClient;

import com.z.shop.common.Order;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;

@FeignClient(name = "order-service")
public interface OrderClient {

    @PostMapping("/order/markPaid")
    int markPaid(@RequestParam Long orderId);

    @GetMapping("/order/findById")
    public Order findById(@RequestParam Long orderId);
}