package com.z.payment_service.feignClient;

import com.z.shop.common.Order;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;

@FeignClient(name = "order-service")
public interface OrderClient {

    @PostMapping("/order/updateStatusAndPayingAtWhenInit")
    int updateStatusAndPayingAtWhenInit(@RequestParam Long orderId, @RequestParam String status, @RequestParam  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime payingAt);

    @PostMapping("/order/markPaid")
    int markPaid(@RequestParam Long orderId);

    @PostMapping("/order-service/order/updatePaying")
    boolean updatePaying(@RequestParam Long orderId);

    @PostMapping("/order-service/order/updateRefunding")
    boolean updateRefunding(@RequestParam Long orderId);

    @GetMapping("/order/findById")
    Order findById(@RequestParam Long orderId);
}