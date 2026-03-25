package com.z.outbox.service.impl;

import com.alibaba.fastjson2.JSON;
import com.z.outbox.domain.OutboxEvent;
import com.z.outbox.mapper.OutboxEventMapper;
import com.z.outbox.service.OutboxService;
import org.springframework.beans.factory.annotation.Autowired;

public class OutboxServiceImpl implements OutboxService {

    @Autowired
    private OutboxEventMapper mapper;

    @Override
    public void saveEvent(String bizId, String topic, Object payload) {

        OutboxEvent event = new OutboxEvent();
        event.setBizId(bizId);
        event.setTopic(topic);
        event.setPayload(JSON.toJSONString(payload));
        event.setStatus("INIT");
        event.setRetryCount(0);
        event.setMaxRetry(5);

        mapper.insert(event);
    }
}