package com.z.payment_service.feignClient;


import com.z.shop.common.InventoryDeductLog;
import com.z.shop.common.Order;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "inventory-service")
public interface InventoryClient {
    @GetMapping("/inventory/getDeductLog")
    InventoryDeductLog getDeductLog(@RequestParam Long orderId, @RequestParam String skuId);
}
