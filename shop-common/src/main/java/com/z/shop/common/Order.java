package com.z.shop.common;


import java.time.LocalDateTime;


public class Order {
    private Long orderId;
    private Long userId;
    private String skuId;
    private int qty;
    private String status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private LocalDateTime payingAt;

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getSkuId() {
        return skuId;
    }

    public void setSkuId(String skuId) {
        this.skuId = skuId;
    }

    public int getQty() {
        return qty;
    }

    public void setQty(int qty) {
        this.qty = qty;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getPayingAt() {
        return payingAt;
    }

    public void setPayingAt(LocalDateTime payingAt) {
        this.payingAt = payingAt;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    public Order() {
    }

    public Order(Long orderId, Long userId, String skuId, int qty, String status, LocalDateTime createTime, LocalDateTime updateTime, LocalDateTime payingAt) {
        this.orderId = orderId;
        this.userId = userId;
        this.skuId = skuId;
        this.qty = qty;
        this.status = status;
        this.createTime = createTime;
        this.updateTime = updateTime;
        this.payingAt = payingAt;
    }
}