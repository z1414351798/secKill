package com.z.outbox.config;

import com.z.outbox.mapper.OutboxEventMapper;
import com.z.outbox.properties.OutboxProperties;
import com.z.outbox.service.OutboxService;
import com.z.outbox.service.impl.OutboxServiceImpl;
import com.z.outbox.task.OutboxSendTask;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
@MapperScan("com.z.outbox.mapper")
@EnableConfigurationProperties(OutboxProperties.class)
public class OutboxAutoConfiguration {

    @Bean
    public OutboxService outboxService() {
        return new OutboxServiceImpl();
    }

    @Bean
    public OutboxSendTask outboxSendTask(
            OutboxEventMapper mapper,
            KafkaTemplate<String, String> kafkaTemplate,
            OutboxProperties properties) {

        return new OutboxSendTask(mapper, kafkaTemplate, properties);
    }
}