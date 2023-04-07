package com.yin.yingateway.service;

import com.yin.yingateway.common.ErrorCode;
import com.yin.yingateway.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.RequestPath;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Author: yin7331
 * Date: 2023/4/7 22:38
 * Describe:
 */
@Component
@Slf4j
public class UserApiGateway implements GatewayFilter , Ordered {
    private final WebClient webClient;


    public UserApiGateway(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }
    @Value("${service-url.gateway-service-url}")
    private String PATH_GATEWAY_SERVICE_URL;
    // @Resource
    // RestTemplate restTemplate;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        // 2．请求日志
        this.getLogInfo(exchange);
        // 3．(黑白名单)
        // TODO 主要是对IP和请求头是否合法做校验，这里先放一放

        // 4．用户鉴权(判断ak、sk 是否存在，合法，频率是否过快)
        Map headerInpo = this.accountAPI(exchange);

        if (headerInpo == null) {
            // TODO 这里验证失败，想个返回值
            // return null;
        }

        String apiSign = (String) headerInpo.get("apiSign");

        System.out.println("===>>>应该是过完了");
        return handleResponse(exchange, chain, apiSign);
        // return chain.filter(exchange);
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

    public Map accountAPI(ServerWebExchange exchange) {
        ServerHttpRequest request = exchange.getRequest();
        HttpHeaders headers = request.getHeaders();

        Map map = new HashMap<>();

        String accessKey = headers.getFirst("accessKey");
        String requestId = headers.getFirst("requestId");
        String timeStamp = headers.getFirst("timeStamp");
        String apiSign = headers.getFirst("apiSign");

        // 直接将经过计算的apiSign传到服务器，在数据库中查找
        // 在yinapi那写出方法，在这里直接比较
        String urlApiSign = PATH_GATEWAY_SERVICE_URL + "/api/requestGateway/invokeApiSign/" + apiSign;
        Mono<Boolean> booleanMono = webClient
                .get()
                .uri(urlApiSign)
                .retrieve()
                .bodyToMono(Boolean.class)
                .map(result->{
                    if (!result){
                        throw new BusinessException(ErrorCode.PARAMS_ERROR);
                    }
                    return true;
                });


        // 添加传过来的时间戳一起计算，作比较，
        // 在yinapi那写出方法，在这里直接比较
        String genSign = headers.getFirst("genSign");
        String urlGenSign = PATH_GATEWAY_SERVICE_URL + "/api/requestGateway/checkTimeStamp/" + accessKey + "/" + timeStamp + "/" + genSign;
        webClient
                .get()
                .uri(urlGenSign)
                .retrieve()
                .bodyToMono(Boolean.class)
                .map(result->{
                    if (!result){
                        throw new BusinessException(ErrorCode.PARAMS_ERROR);
                    }
                    return true;
                });


        // 访问频率
        String urlRequestFrequency = PATH_GATEWAY_SERVICE_URL+"/api/requestGateway/isFrequenceAllowed/"
                +apiSign +"/"
                +timeStamp;
        webClient
                .get()
                .uri(urlRequestFrequency)
                .retrieve()
                .bodyToMono(Boolean.class)
                .map(result->{
                    if (!result){
                        throw new BusinessException(ErrorCode.FORBIDDEN_ERROR,"访问频率过快");
                    }
                    return true;
                });

        map.put("apiSign", apiSign);

        return map;


    }


    public Mono<Void> handleResponse(ServerWebExchange exchange, GatewayFilterChain chain, String apiSign) {
        try {
            ServerHttpResponse originalResponse = exchange.getResponse();
            // 缓存数据的工厂
            DataBufferFactory bufferFactory = originalResponse.bufferFactory();
            // 拿到响应码
            HttpStatus statusCode = originalResponse.getStatusCode();

            if (statusCode == HttpStatus.OK) {
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
                                            // invokeCount/{apiSign}
                                            String urlCount = PATH_GATEWAY_SERVICE_URL+"/api/requestGateway/invokeCount"+apiSign;
                                            // 计数统计
                                            webClient
                                                    .get()
                                                    .uri(urlCount)
                                                    .retrieve()
                                                    .bodyToMono(Boolean.class)
                                                    .map(result->{
                                                        if (!result){
                                                            throw new BusinessException(ErrorCode.PARAMS_ERROR);
                                                        }
                                                        return true;
                                                    });

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
