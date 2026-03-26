package com.z.payment_service.consumer;

import com.z.payment_service.mapper.PaymentMapper;
import com.z.outbox.service.OutboxService;
import com.z.shop.common.Payment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.Map;

@Component
public class RefundConsumer {

    private static final Logger log = LoggerFactory.getLogger(RefundConsumer.class);
    private static final int MAX_RETRY = 5;

    @Autowired
    private PaymentMapper paymentMapper;

    @Autowired
    private OutboxService outboxService;

    @KafkaListener(
            topics = "refund-topic",
            groupId = "payment-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    @Transactional
    public void refund(Map<String, Object> msg, Acknowledgment ack) {

        Long orderId = (Long) msg.get("orderId");
        Long paymentId = (Long) msg.get("paymentId");

        // 1️⃣ 查询支付记录
        Payment payment = paymentMapper.selectByOrderIdAndPaymentId(orderId, paymentId);

        if (payment == null) {
            ack.acknowledge();
            return;
        }

        // 2️⃣ 幂等判断
        if ("REFUND_SUCCESS".equals(payment.getStatus())) {
            ack.acknowledge();
            return;
        }

        if ("REFUND_PROCESSING".equals(payment.getStatus())) {
            throw new RuntimeException("refund processing, retry later");
        }

        // 3️⃣ 抢执行权
        boolean locked = paymentMapper.markRefundProcessing(orderId, paymentId);
        if (!locked) {
            throw new RuntimeException("failed to lock refund, retry");
        }

        // 4️⃣ 执行退款业务
        boolean refundSuccess = doBusiness(paymentId);

        if (refundSuccess) {

            boolean updated = paymentMapper.markRefundSuccess(orderId, paymentId);
            if (!updated) {
                throw new RuntimeException("mark refund success failed");
            }

            // 5️⃣ outbox（事务内）
            outboxService.saveEvent(
                    String.valueOf(orderId),
                    "refund-result-topic",
                    Map.of(
                            "orderId", orderId,
                            "paymentId", paymentId,
                            "success", true
                    )
            );

            // ✅ ACK after commit
            ackAfterCommit(ack);

        } else {

            paymentMapper.markRefundFail(orderId, paymentId);

            int retry = paymentMapper.incrementRefundRetry(orderId, paymentId, MAX_RETRY);

            if (retry == 0) {

                paymentMapper.markRefundFailMaxRetry(orderId, paymentId, MAX_RETRY);

                outboxService.saveEvent(
                        String.valueOf(orderId),
                        "refund-result-topic",
                        Map.of(
                                "orderId", orderId,
                                "paymentId", paymentId,
                                "success", false
                        )
                );

                outboxService.saveEvent(
                        String.valueOf(orderId),
                        "refund-dead-letter-topic",
                        msg
                );

                // ✅ ACK after commit
                ackAfterCommit(ack);

            } else {
                log.warn("Refund failed, retrying orderId={}", orderId);
                throw new RuntimeException("refund retry");
            }
        }
    }

    /**
     * 保证 ACK 在事务提交之后执行
     */
    private void ackAfterCommit(Acknowledgment ack) {
        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        ack.acknowledge();
                    }
                }
        );
    }

    private boolean doBusiness(long paymentId) {
        // 模拟退款
        return Math.random() > 0.3;
    }
}