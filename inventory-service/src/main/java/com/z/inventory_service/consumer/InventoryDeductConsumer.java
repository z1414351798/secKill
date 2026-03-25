package com.z.inventory_service.consumer;

import com.z.inventory_service.mapper.InventoryMapper;
import com.z.inventory_service.mapper.InventoryStockLogMapper;
import com.z.inventory_service.producer.InventoryDeductResultProducer;
import com.z.outbox.service.OutboxService;
import com.z.shop.common.FastSnowflakeIdGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
public class InventoryDeductConsumer {

    @Autowired
    private InventoryMapper inventoryMapper;

    @Autowired
    private InventoryStockLogMapper inventoryStockLogMapper;

    @Autowired
    private InventoryDeductResultProducer inventoryDeductResultProducer;

    @Autowired
    private OutboxService outboxService;

    @Autowired
    private FastSnowflakeIdGenerator fastSnowflakeIdGenerator;

    private static final int MAX_RETRY = 5;

    @Transactional
    @KafkaListener(topics = "inventory-deduct-topic",
            groupId = "inventory-group",
            containerFactory = "kafkaListenerContainerFactory")
    public void deductStock(Map<String, Object> msg, Acknowledgment ack) {

        String skuId = (String) msg.get("skuId");
        int qty = (int) msg.get("qty");
        Long orderId = (Long) msg.get("orderId");

        // 1️⃣ 幂等插入
        inventoryStockLogMapper.insertIgnore(
                fastSnowflakeIdGenerator.nextId(),
                orderId, skuId, qty
        );

        // 2️⃣ 抢执行权（INIT / FAIL -> PROCESSING）
        int updated = inventoryStockLogMapper.markDeductProcessing(orderId, skuId);

        if (updated == 0) {
            ack.acknowledge();
            return; // 已被处理 or 正在处理
        }

        boolean success = false;

        try {
            // 3️⃣ 扣库存（原子）
            success = inventoryMapper.deduct(skuId, qty);

            if (success) {
                inventoryStockLogMapper.markDeductSuccess(orderId, skuId);
                outboxService.saveEvent(
                        String.valueOf(orderId),
                        "inventory-deduct-result-topic",
                        Map.of("orderId", orderId, "success", success)
                );

            } else {
                inventoryStockLogMapper.markDeductFail(orderId, skuId);

                int retry = inventoryStockLogMapper.incrementDeductRetry(orderId, skuId, MAX_RETRY);

                if (retry == 0) {
                    inventoryStockLogMapper.markDeductFailMaxRetry(orderId, skuId, MAX_RETRY);

                    outboxService.saveEvent(
                            String.valueOf(orderId),
                            "inventory-deduct-dead-letter-topic",
                            msg
                    );
                }
            }

        } catch (Exception e) {
            inventoryStockLogMapper.markUnknownError(orderId, skuId);
            throw e;
        }
        
        ack.acknowledge();
    }
}