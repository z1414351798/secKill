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
    private OrderMapper orderMapper;

    @KafkaListener(
            topics = "refund-result-topic",
            groupId = "order-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consume(Map<String, Object> msg, Acknowledgment ack) {

        Long orderId = (Long) msg.get("orderId");
        boolean success = (boolean) msg.get("success");

        if (success) {
            int ok = orderMapper.markRefundSuccess(orderId);
            if (ok == 0) {
                System.out.println("Refund success update failed, orderId=" + orderId);
            }
        } else {
            int ok = orderMapper.markRefundFail(orderId);
            if (ok == 0) {
                System.out.println("Refund fail update failed, orderId=" + orderId);
            }
        }

        ack.acknowledge();
    }
}
