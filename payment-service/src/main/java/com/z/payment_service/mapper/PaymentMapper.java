package com.z.payment_service.mapper;

import com.z.shop.common.Payment;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface PaymentMapper {

    Payment selectByOrderIdAndPaymentId(@Param("orderId") Long orderId,
                                        @Param("paymentId") Long paymentId);

    Payment selectByOrderId(@Param("orderId") Long orderId);

    int insertPayInit(Payment order);

    int updateStatus(@Param("orderId") Long orderId,
                     @Param("paymentId") Long paymentId,
                     @Param("status") String status);

    boolean markPayProcessing(@Param("orderId") Long orderId);

    boolean markPayFail(@Param("orderId") Long orderId);

    boolean markPaySuccess(@Param("orderId") Long orderId);

    boolean markRefundSuccess(@Param("orderId") Long orderId,
                           @Param("paymentId") Long paymentId);

    boolean markRefundFail(@Param("orderId") Long orderId,
                           @Param("paymentId") Long paymentId);

    boolean markRefundProcessing(@Param("orderId") Long orderId,
                                 @Param("paymentId") Long paymentId);

    boolean markRefundInit(@Param("orderId") Long orderId,
                           @Param("paymentId") Long paymentId);
}
