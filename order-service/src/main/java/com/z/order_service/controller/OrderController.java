package com.z.order_service.controller;


import com.z.order_service.mapper.OrderMapper;
import com.z.order_service.service.OrderService;

import com.z.shop.common.FastSnowflakeIdGenerator;
import com.z.shop.common.Order;
import com.z.shop.common.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

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
                                   @RequestParam int qty) {
        return orderService.createOrder(fastSnowflakeIdGenerator.nextId(),userId, skuId, qty);
    }

    @PostMapping("/updateStatusAndPayingAtWhenInit")
    public int updateStatusAndPayingAtWhenInit(@RequestParam Long orderId, @RequestParam String status, @RequestParam  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime payingAt){
        return orderService.updateStatusAndPayingAtWhenInit(orderId,status,payingAt);
    }

    @PostMapping("/markPaid")
    public int markPaid(@RequestParam Long orderId){
        return orderService.markPaid(orderId);
    }

    @GetMapping("/findById")
    public Order findById(@RequestParam Long orderId){
        return orderService.findById(orderId);
    }

    @PostMapping("/updatePaying")
    public boolean updatePaying(@RequestParam Long orderId){
        return orderMapper.updatePaying(orderId);
    }

    @PostMapping("/updateDeducting")
    public boolean updateDeducting(@RequestParam Long orderId){
        return orderMapper.updateDeducting(orderId);
    }

    @PostMapping("/updateRefunding")
    public boolean updateRefunding(@RequestParam Long orderId){
        return orderMapper.updateRefunding(orderId);
    }
}
