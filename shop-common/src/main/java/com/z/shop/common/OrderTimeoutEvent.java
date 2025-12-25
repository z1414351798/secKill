package com.z.shop.common;

import java.io.Serializable;

public class OrderTimeoutEvent implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long orderId;
    private String skuId;
    private int qty;

    public OrderTimeoutEvent() {
    }

    public OrderTimeoutEvent(Long orderId, String skuId, int qty) {
        this.orderId = orderId;
        this.skuId = skuId;
        this.qty = qty;
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

    public int getQty() {
        return qty;
    }

    public void setQty(int qty) {
        this.qty = qty;
    }

}