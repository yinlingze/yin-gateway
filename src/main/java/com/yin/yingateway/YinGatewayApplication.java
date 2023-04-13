package com.yin.yingateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableAsync;


@SpringBootApplication
@EnableAsync(proxyTargetClass = true)
@EnableFeignClients
public class YinGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(YinGatewayApplication.class, args);
    }

}
