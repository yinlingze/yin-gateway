package com.yin.yingateway.service;

import io.netty.buffer.ByteBufAllocator;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.NettyDataBufferFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.RequestPath;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Author: yin7331
 * Date: 2023/4/3 22:46
 * Describe:
 */
@Component
@Slf4j
public class YinapiGateway implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 2．请求日志
        this.getLogInfo(exchange);
        // 3．(黑白名单)
        // TODO 主要是对IP和请求头是否合法做校验，这里先放一放
        // 4．用户鉴权(判断ak、sk 是否合法)
        Map<String, Object> headerInpo = this.accountAPI(exchange);

        // 5．请求的模拟接口是否存在?
        String accessKey = (String) headerInpo.get("accessKey");
        String apiSign = (String) headerInpo.get("apiSign");
        this.emptyAPI(accessKey, apiSign);
        // TODO Map 代码放在了yinapi项目那，等明天注册中心调用，
        //  记录时间设置频率，

        return handleResponse(exchange, chain, "apiSign", "accessKey");
    }



    @Override
    public int getOrder() {
        // 指定过滤器的执行顺序
        return -1;
    }


    public void getLogInfo(ServerWebExchange exchange) {
        ServerHttpRequest request = exchange.getRequest();
        String method = request.getMethod().toString();
        RequestPath path = request.getPath();
        log.info("请求唯一标识：" + request.getId());
        log.info("请求路径：" + path);
        log.info("请求方法：" + method);
        log.info("请求参数：" + request.getQueryParams());
        String sourceAddress = request.getLocalAddress().getHostString();
        log.info("请求来源地址：" + sourceAddress);
        log.info("请求来源地址：" + request.getRemoteAddress());
    }

    public Map<String, Object> accountAPI(ServerWebExchange exchange) {
        ServerHttpRequest request = exchange.getRequest();
        HttpHeaders headers = request.getHeaders();

        Map<String, Object> map = new HashMap();

        String accessKey = headers.getFirst("accessKey");
        String requestId = headers.getFirst("requestId");
        String timeStamp = headers.getFirst("timeStamp");
        // TODO 通过access调用数据库，计算出apiSign,与此处的apiSign作比较
        // 在yinapi那写出方法，在这里直接比较
        String apiSign = headers.getFirst("apiSign");

        // TODO 添加传过来的时间戳一起计算，作比较，
        // 在yinapi那写出方法，在这里直接比较
        String genSign = headers.getFirst("genSign");

        // 将一些数据返回，后面使用；
        map.put("accessKey", accessKey);
        map.put("apiSign", apiSign);

        return map;


    }

    public Boolean emptyAPI(String apiSign, String accessKey) {
        // TODO 注册中心调用查询函数
        return true;
    }

    public Mono<Void> handleResponse(ServerWebExchange exchange, GatewayFilterChain chain, String apiSign, String accessKey) {
        try {
            ServerHttpResponse originalResponse = exchange.getResponse();
            // 缓存数据的工厂
            DataBufferFactory bufferFactory = originalResponse.bufferFactory();
            // 拿到响应码
            HttpStatus statusCode = originalResponse.getStatusCode();

            System.out.println("-=-=-=-=-=-");
            // TODO 这里可能需要修搞一下
            // if (statusCode == HttpStatus.OK) {
            if (true) {
                // 装饰，增强能力
                ServerHttpResponseDecorator decoratedResponse = new ServerHttpResponseDecorator(originalResponse) {
                    // 等调用完转发的接口后才会执行
                    @Override
                    public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
                        log.info("body instanceof Flux: {}", (body instanceof Flux));
                        if (body instanceof Flux) {
                            Flux<? extends DataBuffer> fluxBody = Flux.from(body);
                            // 往返回值里写数据
                            // 拼接字符串
                            return super.writeWith(
                                    fluxBody.map(dataBuffer -> {
                                        // 7. 调用成功，接口调用次数 + 1 invokeCount
                                        try {
                                            // TODO 注册中心远程调用
                                            // innerUserInterfaceInfoService.invokeCount(interfaceInfoId, userId);
                                        } catch (Exception e) {
                                            log.error("invokeCount error", e);
                                        }
                                        byte[] content = new byte[dataBuffer.readableByteCount()];
                                        dataBuffer.read(content);
                                        DataBufferUtils.release(dataBuffer);// 释放掉内存
                                        // 构建日志
                                        StringBuilder sb2 = new StringBuilder(200);
                                        List<Object> rspArgs = new ArrayList<>();
                                        rspArgs.add(originalResponse.getStatusCode());
                                        String data = new String(content, StandardCharsets.UTF_8); // data
                                        sb2.append(data);
                                        // 打印日志
                                        log.info("响应结果：" + data);
                                        return bufferFactory.wrap(content);
                                    }));
                        } else {
                            // 8. 调用失败，返回一个规范的错误码
                            log.error("<--- {} 响应code异常", getStatusCode());
                        }
                        return super.writeWith(body);
                    }
                };
                // 设置 response 对象为装饰过的
                return chain.filter(exchange.mutate().response(decoratedResponse).build());
            }
            return chain.filter(exchange); // 降级处理返回数据
        } catch (Exception e) {
            log.error("网关处理响应异常" + e);
            return chain.filter(exchange);
        }
    }

}
