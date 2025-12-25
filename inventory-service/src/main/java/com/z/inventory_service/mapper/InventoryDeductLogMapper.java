package com.z.inventory_service.mapper;

import com.z.shop.common.InventoryDeductLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

public interface InventoryDeductLogMapper {

    /**
     * 插入扣减日志
     * 幂等保证：order_id 唯一
     */
    int insertIgnore(InventoryDeductLog log);

    /**
     * 根据 orderId 查询（用于判断是否已扣）
     */
    InventoryDeductLog selectByOrderIdAndSkuId(@Param("orderId") Long orderId, @Param("skuId") String skuId);

    int updateStatus(@Param("orderId") Long orderId, @Param("skuId") String skuId, @Param("status") String status);
}
