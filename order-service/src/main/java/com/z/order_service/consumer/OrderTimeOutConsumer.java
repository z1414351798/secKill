package com.z.order_service.consumer;

import com.z.order_service.enums.DeductResult;
import com.z.order_service.mapper.OrderMapper;
import com.z.order_service.service.RedisStockService;
import com.z.outbox.service.OutboxService;
import com.z.shop.common.Order;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Component
@RocketMQMessageListener(
        topic = "order-timeout-topic",
        consumerGroup = "order-timeout-group"
)
public class OrderTimeOutConsumer implements RocketMQListener<Order> {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private RedisStockService redisStockService;

    @Autowired
    private OutboxService outboxService;

    @Override
    @Transactional
    public void onMessage(Order order) {

        Long orderId = order.getOrderId();

        // 1️⃣ CAS cancel (only one thread succeeds)
        int ok = orderMapper.timeoutCancel(orderId);
        if (ok == 0) return;

        // 2️⃣ get latest data AFTER CAS
        Order dbOrder = orderMapper.findById(orderId);
        String status = dbOrder.getStatus();
        String skuId = dbOrder.getSkuId();
        int qty = dbOrder.getQty();

        // 3️⃣ rollback Redis immediately
        redisStockService.rollback(orderId, skuId, qty);

        // 4️⃣ try enter rollback state machine
        if (!orderMapper.markRollbackInit(orderId)) {
            return;
        }

        if (!orderMapper.markRollbackProcessing(orderId)) {
            return;
        }

        // 5️⃣ trigger DB rollback ONLY if needed
        if (needRollbackDB(status)) {
            outboxService.saveEvent(
                    String.valueOf(orderId),
                    "inventory-rollback-topic",
                    Map.of(
                            "orderId", orderId,
                            "skuId", skuId,
                            "qty", qty
                    )
            );
        }
    }

    private boolean needRollbackDB(String status) {
        return "DEDUCT_SUCCESS".equals(status)
                || "CAN_PAY".equals(status)
                || status.startsWith("PAY_");
    }
}