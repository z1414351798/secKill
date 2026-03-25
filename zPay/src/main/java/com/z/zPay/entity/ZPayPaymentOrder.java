package com.z.zPay.entity;

import lombok.Data;

@Data
public class ZPayPaymentOrder {
    private Long id;
    private Long orderId;
    private Double amount;
    private String status;
    private String callbackUrl;
}