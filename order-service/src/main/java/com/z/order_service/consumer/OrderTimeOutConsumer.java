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
public class OrderTimeOutConsumer
        implements RocketMQListener<Order> {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;
    @Autowired
    private RedisStockService redisStockService;
    @Autowired
    private OutboxService outboxService;

    @Override
    @Transactional
    public void onMessage(Order order) {
        Long orderId = order.getOrderId();

        Order dbOrder = orderMapper.findById(orderId);
        String status = dbOrder.getStatus();
        String skuId = dbOrder.getSkuId();
        int qty = dbOrder.getQty();

        // CAS cancel
        int ok = orderMapper.timeoutCancel(orderId);
        if (ok == 0) return;

        boolean init = orderMapper.markRollbackInit(orderId);
        if (!init) return;

        boolean processing = orderMapper.markRollbackProcessing(orderId);
        if (!processing) return;

        redisStockService.rollback(orderId,skuId,qty);

        // If DB deduct likely happened → rollback DB
        if (status.equals("DEDUCT_SUCCESS")
                || status.equals("CAN_PAY")
                || status.startsWith("PAY_")) {

            outboxService.saveEvent(
                    String.valueOf(orderId),
                    "inventory-rollback-topic",
                    Map.of("orderId", orderId, "skuId", skuId, "qty", qty)
            );
        }
    }
}
