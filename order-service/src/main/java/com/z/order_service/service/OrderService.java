package com.z.order_service.service;


import com.z.order_service.enums.DeductResult;
import com.z.order_service.feignClient.InventoryClient;
import com.z.order_service.feignClient.PaymentClient;
import com.z.order_service.mapper.OrderMapper;
import com.z.order_service.producer.OrderTimeoutProducer;

import com.z.outbox.service.OutboxService;
import com.z.outbox.task.OutboxSendTask;
import com.z.shop.common.Order;
import com.z.shop.common.Payment;
import com.z.shop.common.Response;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class OrderService {

    @Autowired
    private RedisStockService redisStockService;
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private RedisRateLimitService rateLimitService;
    @Autowired
    private OrderTimeoutProducer orderTimeoutProducer;
    @Autowired
    private InventoryClient inventoryClient;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private PaymentClient paymentClient;

    @Autowired
    private OutboxService outboxService;

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    @Transactional
    public Response<Order> createOrder(Long orderId , Long userId, String skuId, int qty, BigDecimal amount) {
        try {
            // 1. 秒杀限流
//            boolean pass = rateLimitService.tryAcquire("seckill:{" + skuId + "}", 1000, 500);
//            if (!pass) {
//                return Response.error("系统繁忙");
//            }

            if (!redisStockService.checkUserIdempotent(skuId, userId)) {
                return Response.error("请勿重复下单");
            }


            // 2. Redis预扣库存
            DeductResult preDeductResult = redisStockService.preDeduct(orderId, skuId, qty);
            if (preDeductResult != DeductResult.SUCCESS) {
                return Response.error(preDeductResult == DeductResult.NO_STOCK ?
                        "库存不足" : "重复下单");
            }
            // 3. 创建订单
            Order order = new Order();
            order.setOrderId(orderId);
            order.setUserId(userId);
            order.setSkuId(skuId);
            order.setQty(qty);
            order.setStatus("INIT");
            order.setAmount(amount);
            orderMapper.insert(order);
            log.info("订单创建成功，orderId:{}", orderId);

            outboxService.saveEvent(
                    String.valueOf(orderId),
                    "inventory-deduct-topic",
                    Map.of("orderId", orderId, "skuId", skuId, "qty", qty)
            );

            boolean markDeducting = orderMapper.markDeducting(orderId);

            if (!markDeducting) return Response.error("Mark deducting fail orderId: " + orderId);

            // 5. 发送延时消息（放到最后，避免消息发送成功但其他操作失败）
            try {
                orderTimeoutProducer.sendDelay(order);
                log.info("超时消息发送成功，orderId:{}", orderId);
            } catch (Exception e) {
                log.error("超时消息发送失败，orderId:{}", orderId, e);
                // 消息发送失败不影响主流程，可以记录日志或重试
            }

            // 5️⃣ 标记幂等成功
            redisTemplate.opsForValue()
                    .set("idempotent:sku:{" + skuId + "}:"+ "user:" + userId,
                            "SUCCESS",
                            10, TimeUnit.MINUTES);

            return Response.success(order);

        } catch (RuntimeException e) {
            // 1️⃣ 回滚库存
            redisStockService.rollback(orderId, skuId, qty);

            // 2️⃣ 删除幂等标记（允许重试）
            redisTemplate.delete("seckill:order:" + skuId + ":" + userId);

            throw e;
        } catch (Exception e) {
            log.error("创建订单未知异常，orderId:{}, error:{}", orderId, e.getMessage(), e);
            throw new RuntimeException("系统异常，请稍后重试");
        }
    }

//    @Transactional(rollbackFor = Exception.class)
//    private Response<Order> insert(Long orderId, Order order) {
//        int inserted = orderMapper.insertIgnore(order);
//        if (inserted == 0) {
//            // 已经被其他线程/consumer处理过
//            redisStockService.rollback(orderId, order.getSkuId(), order.getQty());
//            return Response.error("Duplicated order orderId: " + orderId);
//        }
//        return null;
//    }



    public Order findById(Long orderId){
        return orderMapper.findById(orderId);
    }

    @Transactional
    public Response<String>     payOrder(Long orderId) {

        boolean markPayInit = orderMapper.markPayInit(orderId);
        if (!markPayInit) {
            return Response.error("Can not mark pay init");
        }

        Order order = orderMapper.findById(orderId);

        // 3️⃣ 调用支付服务（不是直接支付！）
        Payment payment = new Payment();
        payment.setQty(order.getQty());
        payment.setAmount(order.getAmount());
        payment.setSkuId(order.getSkuId());
        payment.setOrderId(orderId);

        Response<String> payUrl = paymentClient.createPayment(payment);
        if (payUrl.getCode() == 200) {
            if (orderMapper.markPayProcessing(orderId)) {
                return Response.success(payUrl.getData());
            }
        } else {
            throw new RuntimeException("create payment fail");
        }

        return Response.error("Create payment error");
    }
}