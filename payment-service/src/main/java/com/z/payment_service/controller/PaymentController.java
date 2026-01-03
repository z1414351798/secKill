package com.z.payment_service.controller;

import com.z.payment_service.domain.PaymentOrder;
import com.z.payment_service.mapper.PaymentOrderMapper;
import com.z.payment_service.service.PaymentService;
import com.z.shop.common.Response;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(("/payment"))
public class PaymentController {

    @Autowired
    private PaymentOrderMapper paymentOrderMapper;

    @Autowired
    private PaymentService paymentService;

    /**
     * 用户发起支付
     */
    @PostMapping("/pay")
    public Response<PaymentOrder> pay(@RequestBody PaymentOrder paymentOrder) {

        // 4️⃣ 触发异步支付（线程池 / MQ）
        return paymentService.process(paymentOrder);

    }

}
