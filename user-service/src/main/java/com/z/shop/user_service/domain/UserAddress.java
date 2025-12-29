package com.z.shop.user_service.domain;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserAddress {
    private Long id;
    private Long userId;
    private Long addressId;
    private Integer isDefault;
    private LocalDateTime createdAt;
}
