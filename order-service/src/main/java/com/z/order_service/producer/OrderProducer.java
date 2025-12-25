package com.z.order_service.producer;

import com.z.shop.common.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class OrderProducer {

    @Autowired
    private KafkaTemplate<String, Order> kafkaTemplate;

    public void send(Order order) {
        kafkaTemplate.send(
            "order-create-topic",
            order.getSkuId(),
            order
        );
    }
}
