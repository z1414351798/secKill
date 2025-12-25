package com.z.order_service.config;

import com.z.shop.common.FastSnowflakeIdGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class IdGeneratorConfig {

    @Bean
    public FastSnowflakeIdGenerator snowflakeIdGenerator() {
        long workerId = 1;      // 来自 env / k8s ordinal
        long datacenterId = 1;
        return new FastSnowflakeIdGenerator(workerId, datacenterId);
    }
}
