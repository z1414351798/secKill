package com.z.order_service.mapper;

import com.z.shop.common.Order;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderMapper {
    int insert(Order order);

    Order findById(@Param("orderId") Long orderId);

    int timeoutCancel(@Param("orderId") Long orderId);

    boolean markPayProcessing(@Param("orderId") Long orderId);

    boolean markPaySuccess(@Param("orderId") Long orderId);

    boolean markPayFAIL(@Param("orderId") Long orderId);

    boolean markCanPay(@Param("orderId") Long orderId);

    boolean markDeducting(@Param("orderId") Long orderId);

    boolean markDeductSuccess(@Param("orderId") Long orderId);

    boolean markDeductFail(@Param("orderId") Long orderId);

    boolean markPayInit(@Param("orderId") Long orderId);

    boolean markFinalSuccess(@Param("orderId") Long orderId);

    boolean markRollbackInit(@Param("orderId") Long orderId);

    boolean markRollbackProcessing(@Param("orderId") Long orderId);

    boolean markRollbackFail(@Param("orderId") Long orderId);

    boolean markRollbackSuccess(@Param("orderId") Long orderId);
}
