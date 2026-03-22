package com.z.payment_service.service;

import com.z.payment_service.domain.Payment;
import com.z.payment_service.feignClient.InventoryClient;
import com.z.payment_service.feignClient.OrderClient;
import com.z.payment_service.mapper.PaymentMapper;
import com.z.payment_service.producer.PaymentResultProducer;
import com.z.shop.common.FastSnowflakeIdGenerator;
import com.z.shop.common.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PaymentService {

    @Autowired
    private PaymentMapper paymentMapper;

    @Autowired
    private PaymentResultProducer paymentResultProducer;
    @Autowired
    private OrderClient orderClient;
    @Autowired
    private InventoryClient inventoryClient;
    @Autowired
    private FastSnowflakeIdGenerator fastSnowflakeIdGenerator;


    @Transactional
    public Response<Payment> process(Payment payment) {

        Long orderId = payment.getOrderId();

        // 1️⃣ 幂等插入（只会成功一次）
        long paymentId = fastSnowflakeIdGenerator.nextId();
        payment.setPaymentId(paymentId);
        paymentMapper.insertPayInit(payment);

        // 2️⃣ 抢执行权（核心 CAS）
        boolean updated = paymentMapper.markPayProcessing(orderId);

        if (!updated) {
            // 没抢到执行权 → 查状态
            Payment exist = paymentMapper.selectByOrderId(orderId);

            if ("PAY_SUCCESS".equals(exist.getStatus())) {
                return Response.success(exist); // 幂等返回
            }

            if ("PAY_PROCESSING".equals(exist.getStatus())) {
                return Response.error("Payment is processing");
            }

            // FAIL → 可以重试（理论不会走到这里，因为FAIL也能抢）
            return Response.error("Retry later");
        }

        // 3️⃣ 执行支付（⚠️ paymentId作为幂等key）
        boolean success = doBusiness(paymentId);

        // 4️⃣ 更新状态
        if (success) {
            paymentMapper.markPaySuccess(orderId);
        } else {
            paymentMapper.markPayFail(orderId);
        }

        // 5️⃣ 发消息（最终结果）
        Map<String, Object> msg = new HashMap<>();
        msg.put("orderId", orderId);
        msg.put("paymentId", paymentId);
        msg.put("success", success);
        paymentResultProducer.send(msg, orderId.toString());

        return Response.success(payment);
    }

    private boolean doBusiness(long id) {
        // 模拟支付 / 风控 / 撮合逻辑
        return Math.random() > 0.5;
    }


}
