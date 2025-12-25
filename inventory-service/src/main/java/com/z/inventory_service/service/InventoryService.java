package com.z.inventory_service.service;

import com.z.inventory_service.feignClient.OrderClient;
import com.z.inventory_service.mapper.InventoryDeductLogMapper;
import com.z.inventory_service.mapper.InventoryMapper;
import com.z.shop.common.InventoryDeductLog;
import com.z.shop.common.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class InventoryService {

    @Autowired
    private InventoryMapper inventoryMapper;
    @Autowired
    private InventoryDeductLogMapper inventoryDeductLogMapper;
    @Autowired
    private KafkaTemplate<String, Order> kafkaTemplate;
    @Autowired
    private OrderClient orderClient;


    @KafkaListener( topics = "inventory-deduct-topic",
                    groupId = "inventory-group",
                    containerFactory = "kafkaListenerContainerFactory")
    @Transactional
    public void inventoryDeduct(Map<String, Object> msg, Acknowledgment ack) {

        String skuId = (String) msg.get("skuId");
        int qty = (int) msg.get("qty");
        Long orderId = (Long) msg.get("orderId");

        InventoryDeductLog inventoryDeductLog = new InventoryDeductLog();
        inventoryDeductLog.setOrderId(orderId);
        inventoryDeductLog.setSkuId(skuId);
        inventoryDeductLog.setQty(qty);
        int inserted = inventoryDeductLogMapper.insertIgnore(inventoryDeductLog);
        if (inserted == 0) {
            // 已经被其他线程/consumer处理过
            ack.acknowledge();
            return;
        }

        inventoryMapper.deduct(skuId, qty);

        ack.acknowledge();
        System.out.println("Inventory deduct success skuId: "+ skuId + " qty: "+ qty);
    }

}
