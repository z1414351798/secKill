package com.z.payment_service.producer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class PaymentResultProducer {

    @Autowired
    private KafkaTemplate<String, Map<String, Object>> kafkaTemplate;

    public void send(Map<String, Object> msg, String orderId) {


        kafkaTemplate.send(
            "payment-result-topic",
                orderId,
            msg
        );
    }
}
