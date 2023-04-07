package com.yin.yingateway.config;

import com.yin.yingateway.service.TestGateway;
import com.yin.yingateway.service.UserApiGateway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class GatewayConfig {

    @Autowired
    WebClient.Builder webClientBuilder;
    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {

        // WebClient webClient = webClientBuilder.build();
        return builder.routes()
                .route("route1", r -> r.path("/api/**")
                        .filters(f -> f.filter(new UserApiGateway(webClientBuilder)))
                        .uri("http://localhost:8090"))
                .route("route2", r -> r.path("/route1")
                        .filters(f -> f.filter(new TestGateway()))
                        .uri("http://localhost:8082"))
                .build();
    }
}
