package com.z.shop.user_service.domain;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Address {
    private Long id;
    private String receiverName;
    private String receiverPhone;
    private String province;
    private String city;
    private String district;
    private String detail;
    private LocalDateTime createdAt;
}
