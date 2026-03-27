package com.z.payment_service.service;

import com.z.outbox.service.OutboxService;
import com.z.payment_service.feignClient.OrderClient;
import com.z.payment_service.mapper.PaymentMapper;
import com.z.shop.common.Order;
import com.z.shop.common.Payment;
import com.z.shop.common.PaymentDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);
    private static final int MAX_RETRY = 5;

    @Autowired
    private PaymentMapper paymentMapper;

    @Autowired
    private OutboxService outboxService;

    @Autowired
    OrderClient orderClient;

    @Transactional
    public void process(Long orderId, String status) {

        Order order = orderClient.findById(orderId);

        // 1️⃣ 查询支付记录
        Payment payment = paymentMapper.selectByOrderId(orderId);
        Long paymentId = payment.getPaymentId();
        if (payment == null) {
            throw new RuntimeException("payment not found");
        }

        // 2️⃣ 幂等控制
        if ("PAY_SUCCESS".equals(payment.getStatus())) {
            return; // 已成功，直接返回
        }

        boolean success = "SUCCESS".equals(status);

        if (success) {

            // 🚨 critical check
            if (order.getStatus().equals("TIME_OUT_CANCEL")
                    || order.getStatus().startsWith("ROLLBACK")) {

                // 👉 trigger refund
                paymentMapper.markRefundProcessing(orderId, paymentId);

                outboxService.saveEvent(
                        String.valueOf(orderId),
                        "refund-topic",
                        Map.of("orderId", orderId, "paymentId", paymentId)
                );

                return;
            }

            // 3️⃣ 标记成功
            boolean updated = paymentMapper.markPaySuccess(orderId);
            if (!updated) {
                throw new RuntimeException("mark pay success failed");
            }

            // 4️⃣ outbox（事务内）
            outboxService.saveEvent(
                    String.valueOf(orderId),
                    "payment-result-topic",
                    Map.of(
                            "orderId", orderId,
                            "success", true,
                            "skuId", payment.getSkuId(),
                            "qty", payment.getQty()
                    )
            );

        } else {

            // 5️⃣ 标记失败
            boolean failUpdated = paymentMapper.markPayFail(orderId);
            if (!failUpdated) {
                throw new RuntimeException("mark pay fail failed");
            }

            // 6️⃣ 增加重试次数
            int retry = paymentMapper.incrementPayRetry(orderId, MAX_RETRY);

            if (retry == 0) {
                // 达到最大重试次数

                boolean maxUpdated = paymentMapper.markPayFailMaxRetry(orderId, MAX_RETRY);
                if (!maxUpdated) {
                    throw new RuntimeException("mark pay fail max retry failed");
                }

                // 发送最终失败事件
                outboxService.saveEvent(
                        String.valueOf(orderId),
                        "payment-result-topic",
                        Map.of(
                                "orderId", orderId,
                                "success", false,
                                "skuId", payment.getSkuId(),
                                "qty", payment.getQty()
                        )
                );

                // 可选：死信
                outboxService.saveEvent(
                        String.valueOf(orderId),
                        "payment-dead-letter-topic",
                        Map.of(
                                "orderId", orderId,
                                "status", status
                        )
                );

            } else {
                // ❗ 触发重试（关键）
                log.warn("Payment failed, retrying orderId={}", orderId);
                throw new RuntimeException("payment retry");
            }
        }
    }
}