package com.z.order_service.producer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class InventoryDeductProducer {

    @Autowired
    private KafkaTemplate<String, Map<String, Object>> kafkaTemplate;

    public void send(Map<String, Object> msg, String skuId) {
        kafkaTemplate.send(
            "inventory-deduct-topic",
                skuId,
                msg
        );
    }
}
