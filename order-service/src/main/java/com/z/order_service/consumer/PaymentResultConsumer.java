package com.z.order_service.consumer;

import com.alibaba.fastjson2.JSON;
import com.z.order_service.enums.DeductResult;
import com.z.order_service.feignClient.InventoryClient;
import com.z.order_service.feignClient.PaymentClient;
import com.z.order_service.mapper.OrderMapper;
import com.z.order_service.service.RedisStockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class PaymentResultConsumer {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private RedisStockService redisStockService;
    @Autowired
    private InventoryClient inventoryClient;

    @KafkaListener(topics = "payment-result-topic", groupId = "order-group", containerFactory = "kafkaListenerContainerFactory")
    public void consume(String msg, Acknowledgment ack) {
        Map<String, Object> map = JSON.parseObject(msg);
        Long  orderId = ((Number) map.get("orderId")).longValue();
        boolean success = (boolean) map.get("success");
        String skuId = map.get("skuId").toString();
        int qty = (int)map.get("qty");

        if (success) {
            if (!orderMapper.markPaySuccess(orderId)){
                System.out.println("Order update PAY_SUCCESS failed, orderId : "+ orderId);
                ack.acknowledge();
                return;
            }
            DeductResult confirm = redisStockService.confirm(orderId, skuId, qty);
            boolean markFinalSuccess = orderMapper.markFinalSuccess(orderId);

        } else {

            if (!orderMapper.markPayFAIL(orderId)){
                System.out.println("Order update PAY_FAILED failed, orderId : "+ orderId);
                ack.acknowledge();
                return;
            }
            System.out.println("Order update PAY_FAILED success, orderId : "+ orderId);
        }
        ack.acknowledge();
    }
}
