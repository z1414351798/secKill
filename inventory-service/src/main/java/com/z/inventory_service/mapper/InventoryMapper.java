package com.z.inventory_service.mapper;

import com.z.shop.common.Inventory;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface InventoryMapper {
    int deduct(@Param("skuId") String skuId,
               @Param("qty") int qty);
    int save(Inventory inventory);
    List<Inventory> findAll();
    int confirm(@Param("skuId") String skuId,
                @Param("qty") int qty);
    int rollback(@Param("skuId") String skuId,
                 @Param("qty") int qty);

}
