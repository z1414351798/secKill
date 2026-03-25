package com.z.zPay.mapper;

import com.z.zPay.entity.ZPayPaymentOrder;
import org.apache.ibatis.annotations.*;

@Mapper
public interface ZPayPaymentOrderMapper {

    @Insert("INSERT INTO zpay_payment_order(order_id, amount, status, callback_url) VALUES(#{orderId}, #{amount}, #{status}, #{callbackUrl})")
    void insert(ZPayPaymentOrder order);

    @Select("SELECT * FROM zpay_payment_order WHERE order_id = #{orderId}")
    ZPayPaymentOrder findByOrderId(Long orderId);

    @Update("UPDATE zpay_payment_order SET status = #{status} WHERE order_id = #{orderId}")
    void updateStatus(@Param("orderId") Long orderId, @Param("status") String status);
}