package com.z.order_service.consumer;

import com.alibaba.fastjson2.JSON;
import com.z.order_service.mapper.OrderMapper;
import com.z.order_service.service.RedisStockService;
import com.z.shop.common.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class RollbackResultConsumer {

    @Autowired
    OrderMapper orderMapper;

    @Autowired
    private RedisStockService redisStockService;

    @KafkaListener(
            topics = "inventory-rollback-result-topic",
            groupId = "order-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consume(String msg, Acknowledgment ack) {
        Map<String, Object> map = JSON.parseObject(msg);
        Long  orderId = ((Number) map.get("orderId")).longValue();
        boolean success = (boolean) map.get("success");
        if (success){
            boolean markRollbackSuccess = orderMapper.markRollbackSuccess(orderId);
        } else {
            orderMapper.markRollbackFail(orderId);
        }
        ack.acknowledge();
    }
}
