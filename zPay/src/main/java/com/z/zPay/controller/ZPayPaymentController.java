package com.z.zPay.controller;

import com.z.zPay.entity.PaymentRequest;
import com.z.zPay.service.ZPayPaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/mock")
public class ZPayPaymentController {

    @Autowired
    private ZPayPaymentService zPayPaymentService;

    @PostMapping("/create")
    public String create(@RequestBody PaymentRequest req) {
        return zPayPaymentService.createPayment(
                req.getOrderId(),
                req.getAmount(),
                req.getCallbackUrl()
        );
    }

    @GetMapping("/success")
    public String success(@RequestParam Long orderId) {
        zPayPaymentService.paySuccess(orderId);
        return "Payment Success";
    }

    @GetMapping("/fail")
    public String fail(@RequestParam Long orderId) {
        zPayPaymentService.payFail(orderId);
        return "Payment Failed";
    }

}