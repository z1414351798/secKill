package com.z.shop.common;


import java.time.LocalDateTime;


public class InventoryStockLog {
    private Long Id;
    private Long orderId;
    private String skuId;
    private Integer qty;
    private String status;
    private Integer deductRetryCount;
    private Integer RollbackRetryCount;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

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

    public Long getId() {
        return Id;
    }

    public void setId(Long id) {
        Id = id;
    }

    public Integer getDeductRetryCount() {
        return deductRetryCount;
    }

    public void setDeductRetryCount(Integer deductRetryCount) {
        this.deductRetryCount = deductRetryCount;
    }

    public Integer getRollbackRetryCount() {
        return RollbackRetryCount;
    }

    public void setRollbackRetryCount(Integer rollbackRetryCount) {
        RollbackRetryCount = rollbackRetryCount;
    }
}
