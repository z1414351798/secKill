package com.z.outbox.service;

public interface OutboxService {

    void saveEvent(String bizId, String topic, Object payload);
}