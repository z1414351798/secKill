package com.z.order_service.feignClient;

import com.z.shop.common.Inventory;
import com.z.shop.common.InventoryDeductLog;
import com.z.shop.common.Order;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "inventory-service" )
public interface InventoryClient {

    @GetMapping("/inventory-service/inventory/all")
    List<Inventory> findAll();

    @GetMapping("/inventory-service/inventory/getDeductLog")
    InventoryDeductLog getDeductLog(@RequestParam Long orderId, @RequestParam String skuId);

    @PostMapping("/inventory-service/inventory/addDeductLog")
    int addDeductLog(@RequestBody InventoryDeductLog inventoryDeductLog);
}
