package com.z.order_service.consumer;

import com.z.order_service.enums.DeductResult;
import com.z.order_service.feignClient.InventoryClient;
import com.z.order_service.mapper.OrderMapper;
import com.z.order_service.producer.InventoryDeductProducer;
import com.z.order_service.service.RedisStockService;
import com.z.shop.common.InventoryDeductLog;
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
    private InventoryDeductProducer inventoryDeductProducer;
    @Autowired
    private InventoryClient inventoryClient;

    @KafkaListener(topics = "payment-result-topic", groupId = "order-group", containerFactory = "kafkaListenerContainerFactory")
    public void consume(Map<String, Object> msg, Acknowledgment ack) {
        Long orderId = (Long)(msg.get("orderId"));
        boolean success = (boolean)msg.get("success");
        String skuId = msg.get("skuId").toString();
        int qty = (int)msg.get("qty");

        if (success) {
            if (!orderMapper.updatePaySuccess(orderId)){
                System.out.println("Order update PAY_SUCCESS failed, orderId : "+ orderId);
                ack.acknowledge();
                return;
            }
            DeductResult confirm = redisStockService.confirm(orderId, skuId, qty);
            HashMap<String,Object> map = new HashMap<>();
            map.put("skuId",skuId);
            map.put("qty", qty);
            map.put("orderId",orderId);
            inventoryDeductProducer.send(map,skuId);
            System.out.println("orderId: "+orderId+"confirm result: " + confirm);
        } else {

            if (!orderMapper.updatePayFAILED(orderId)){
                System.out.println("Order update PAY_FAILED failed, orderId : "+ orderId);
                ack.acknowledge();
                return;
            }
            System.out.println("Order update PAY_FAILED success, orderId : "+ orderId);
        }
        ack.acknowledge();
    }
}
