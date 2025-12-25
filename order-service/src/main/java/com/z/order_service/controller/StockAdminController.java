package com.z.order_service.controller;

import com.z.order_service.feignClient.InventoryClient;
import com.z.shop.common.Inventory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/admin/stock")
public class StockAdminController {

    @Autowired
    private InventoryClient inventoryClient;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @PostMapping("/reload")
    public String reload() {

        List<Inventory> inventories = inventoryClient.findAll();

        inventories.forEach(inv -> {
            String skuId = inv.getSkuId();
            String tag = "{" + skuId + "}";

            // 1️⃣ 可用库存
            redisTemplate.opsForValue().set(
                    "stock:" + tag,
                    String.valueOf(inv.getAvailable())
            );

            // 2️⃣ 清空锁库存
            redisTemplate.delete("stock:lock:" + tag);
        });

        return "STOCK CACHE RELOADED, COUNT=" + inventories.size();
    }
}
