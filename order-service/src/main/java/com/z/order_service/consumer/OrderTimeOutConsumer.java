package com.z.order_service.consumer;

import com.z.order_service.enums.DeductResult;
import com.z.order_service.mapper.OrderMapper;
import com.z.order_service.service.RedisStockService;
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

    @Override
    @Transactional
    public void onMessage(Order order) {
        // 1️⃣ CAS 尝试进入 CANCELING
        int ok = orderMapper.timeoutCancel(order.getOrderId());
        if (ok == 0) {
            // 要么已支付，要么已被处理
            return;
        }
        // 2️⃣ 只有 CAS 成功的人，才回滚 Redis
        DeductResult rollback = redisStockService.rollback(
                order.getOrderId(),
                order.getSkuId(),
                order.getQty()
        );
        System.out.println("Timeout rollback orderId: " + order.getOrderId());
    }
}
