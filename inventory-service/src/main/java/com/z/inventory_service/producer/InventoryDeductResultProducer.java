package com.z.inventory_service.producer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class InventoryDeductResultProducer {
    @Autowired
    private KafkaTemplate<String, Map<String, Object>> kafkaTemplate;

    public void send(Map<String, Object> msg, String orderId) {
        kafkaTemplate.send(
                "inventory-deduct-result-topic",
                orderId,
                msg
        );
    }
}
