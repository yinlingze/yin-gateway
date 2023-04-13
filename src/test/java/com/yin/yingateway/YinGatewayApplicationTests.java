package com.yin.yingateway;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import javax.annotation.Resource;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

@SpringBootTest
class YinGatewayApplicationTests {

    @Autowired
    private WebClient.Builder webClientBuilder;
    // @Resource
    // RestTemplate restTemplate;

    @Resource
    RestTemplateBuilder restTemplateBuilder;
    @Test
    void contextLoads() throws InterruptedException {
        String url = "http://springboot-init11111/api/requestGateway/invokeCount/"+"02487cc80dcc5a51238e3fcae7bcd441dc82723d672518898d7f938c1415a84e";

        // Boolean forObject = restTemplate.getForObject(url, Boolean.class);
        // System.out.println(forObject);
        RestTemplate restTemplate = restTemplateBuilder
                .setConnectTimeout(Duration.ofSeconds(10))
                .setReadTimeout(Duration.ofSeconds(30))
                .build();

        CompletableFuture<Boolean> booleanCompletableFuture = CompletableFuture.supplyAsync(
                () -> restTemplate.getForObject(url, Boolean.class)
        );
        // System.out.println(booleanCompletableFuture);
    }

}
