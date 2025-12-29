package com.z.shop.gateway.filter;

import com.z.shop.gateway.service.RedisRateLimitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

@Component
public class SeckillRateLimitFilter implements GlobalFilter, Ordered {

    @Autowired
    private RedisRateLimitService rateLimitService;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        String path = exchange.getRequest().getURI().getPath();
        if (!path.contains("/order/seckill")) {
            return chain.filter(exchange);
        }

        String skuId = exchange.getRequest().getQueryParams().getFirst("skuId");
        String userId = exchange.getRequest().getHeaders().getFirst("X-User-Id");

        if (skuId == null || userId == null) {
            return unauthorized(exchange);
        }

        String ip = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
        if (ip == null) {
            ip = exchange.getRequest()
                    .getRemoteAddress()
                    .getAddress()
                    .getHostAddress();
        }

        if (!checkSeckill(skuId, userId, ip)) {
            return tooMany(exchange);
        }

        return chain.filter(exchange);
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }

    private Mono<Void> tooMany(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
        return exchange.getResponse().setComplete();
    }

    @Override
    public int getOrder() {
        return -100; // 越小越早
    }

    public boolean checkSeckill(String skuId, String userId, String ip) {

        // 第一层：SKU
        if (!rateLimitService.tryAcquire(
                "seckill:{" + skuId + "}:rate",
                1000,
                500)) {
            return false;
        }

        // 第二层：IP
        if (!rateLimitService.tryAcquire(
                "seckill:{" + skuId + "}:ip:{" + ip + "}:rate",
                20,
                10)) {
            return false;
        }

        // 第三层：用户
        if (!rateLimitService.tryAcquire(
                "seckill:{" + skuId + "}:user:{" + userId + "}:rate",
                5,
                1)) {
            return false;
        }

        return true;
    }

}
