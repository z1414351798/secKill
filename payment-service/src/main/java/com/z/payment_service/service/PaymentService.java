package com.z.payment_service.service;

import com.z.payment_service.domain.PaymentOrder;
import com.z.payment_service.feignClient.InventoryClient;
import com.z.payment_service.feignClient.OrderClient;
import com.z.payment_service.mapper.PaymentOrderMapper;
import com.z.payment_service.producer.PaymentResultProducer;
import com.z.shop.common.FastSnowflakeIdGenerator;
import com.z.shop.common.InventoryDeductLog;
import com.z.shop.common.Order;
import com.z.shop.common.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class PaymentService {

    @Autowired
    private PaymentOrderMapper paymentOrderMapper;

    @Autowired
    private PaymentResultProducer paymentResultProducer;
    @Autowired
    private OrderClient orderClient;
    @Autowired
    private InventoryClient inventoryClient;
    @Autowired
    private FastSnowflakeIdGenerator fastSnowflakeIdGenerator;


    @Transactional
    public Response<PaymentOrder> process(PaymentOrder paymentOrder) {

        paymentOrder.setStatus("PROCESSING");
        int inserted = paymentOrderMapper.insertIgnore(paymentOrder);
        if (inserted == 0) {
            // 已经被其他线程/consumer处理过
            return Response.error("Can not insert payment order");
        }

        // 2️⃣ 执行业务（模拟）
        boolean success = doBusiness(paymentOrder);

        // 3️⃣ 更新支付状态
        int updatePstatus = paymentOrderMapper.updateStatus(
                paymentOrder.getOrderId(),
                success ? "SUCCESS" : "FAIL"
        );
        if (updatePstatus == 0){
            return Response.error("Update payment status fail");
        }

        // 4️⃣ 发结果事件
        Map<String, Object> msg = new HashMap<>();
        msg.put("orderId", paymentOrder.getOrderId());
        msg.put("skuId", paymentOrder.getSkuId());
        msg.put("qty", paymentOrder.getQty());
        msg.put("success", success);
        paymentResultProducer.send(msg, paymentOrder.getOrderId().toString());
        System.out.println("Payment success result: " + success + " orderId: " +paymentOrder.getOrderId() + " paymentOrderId: " + paymentOrder.getPaymentId());
        return Response.success(paymentOrder);

    }

    private boolean doBusiness(PaymentOrder paymentOrder) {
        // 模拟支付 / 撮合 / 风控
        return Math.random() > 0.5; // 50% 成功
    }

}
