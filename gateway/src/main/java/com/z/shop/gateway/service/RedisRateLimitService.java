package com.z.shop.gateway.service;

import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RedisRateLimitService {

    private final StringRedisTemplate redisTemplate;
    private final DefaultRedisScript<Long> script;

    public RedisRateLimitService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.script = new DefaultRedisScript<>();
        this.script.setLocation(new ClassPathResource("lua/rate_limit.lua"));
        this.script.setResultType(Long.class);
    }

    public boolean tryAcquire(String key, int maxTokens, int refillRate) {
        Long result = redisTemplate.execute(
                script,
                List.of(key),
                String.valueOf(maxTokens),
                String.valueOf(refillRate),
                String.valueOf(System.currentTimeMillis())
        );
        return result != null && result == 1;
    }
}
