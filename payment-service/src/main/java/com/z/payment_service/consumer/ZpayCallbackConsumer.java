package com.z.payment_service.consumer;

import com.alibaba.fastjson2.JSON;
import com.z.payment_service.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;

@Component
public class ZpayCallbackConsumer {

    @Autowired
    private PaymentService paymentService;

    @KafkaListener(
            topics = "zpay-callback-topic",
            groupId = "payment-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void callback(String msg, Acknowledgment ack){
        Map<String, Object> map = JSON.parseObject(msg);
        Long orderId = (Long) map.get("orderId");
        String status = (String) map.get("status");

        paymentService.process(orderId, status);

        ack.acknowledge();
    }
}
