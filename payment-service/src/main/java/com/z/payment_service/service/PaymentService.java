package com.z.payment_service.service;

import com.z.outbox.service.OutboxService;
import com.z.shop.common.Payment;
import com.z.payment_service.feignClient.InventoryClient;
import com.z.payment_service.feignClient.OrderClient;
import com.z.payment_service.mapper.PaymentMapper;
import com.z.payment_service.producer.PaymentResultProducer;
import com.z.shop.common.FastSnowflakeIdGenerator;
import com.z.shop.common.PaymentDto;
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
    @Autowired
    private OutboxService outboxService;


    @Transactional
    public void process(PaymentDto dto) {
        Long orderId = dto.getOrderId();
        Payment payment = paymentMapper.selectByOrderId(orderId);
        boolean success = dto.getStatus().equals("SUCCESS");
        if (success) {
            boolean markPaySuccess = paymentMapper.markPaySuccess(orderId);
            if (!markPaySuccess) {
                System.out.println("payment can not mark pay_success orderId: " + orderId);
            }
        } else {
            boolean markPayFail = paymentMapper.markPayFail(orderId);
            if (!markPayFail) {
                System.out.println("payment can not mark pay_fail orderId: " + orderId);
            }
        }

        outboxService.saveEvent(
                String.valueOf(orderId),
                "payment_result_topic",
                Map.of("orderId", orderId, "success", success, "skuId", payment.getSkuId(), "qty", payment.getQty())
        );
    }



}
