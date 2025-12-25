package com.z.shop.common;


public class Inventory {
    private String skuId;
    private int total;
    private int available;
    private int locked;

    public int getAvailable() {
        return available;
    }

    public void setAvailable(int available) {
        this.available = available;
    }

    public String getSkuId() {
        return skuId;
    }

    public void setSkuId(String skuId) {
        this.skuId = skuId;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getLocked() {
        return locked;
    }

    public void setLocked(int locked) {
        this.locked = locked;
    }

    public Inventory() {
    }

    public Inventory(String skuId, int total, int available, int locked) {
        this.skuId = skuId;
        this.total = total;
        this.available = available;
        this.locked = locked;
    }
}