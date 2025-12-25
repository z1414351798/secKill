package com.z.order_service.producer;

import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MessageProducer {
    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    public String sendMessage(String topic, String message) {
        rocketMQTemplate.convertAndSend(topic, message);
        return "success";
    }
}
