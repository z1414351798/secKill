package com.z.inventory_service.consumer;

import com.alibaba.fastjson2.JSON;
import com.z.inventory_service.feignClient.OrderClient;
import com.z.inventory_service.mapper.InventoryMapper;
import com.z.inventory_service.mapper.InventoryStockLogMapper;
import com.z.outbox.service.OutboxService;
import com.z.shop.common.FastSnowflakeIdGenerator;
import com.z.shop.common.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.Map;

@Service
public class InventoryRollbackConsumer {

    private static final Logger log = LoggerFactory.getLogger(InventoryRollbackConsumer.class);
    private static final int MAX_RETRY = 5;

    @Autowired
    private InventoryMapper inventoryMapper;

    @Autowired
    private InventoryStockLogMapper inventoryStockLogMapper;

    @Autowired
    private OutboxService outboxService;

    @Autowired
    private OrderClient orderClient;

    @KafkaListener(
            topics = "inventory-rollback-topic",
            groupId = "inventory-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    @Transactional
    public void rollbackStock(String msg, Acknowledgment ack) {

        Map<String, Object> map = JSON.parseObject(msg);
        String skuId = (String) map.get("skuId");
        int qty = (int) map.get("qty");
        Long orderId = ((Number) map.get("orderId")).longValue();

        // 0️⃣ 防止“已最终成功订单”被回滚（重要保护）
        Order order = orderClient.findById(orderId);
        if (order != null && "FINAL_SUCCESS".equals(order.getStatus())) {
            ack.acknowledge();
            return;
        }

        // 1️⃣ 只有 DEDUCT_SUCCESS 才能进入 rollback
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

        // 3️⃣ 执行库存回滚
        boolean rollbackSuccess = inventoryMapper.rollback(skuId, qty) > 0;

        if (rollbackSuccess) {

            int updated = inventoryStockLogMapper.markRollbackSuccess(orderId, skuId);
            if (updated <= 0) {
                throw new RuntimeException("mark rollback success failed");
            }

            // 4️⃣ outbox（事务内）
            outboxService.saveEvent(
                    String.valueOf(orderId),
                    "inventory-rollback-result-topic",
                    Map.of("orderId", orderId, "success", true)
            );

            // ✅ ACK after commit
            ackAfterCommit(ack);

        } else {

            // 5️⃣ 标记失败
            inventoryStockLogMapper.markRollbackFail(orderId, skuId);

            int retry = inventoryStockLogMapper.incrementRollbackRetry(orderId, skuId, MAX_RETRY);

            if (retry == 0) {

                int updated = inventoryStockLogMapper
                        .markRollbackFailMaxRetry(orderId, skuId, MAX_RETRY);

                if (updated <= 0) {
                    throw new RuntimeException("mark rollback max retry failed");
                }

                // 失败结果
                outboxService.saveEvent(
                        String.valueOf(orderId),
                        "inventory-rollback-result-topic",
                        Map.of("orderId", orderId, "success", false)
                );

                // 死信
                outboxService.saveEvent(
                        String.valueOf(orderId),
                        "inventory-rollback-dead-letter-topic",
                        msg
                );

                // ✅ ACK after commit
                ackAfterCommit(ack);

            } else {
                log.warn("Rollback failed, retrying orderId={}", orderId);
                throw new RuntimeException("rollback retry");
            }
        }
    }

    /**
     * 确保 ACK 在事务提交之后执行
     */
    private void ackAfterCommit(Acknowledgment ack) {
        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        ack.acknowledge();
                    }
                }
        );
    }
}