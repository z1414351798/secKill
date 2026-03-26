package com.z.inventory_service.consumer;

import com.z.inventory_service.mapper.InventoryMapper;
import com.z.inventory_service.mapper.InventoryStockLogMapper;
import com.z.outbox.service.OutboxService;
import com.z.shop.common.FastSnowflakeIdGenerator;
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
public class InventoryDeductConsumer {

    private static final Logger log = LoggerFactory.getLogger(InventoryDeductConsumer.class);
    private static final int MAX_RETRY = 5;

    @Autowired
    private InventoryMapper inventoryMapper;

    @Autowired
    private InventoryStockLogMapper inventoryStockLogMapper;

    @Autowired
    private OutboxService outboxService;

    @Autowired
    private FastSnowflakeIdGenerator idGenerator;

    @KafkaListener(
            topics = "inventory-deduct-topic",
            groupId = "inventory-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    @Transactional
    public void deductStock(Map<String, Object> msg, Acknowledgment ack) {

        String skuId = (String) msg.get("skuId");
        int qty = (int) msg.get("qty");
        Long orderId = (Long) msg.get("orderId");

        // 1️⃣ 幂等插入（依赖唯一索引 order_id + sku_id）
        inventoryStockLogMapper.insertIgnore(
                idGenerator.nextId(),
                orderId,
                skuId,
                qty
        );

        // 2️⃣ 抢执行权
        int updated = inventoryStockLogMapper.markDeductProcessing(orderId, skuId);

        if (updated == 0) {
            // 已处理 or 正在处理中 → 直接 ACK
            ack.acknowledge();
            return;
        }

        // 3️⃣ 扣库存（原子操作）
        boolean deductSuccess = inventoryMapper.deduct(skuId, qty);

        if (deductSuccess) {

            // 4️⃣ 更新日志状态
            int updatedRows = inventoryStockLogMapper.markDeductSuccess(orderId, skuId);

            if (updatedRows <= 0) {
                throw new RuntimeException("mark deduct success failed");
            }

            // 5️⃣ 写 outbox（与事务绑定）
            outboxService.saveEvent(
                    String.valueOf(orderId),
                    "inventory-deduct-result-topic",
                    Map.of("orderId", orderId, "success", true)
            );

            // ✅ ACK after commit
            ackAfterCommit(ack);

        } else {

            // 6️⃣ 标记失败
            inventoryStockLogMapper.markDeductFail(orderId, skuId);

            int retry = inventoryStockLogMapper.incrementDeductRetry(orderId, skuId, MAX_RETRY);

            if (retry == 0) {
                // 达到最大重试次数

                int updatedRows = inventoryStockLogMapper
                        .markDeductFailMaxRetry(orderId, skuId, MAX_RETRY);

                if (updatedRows <= 0) {
                    throw new RuntimeException("mark fail max retry failed");
                }

                // 发送失败结果
                outboxService.saveEvent(
                        String.valueOf(orderId),
                        "inventory-deduct-result-topic",
                        Map.of("orderId", orderId, "success", false)
                );

                // 发送死信
                outboxService.saveEvent(
                        String.valueOf(orderId),
                        "inventory-deduct-dead-letter-topic",
                        msg
                );

                // ✅ ACK after commit
                ackAfterCommit(ack);

            } else {
                // 继续重试（不 ACK）
                log.warn("Deduct failed, retrying orderId={}", orderId);
                throw new RuntimeException("deduct retry");
            }
        }
    }

    /**
     * 保证 ACK 在事务提交之后执行
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