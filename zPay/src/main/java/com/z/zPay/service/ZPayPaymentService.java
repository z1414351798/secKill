package com.z.zPay.service;

import com.z.zPay.entity.ZPayPaymentOrder;
import com.z.zPay.mapper.ZPayPaymentOrderMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;

@Service
public class ZPayPaymentService {

    @Autowired
    private ZPayPaymentOrderMapper mapper;

    private RestTemplate restTemplate = new RestTemplate();

    @Transactional
    public String createPayment(Long orderId, BigDecimal amount, String callbackUrl) {
            ZPayPaymentOrder order = new ZPayPaymentOrder();
            order.setOrderId(orderId);
            order.setAmount(amount);
            order.setStatus("INIT");
            order.setCallbackUrl(callbackUrl);
            mapper.insert(order);
        return "http://localhost:9000/zpay-service/mock/pay-page?orderId=" + orderId;
    }

    @Transactional
    public void paySuccess(Long orderId) {
        mapper.updateStatus(orderId, "SUCCESS");
        ZPayPaymentOrder order = mapper.findByOrderId(orderId);
        // callback your system
        String result = restTemplate.postForObject(order.getCallbackUrl(), order, String.class);
        if (result.equals("success")) System.out.println("Payment success orderId: " + orderId);
    }

    @Transactional
    public void payFail(Long orderId) {
        mapper.updateStatus(orderId, "FAILED");
        ZPayPaymentOrder order = mapper.findByOrderId(orderId);
        // callback your system
        String result = restTemplate.postForObject(order.getCallbackUrl(), order, String.class);
        if (result.equals("success")) System.out.println("Payment success orderId: " + orderId);
    }

}