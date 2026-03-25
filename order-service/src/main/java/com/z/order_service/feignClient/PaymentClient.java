package com.z.order_service.feignClient;

import com.z.shop.common.Payment;
import com.z.shop.common.Response;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "payment-service" )
public interface PaymentClient {

    @PostMapping("/payment-service/payment/pay")
    Response<Payment> pay(@RequestBody Payment payment);

    @PostMapping("/payment-service/payment/createPayment")
    Response<String> createPayment(@RequestBody Payment payment);
}
