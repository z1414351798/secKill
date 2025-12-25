package com.z.shop.common;


import java.time.LocalDateTime;

public class PaymentOrder {
    private Long paymentId;
    private Long orderId;
    private String skuId;
    private Integer qty;
    private String status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public Long getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(Long paymentId) {
        this.paymentId = paymentId;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public String getSkuId() {
        return skuId;
    }

    public void setSkuId(String skuId) {
        this.skuId = skuId;
    }

    public Integer getQty() {
        return qty;
    }

    public void setQty(Integer qty) {
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

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    public PaymentOrder() {
    }

    public PaymentOrder(Long paymentId, Long orderId, String skuId, Integer qty, String status, LocalDateTime createTime, LocalDateTime updateTime) {
        this.paymentId = paymentId;
        this.orderId = orderId;
        this.skuId = skuId;
        this.qty = qty;
        this.status = status;
        this.createTime = createTime;
        this.updateTime = updateTime;
    }
}
