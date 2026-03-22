package com.z.order_service.consumer;

import com.z.order_service.mapper.OrderMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class RefundResultConsumer {
    @Autowired
    OrderMapper orderMapper;

    @KafkaListener(topics = "refund-result-topic", groupId = "order-group", containerFactory = "kafkaListenerContainerFactory")
    public void consume(Map<String, Object> msg, Acknowledgment ack) {
        Long orderId = (Long) msg.get("orderId");
        boolean success = (boolean) msg.get("success");

        if (success){
            orderMapper.updateRefundSuccess(orderId);
        } else {
            orderMapper.updateRefundFailed(orderId);
        }
        ack.acknowledge();
    }
}
