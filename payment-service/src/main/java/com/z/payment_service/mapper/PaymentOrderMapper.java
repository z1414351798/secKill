package com.z.payment_service.mapper;

import com.z.payment_service.domain.PaymentOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

public interface PaymentOrderMapper {

    PaymentOrder selectByOrderId(Long orderId);

    int insertIgnore(PaymentOrder order);

    int updateStatus(@Param("orderId") Long orderId,
                     @Param("status") String status);
}
