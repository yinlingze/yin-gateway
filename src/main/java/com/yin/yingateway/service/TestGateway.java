package com.yin.yingateway.service;

import com.sun.org.apache.xpath.internal.operations.Or;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Author: yin7331
 * Date: 2023/4/7 22:59
 * Describe:
 */
public class TestGateway implements GatewayFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        System.out.println("testGateway");

        return null;
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
