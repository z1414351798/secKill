package com.z.order_service.controller;

import com.z.order_service.producer.MessageProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rocketmq")
public class RocketMqController {
    @Autowired
    private MessageProducer messageProducer;

    @PostMapping("/send")
    public String send(@RequestParam String topic ,@RequestParam String message){
        return messageProducer.sendMessage(topic,message);
    }
}
