package com.z.order_service.service;

import com.z.order_service.enums.DeductResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.z.order_service.enums.DeductResult.*;

@Component
public class RedisStockService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final DefaultRedisScript<Long> PRE_DEDUCT_SCRIPT;
    private static final DefaultRedisScript<Long> ROLLBACK_DEDUCT_SCRIPT;
    private static final DefaultRedisScript<Long> CONFIRM_DEDUCT_SCRIPT;

    static {
        PRE_DEDUCT_SCRIPT = new DefaultRedisScript<>();
        PRE_DEDUCT_SCRIPT.setResultType(Long.class);
        PRE_DEDUCT_SCRIPT.setLocation(
                new ClassPathResource("lua/stock_pre_deduct.lua")
        );
        ROLLBACK_DEDUCT_SCRIPT = new DefaultRedisScript<>();
        ROLLBACK_DEDUCT_SCRIPT.setResultType(Long.class);
        ROLLBACK_DEDUCT_SCRIPT.setLocation(
                new ClassPathResource("lua/rollback_deduct.lua")
        );
        CONFIRM_DEDUCT_SCRIPT = new DefaultRedisScript<>();
        CONFIRM_DEDUCT_SCRIPT.setResultType(Long.class);
        CONFIRM_DEDUCT_SCRIPT.setLocation(
                new ClassPathResource("lua/confirm_deduct.lua")
        );
    }

    public DeductResult preDeduct(Long orderId, String skuId, int qty) {
        String tag = "{" + skuId + "}";
        Long r = redisTemplate.execute(
                PRE_DEDUCT_SCRIPT,
                List.of(
                        "stock:" + tag,
                        "stock:lock:" + tag,
                        "stock:freeze:" + tag + ":order:" +  orderId
                ),
                String.valueOf(qty)
        );
        if (r == 1) return SUCCESS;
        if (r == 2) return DUPLICATE;
        return NO_STOCK;
    }


    public DeductResult rollback(Long orderId, String skuId, int qty) {
        String tag = "{" + skuId + "}";
        Long r = redisTemplate.execute(
                ROLLBACK_DEDUCT_SCRIPT,
                List.of(
                        "stock:" + tag,
                        "stock:lock:" + tag,
                        "stock:freeze:" + tag + ":order:" +  orderId
                ),
                String.valueOf(qty)
        );
        if (r == 0) return FREEZE_NOT_EXIST;
        return SUCCESS;
    }

    public DeductResult confirm(Long orderId, String skuId, int qty) {
        String tag = "{" + skuId + "}";
        Long r = redisTemplate.execute(
                CONFIRM_DEDUCT_SCRIPT,
                List.of(
                        "stock:lock:" + tag,
                        "stock:freeze:" + tag + ":order:" +  orderId
                ),
                String.valueOf(qty));
        if (r == 0) return FREEZE_NOT_EXIST;
        return SUCCESS;
    }
}

