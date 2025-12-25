package com.z.order_service.producer;


import com.z.shop.common.Order;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

@Component
public class OrderTimeoutProducer {

    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    // 30 分钟 = delayLevel 18
    public void sendDelay(Order order) {
        rocketMQTemplate.syncSend(
                "order-timeout-topic",
                MessageBuilder.withPayload(order).build(),
                3000,
                9 //5mins
        );
    }
}
