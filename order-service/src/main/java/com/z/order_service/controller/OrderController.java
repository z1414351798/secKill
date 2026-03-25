package com.z.order_service.controller;


import com.z.order_service.mapper.OrderMapper;
import com.z.order_service.service.OrderService;

import com.z.shop.common.FastSnowflakeIdGenerator;
import com.z.shop.common.Order;
import com.z.shop.common.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private OrderService orderService;
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private FastSnowflakeIdGenerator fastSnowflakeIdGenerator;

    @PostMapping("/seckill")
    public Response<Order> seckill(@RequestHeader("X-User-Id") Long userId,
                                   @RequestParam String skuId,
                                   @RequestParam int qty,
                                   @RequestParam BigDecimal amount) {
        return orderService.createOrder(fastSnowflakeIdGenerator.nextId(),userId, skuId, qty, amount);
    }

    @GetMapping("/findById")
    public Order findById(@RequestParam Long orderId){
        return orderService.findById(orderId);
    }


    @GetMapping("/{orderId}/status")
    public Response<Order> getOrderStatus(@PathVariable Long orderId) {
        Order order = orderService.findById(orderId);
        return Response.success(order);
    }

    @PostMapping("/{orderId}/pay")
    public Response<String> payOrder(@PathVariable Long orderId) {
        return orderService.payOrder(orderId);
    }
}
