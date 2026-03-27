package com.z.payment_service.controller;

import com.z.outbox.service.OutboxService;
import com.z.payment_service.mapper.PaymentMapper;
import com.z.payment_service.service.PaymentService;
import com.z.shop.common.Payment;
import com.z.shop.common.PaymentDto;
import com.z.shop.common.PaymentRequest;
import com.z.shop.common.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@RestController
@RequestMapping(("/payment"))
public class PaymentController {

    @Autowired
    private PaymentMapper paymentMapper;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private OutboxService outboxService;

    private RestTemplate restTemplate = new RestTemplate();

    /**
     * 用户发起支付
     */
    @PostMapping("/pay/callback")
    public String pay(@RequestBody PaymentDto paymentDto) {
        // 发送最终失败事件
        outboxService.saveEvent(
                String.valueOf(paymentDto.getOrderId()),
                "zpay-callback-topic",
                paymentDto
        );

         return "success";
    }

    @Transactional
    @PostMapping("/createPayment")
    public Response<String> createPayment(@RequestBody Payment payment) {

        paymentMapper.insertPayInit(payment);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        PaymentRequest req = new PaymentRequest();
        req.setOrderId(payment.getOrderId());
        req.setAmount(payment.getAmount());
        req.setCallbackUrl("http://localhost:8401/payment-service/payment/pay/callback");

        HttpEntity<PaymentRequest> request = new HttpEntity<>(req, headers);

        boolean markPayProcessing = paymentMapper.markPayProcessing(payment.getOrderId());

        if (markPayProcessing) {
            String payUrl = restTemplate.postForObject(
                    "http://localhost:9000/zpay-service/mock/create",
                    request,
                    String.class
            );
            if (payUrl == null) {
                throw new RuntimeException("call zpay create payment fail");
            }

            return Response.success(payUrl);
        }
        return Response.error("Create payment fail");
    }

}
