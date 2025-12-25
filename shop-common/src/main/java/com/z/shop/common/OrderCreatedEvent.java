package com.z.shop.common;


import java.io.Serializable;
import java.time.LocalDateTime;

public class OrderCreatedEvent implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long orderId;
    private Long userId;
    private String skuId;
    private int qty;
    private String status;
    private LocalDateTime createTime = LocalDateTime.now();

    public int getQty() {
        return qty;
    }

    public void setQty(int qty) {
        this.qty = qty;
    }

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

    public OrderCreatedEvent(Long orderId, Long userId, String skuId, int qty, String status, LocalDateTime createTime) {
        this.orderId = orderId;
        this.userId = userId;
        this.skuId = skuId;
        this.qty = qty;
        this.status = status;
        this.createTime = createTime;
    }

    public OrderCreatedEvent() {
    }
}
