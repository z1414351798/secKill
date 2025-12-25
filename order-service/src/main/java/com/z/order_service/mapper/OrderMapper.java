package com.z.order_service.mapper;

import com.z.shop.common.Order;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderMapper {
    int updateStatusAndPayingAtWhenInit(@Param("orderId") Long orderId,
                                        @Param("status") String status,
                                        @Param("payingAt")LocalDateTime payingAt);
    int updateStatusWhenPaying(@Param("orderId") Long orderId,
                             @Param("status") String status);
    int updateStatusByOrderId(@Param("orderId") Long orderId,
                              @Param("status") String status);
    int insertIgnore(Order order);

    Order findById(@Param("orderId") Long orderId);

    int timeoutCancel(@Param("orderId") Long orderId);

    int confirm(@Param("orderId") Long orderId);

    int rollback(@Param("orderId") Long orderId);

    List<Long> selectTimeoutOrders(@Param("limit") int limit);

    int markPaid(@Param("orderId") Long orderId);
}
