package com.z.order_service.service;


import com.z.order_service.enums.DeductResult;
import com.z.order_service.feignClient.InventoryClient;
import com.z.order_service.mapper.OrderMapper;
import com.z.order_service.producer.OrderProducer;
import com.z.order_service.producer.OrderTimeoutProducer;

import com.z.shop.common.InventoryDeductLog;
import com.z.shop.common.Order;
import com.z.shop.common.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.web.exchanges.HttpExchange;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class OrderService {

    @Autowired
    private RedisStockService redisStockService;
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderProducer producer;
    @Autowired
    private RedisRateLimitService rateLimitService;
    @Autowired
    private OrderTimeoutProducer orderTimeoutProducer;
    @Autowired
    private InventoryClient inventoryClient;
    @Autowired
    private StringRedisTemplate redisTemplate;

    public Response<Order> createOrder(Long orderId , Long userId, String skuId, int qty) {
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
            Response<Order> orderId1 = insert(orderId, order);
            if (orderId1 != null) return orderId1;
            log.info("订单创建成功，orderId:{}", orderId);

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

    @Transactional(rollbackFor = Exception.class)
    private Response<Order> insert(Long orderId, Order order) {
        int inserted = orderMapper.insertIgnore(order);
        if (inserted == 0) {
            // 已经被其他线程/consumer处理过
            return Response.error("Duplicated order orderId: " + orderId);
        }
        return null;
    }

    public int updateStatusAndPayingAtWhenInit(Long orderId, String status, LocalDateTime payingAt) {
        return orderMapper.updateStatusAndPayingAtWhenInit(orderId,status,payingAt);
    }

    public int markPaid(Long orderId){
        return orderMapper.markPaid(orderId);
    }

    public Order findById(Long orderId){
        return orderMapper.findById(orderId);
    }
}