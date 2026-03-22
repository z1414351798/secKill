package com.z.inventory_service.consumer;

import com.z.inventory_service.feignClient.OrderClient;
import com.z.inventory_service.mapper.InventoryDeductLogMapper;
import com.z.inventory_service.mapper.InventoryMapper;
import com.z.inventory_service.producer.InventoryDeductResultProducer;
import com.z.shop.common.FastSnowflakeIdGenerator;
import com.z.shop.common.InventoryDeductLog;
import com.z.shop.common.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
public class InventoryDeductConsumer {

    @Autowired
    private InventoryMapper inventoryMapper;
    @Autowired
    private InventoryDeductLogMapper inventoryDeductLogMapper;
    @Autowired
    private KafkaTemplate<String, Order> kafkaTemplate;
    @Autowired
    private OrderClient orderClient;
    @Autowired
    private InventoryDeductResultProducer inventoryDeductResultProducer;
    @Autowired
    private FastSnowflakeIdGenerator fastSnowflakeIdGenerator;


    @Transactional
    @KafkaListener(
            topics = "inventory-deduct-topic",
            groupId = "inventory-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void inventoryDeduct(Map<String, Object> msg, Acknowledgment ack) {

        String skuId = (String) msg.get("skuId");
        int qty = (int) msg.get("qty");
        Long orderId = (Long) msg.get("orderId");

        // 1️⃣ 直接 insert（幂等）
        InventoryDeductLog newLog = new InventoryDeductLog();
        newLog.setId(fastSnowflakeIdGenerator.nextId());
        newLog.setOrderId(orderId);
        newLog.setSkuId(skuId);
        newLog.setQty(qty);
        newLog.setStatus("DEDUCT_INIT");
        inventoryDeductLogMapper.insertIgnore(newLog);

        // 2️⃣ 抢执行权（核心！！！）
        boolean updated = inventoryDeductLogMapper.markProcessing(orderId, skuId);

        if (!updated) {
            InventoryDeductLog log = inventoryDeductLogMapper.selectByOrderIdAndSkuId(orderId, skuId);
            if ("DEDUCT_SUCCESS".equals(log.getStatus())) {
                ack.acknowledge();
                return;
            }
            throw new RuntimeException("retry"); // Kafka 重试
        }

        // 3️⃣ 扣库存
        boolean success = inventoryMapper.deduct(skuId, qty);

        // 4️⃣ 更新状态
        if (success) {
            inventoryDeductLogMapper.markSuccess(orderId, skuId);
        } else {
            inventoryDeductLogMapper.markFail(orderId, skuId);
        }

        // 5️⃣ 发消息
        Map<String, Object> deductResultMsg = new HashMap<>();
        deductResultMsg.put("success", success);
        deductResultMsg.put("orderId", orderId);
        deductResultMsg.put("skuId", skuId);
        deductResultMsg.put("qty", qty);
        inventoryDeductResultProducer.send(deductResultMsg, orderId.toString());

        // 6️⃣ ack
        ack.acknowledge();
    }

}
