package com.z.order_service.config;

import com.z.shop.common.FastSnowflakeIdGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class IdGeneratorConfig {

    @Bean
    public FastSnowflakeIdGenerator snowflakeIdGenerator() {
        long workerId = Long.getLong("WORKER_ID");
        long datacenterId = Long.getLong("DATACENTER_ID");
        return new FastSnowflakeIdGenerator(workerId, datacenterId);
    }
}
