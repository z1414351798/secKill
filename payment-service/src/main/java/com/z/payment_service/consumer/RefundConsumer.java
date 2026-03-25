package com.z.payment_service.consumer;

import com.z.shop.common.Payment;
import com.z.payment_service.feignClient.OrderClient;
import com.z.payment_service.mapper.PaymentMapper;
import com.z.payment_service.producer.RefundResultProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Component
public class RefundConsumer {
    @Autowired
    PaymentMapper paymentMapper;
    @Autowired
    OrderClient orderClient;
    @Autowired
    RefundResultProducer refundResultProducer;

    @KafkaListener( topics = "refund-topic",
            groupId = "payment-group",
            containerFactory = "kafkaListenerContainerFactory")
    @Transactional
    public void refund(Map<String, Object> msg, Acknowledgment ack) {
        Long orderId = (Long) msg.get("orderId");
        Long paymentId = (Long) msg.get("paymentId");

        // 1️⃣ 查询支付记录，判断当前状态
        Payment payment = paymentMapper.selectByOrderIdAndPaymentId(orderId, paymentId);

        if ("REFUND_SUCCESS".equals(payment.getStatus())) {
            ack.acknowledge(); // 如果退款成功，直接返回
            return;
        }

        if ("REFUND_PROCESSING".equals(payment.getStatus())) {
            throw new RuntimeException("retry later"); // 如果正在退款，抛异常让 Kafka 自动重试
        }

        // 2️⃣ 尝试更新状态
        boolean updated = paymentMapper.markRefundProcessing(orderId, paymentId);
        if (!updated) {
            throw new RuntimeException("retry later"); // 更新失败，抛出异常让 Kafka 重试
        }

        // 3️⃣ 执行退款业务
        boolean refundSuccess = doBusiness(paymentId);

        // 4️⃣ 更新退款结果
        if (refundSuccess) {
            paymentMapper.markRefundSuccess(orderId, paymentId);
        } else {
            paymentMapper.markRefundFail(orderId, paymentId);
            throw new RuntimeException("Refund failed, trigger retry");
        }

        // 5️⃣ 发消息
        Map<String, Object> refundResultMsg = new HashMap<>();
        refundResultMsg.put("orderId", orderId);
        refundResultMsg.put("paymentId", paymentId);
        refundResultMsg.put("success", refundSuccess);

        refundResultProducer.send(refundResultMsg, orderId.toString());

        ack.acknowledge();
    }

    private boolean doBusiness(long id) {
        // 模拟支付 / 撮合 / 风控
        return Math.random() > 0.5; // 50% 成功
    }
}
