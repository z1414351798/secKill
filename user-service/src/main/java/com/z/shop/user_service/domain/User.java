package com.z.shop.user_service.domain;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class User {
    private Long id;
    private String username;
    private String password;
    private String mobile;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
