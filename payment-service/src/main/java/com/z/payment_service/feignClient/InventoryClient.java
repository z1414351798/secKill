package com.z.payment_service.feignClient;


import com.z.shop.common.InventoryStockLog;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "inventory-service")
public interface InventoryClient {
    @GetMapping("/inventory/getDeductLog")
    InventoryStockLog getDeductLog(@RequestParam Long orderId, @RequestParam String skuId);
}
