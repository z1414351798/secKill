package com.z.order_service.consumer;

import com.z.order_service.mapper.OrderMapper;
import com.z.order_service.producer.RefundProducer;
import org.apache.commons.collections.OrderedMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class InventoryDeductResultConsumer {
    @Autowired
    OrderMapper orderMapper;
    @Autowired
    RefundProducer refundProducer;

    @KafkaListener(topics = "inventory-deduct-result-topic", groupId = "order-group", containerFactory = "kafkaListenerContainerFactory")
    public void consume(Map<String, Object> msg, Acknowledgment ack) {
        Long orderId = (Long)(msg.get("orderId"));
        boolean success = (boolean)msg.get("success");

        if (success){
            if (!orderMapper.updateDeductSuccess(orderId)){
                System.out.println("update deduct success failed, orderId: " + orderId);
                ack.acknowledge();
                return;
            }
        } else {
            if (!orderMapper.updateDeductFail(orderId)){
                System.out.println("update deduct fail failed, orderId: " + orderId);
                ack.acknowledge();
                return;
            }
            Map<String, Object> refundMsg = new HashMap<>();
            refundMsg.put("orderId",orderId);
            refundProducer.send(refundMsg,orderId.toString());
        }
    }
}
