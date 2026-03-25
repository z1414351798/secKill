package com.z.order_service.consumer;

import com.z.order_service.mapper.OrderMapper;
import com.z.outbox.task.OutboxSendTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class InventoryDeductResultConsumer {

    @Autowired
    private OrderMapper orderMapper;

    private static final Logger log = LoggerFactory.getLogger(InventoryDeductResultConsumer.class);

    @KafkaListener(
            topics = "inventory-deduct-result-topic",
            groupId = "order-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consume(Map<String, Object> msg, Acknowledgment ack) {
        Long orderId = (Long) msg.get("orderId");
        boolean success = (boolean) msg.get("success");

        try {
            if (success) {
                // 1️⃣ 扣减成功
                boolean deductSuccess = orderMapper.markDeductSuccess(orderId);

                if (!deductSuccess) {
                    // 如果已处理或正在处理中，跳过
                    ack.acknowledge();
                    return;
                }

                // 2️⃣ 更新为可支付
                boolean canPay = orderMapper.markCanPay(orderId);

                if (!canPay) {
                    // 如果已处理或正在处理中，跳过
                    ack.acknowledge();
                    return;
                }

            } else {
                // 3️⃣ 扣减失败
                boolean deductFail = orderMapper.markDeductFail(orderId);
                if (!deductFail) {
                    // 如果已处理或正在处理中，跳过
                    ack.acknowledge();
                    return;
                }
                // 不需要退款逻辑，只需记录扣减失败
            }
        } catch (Exception e) {
            // 记录日志
            log.error("处理库存扣减结果失败，orderId={}", orderId, e);
        } finally {
            // 确保每次处理完都ack
            ack.acknowledge();
        }
    }
}