package com.z.inventory_service.mapper;

import org.apache.ibatis.annotations.Param;

public interface InventoryStockLogMapper {

    int insertIgnore(@Param("id") Long id,
                     @Param("orderId") Long orderId,
                     @Param("skuId") String skuId,
                     @Param("qty") int qty);

    int markDeductProcessing(@Param("orderId") Long orderId,
                             @Param("skuId") String skuId);

    int markDeductSuccess(@Param("orderId") Long orderId,
                          @Param("skuId") String skuId);

    int markDeductFail(@Param("orderId") Long orderId,
                       @Param("skuId") String skuId);

    int incrementDeductRetry(@Param("orderId") Long orderId,
                             @Param("skuId") String skuId,
                             @Param("deductMaxCount") int maxRetry);

    int markDeductFailMaxRetry(@Param("orderId") Long orderId,
                               @Param("skuId") String skuId,
                               @Param("deductMaxCount") int maxRetry);

    int markRollbackInit(@Param("orderId") Long orderId,
                         @Param("skuId") String skuId);

    int markRollbackProcessing(@Param("orderId") Long orderId,
                               @Param("skuId") String skuId);

    int markRollbackSuccess(@Param("orderId") Long orderId,
                            @Param("skuId") String skuId);

    int markRollbackFail(@Param("orderId") Long orderId,
                         @Param("skuId") String skuId);

    int incrementRollbackRetry(@Param("orderId") Long orderId,
                               @Param("skuId") String skuId,
                               @Param("rollbackMaxCount") int maxRetry);

    int markRollbackFailMaxRetry(@Param("orderId") Long orderId,
                                 @Param("skuId") String skuId,
                                 @Param("rollbackMaxCount") int maxRetry);

    int markUnknownError(@Param("orderId") Long orderId,
                         @Param("skuId") String skuId);
}