package com.z.inventory_service.controller;


import com.z.inventory_service.mapper.InventoryStockLogMapper;
import com.z.inventory_service.mapper.InventoryMapper;
import com.z.shop.common.Inventory;
import com.z.shop.common.InventoryStockLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/inventory")
public class InventoryController {

    @Autowired
    private InventoryMapper inventoryMapper;

    @Autowired
    private InventoryStockLogMapper inventoryStockLogMapper;

    @GetMapping("/all")
    public List<Inventory> findAll() {
        return inventoryMapper.findAll();
    }


}
