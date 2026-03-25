package com.z.zPay.controller;

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
    public String create(@RequestParam Long orderId,
                         @RequestParam Double amount,
                         @RequestParam String callbackUrl) {
        return zPayPaymentService.createPayment(orderId, amount, callbackUrl);
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