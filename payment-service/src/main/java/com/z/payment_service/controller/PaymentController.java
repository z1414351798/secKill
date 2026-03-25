package com.z.payment_service.controller;

import com.z.payment_service.mapper.PaymentMapper;
import com.z.payment_service.service.PaymentService;
import com.z.shop.common.Payment;
import com.z.shop.common.PaymentDto;
import com.z.shop.common.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping(("/payment"))
public class PaymentController {

    @Autowired
    private PaymentMapper paymentMapper;

    @Autowired
    private PaymentService paymentService;

    private RestTemplate restTemplate = new RestTemplate();

    /**
     * 用户发起支付
     */
    @PostMapping("/pay/callback")
    public String pay(@RequestBody PaymentDto paymentDto) {

         paymentService.process(paymentDto);
         return "success";
    }

    @PostMapping("/createPayment")
    public Response<String> createPayment(@RequestBody Payment payment) {

        paymentMapper.insertPayInit(payment);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("orderId", String.valueOf(payment.getOrderId()));
        params.add("amount", String.valueOf(payment.getAmount()));
        params.add("callbackUrl", "http://localhost:6666/payment-service/pay/callback");

        HttpEntity<MultiValueMap<String, String>> request =
                new HttpEntity<>(params, headers);

        paymentMapper.markPayProcessing(payment.getOrderId());
        String payUrl = restTemplate.postForObject(
                "http://localhost:9000/mock/create",
                request,
                String.class
        );
        return Response.success(payUrl);
    }

}
