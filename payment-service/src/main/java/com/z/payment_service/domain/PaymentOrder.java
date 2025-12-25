package com.z.payment_service.domain;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PaymentOrder {
    private Long paymentId;
    private Long orderId;
    private String skuId;
    private Integer qty;
    private String status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
