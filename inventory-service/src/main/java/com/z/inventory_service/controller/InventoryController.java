package com.z.inventory_service.controller;


import com.z.inventory_service.mapper.InventoryDeductLogMapper;
import com.z.inventory_service.mapper.InventoryMapper;
import com.z.shop.common.Inventory;
import com.z.shop.common.InventoryDeductLog;
import com.z.shop.common.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/inventory")
public class InventoryController {

    @Autowired
    private InventoryMapper inventoryMapper;

    @Autowired
    private InventoryDeductLogMapper inventoryDeductLogMapper;

    @GetMapping("/all")
    public List<Inventory> findAll() {
        return inventoryMapper.findAll();
    }

    @GetMapping("/getDeductLog")
    public InventoryDeductLog getDeductLog(@RequestParam Long orderId, @RequestParam String skuId){
        return inventoryDeductLogMapper.selectByOrderIdAndSkuId(orderId, skuId);
    }

    @PostMapping("/addDeductLog")
    public int addDeductLog(@RequestBody InventoryDeductLog inventoryDeductLog){
        return inventoryDeductLogMapper.insertIgnore(inventoryDeductLog);
    }
}
