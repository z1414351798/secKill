package com.z.payment_service.producer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class RefundResultProducer {

    @Autowired
    private KafkaTemplate<String, Map<String, Object>> kafkaTemplate;

    public void send(Map<String, Object> msg, String orderId) {
        kafkaTemplate.send(
                "refund-result-topic",
                orderId,
                msg
        );
    }
}
