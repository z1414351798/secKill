package com.z.order_service.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RedisRateLimitService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final DefaultRedisScript<Long> RATE_LIMIT_SCRIPT;

    static {
        RATE_LIMIT_SCRIPT = new DefaultRedisScript<>();
        RATE_LIMIT_SCRIPT.setResultType(Long.class);
        RATE_LIMIT_SCRIPT.setLocation(
            new ClassPathResource("lua/rate_limit.lua")
        );
    }

    /**
     * @param key 限流 key（如 sku / 接口）
     */
    public boolean tryAcquire(String key, int maxTokens, int refillRate) {

        Long result = redisTemplate.execute(
            RATE_LIMIT_SCRIPT,
            List.of("rate:" + key),
            String.valueOf(maxTokens),
            String.valueOf(refillRate),
            String.valueOf(System.currentTimeMillis())
        );

        return result == 1;
    }
}
