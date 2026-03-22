package com.z.payment_service.controller;

import com.z.payment_service.domain.Payment;
import com.z.payment_service.mapper.PaymentMapper;
import com.z.payment_service.service.PaymentService;
import com.z.shop.common.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(("/payment"))
public class PaymentController {

    @Autowired
    private PaymentMapper paymentMapper;

    @Autowired
    private PaymentService paymentService;

    /**
     * 用户发起支付
     */
    @PostMapping("/pay")
    public Response<Payment> pay(@RequestBody Payment payment) {

        return paymentService.process(payment);

    }

}
