package com.z.inventory_service.consumer;
import com.z.inventory_service.feignClient.OrderClient;
import com.z.inventory_service.mapper.InventoryMapper;
import com.z.inventory_service.mapper.InventoryStockLogMapper;
import com.z.inventory_service.producer.InventoryDeductResultProducer;
import com.z.outbox.service.OutboxService;
import com.z.shop.common.FastSnowflakeIdGenerator;
import com.z.shop.common.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;


public class InventoryRollbackConsumer {

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

    @Autowired
    private OrderClient orderClient;

    private static final int MAX_RETRY = 5;

    @Transactional
    @KafkaListener(topics = "inventory-rollback-topic",
            groupId = "inventory-group",
            containerFactory = "kafkaListenerContainerFactory")
    public void rollbackStock(Map<String, Object> msg, Acknowledgment ack) {

        String skuId = (String) msg.get("skuId");
        int qty = (int) msg.get("qty");
        Long orderId = (Long) msg.get("orderId");

        Order order = orderClient.findById(orderId);

        if ("FINAL_SUCCESS".equals(order.getStatus())) {
            ack.acknowledge();
            return;
        }

        // 1️⃣ 只有 SUCCESS 才能进入 rollback
        int init = inventoryStockLogMapper.markRollbackInit(orderId, skuId);
        if (init == 0) {
            ack.acknowledge();
            return;
        }

        // 2️⃣ 抢执行权
        int processing = inventoryStockLogMapper.markRollbackProcessing(orderId, skuId);
        if (processing == 0) {
            ack.acknowledge();
            return;
        }

        boolean success = false;

        try {
            // 3️⃣ 回滚库存
            success = inventoryMapper.rollback(skuId, qty) > 0;

            if (success) {
                inventoryStockLogMapper.markRollbackSuccess(orderId, skuId);
            } else {
                inventoryStockLogMapper.markRollbackFail(orderId, skuId);

                int retry = inventoryStockLogMapper.incrementRollbackRetry(orderId, skuId, MAX_RETRY);

                if (retry == 0) {
                    inventoryStockLogMapper.markRollbackFailMaxRetry(orderId, skuId, MAX_RETRY);
                }
            }

        } catch (Exception e) {
            inventoryStockLogMapper.markUnknownError(orderId, skuId);
            throw e;
        }

        // 4️⃣ 发结果
        outboxService.saveEvent(
                String.valueOf(orderId),
                "inventory-rollback-result-topic",
                Map.of("orderId", orderId, "success", success)
        );

        ack.acknowledge();
    }
}
