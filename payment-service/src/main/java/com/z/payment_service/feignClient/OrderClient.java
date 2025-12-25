package com.z.payment_service.feignClient;

import com.z.shop.common.Order;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.format.annotation.DateTimeFormat;
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
}