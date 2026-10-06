package org.example.apigateway.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class GatewayConfig {

    @Bean
    public RouteLocator customRouteLocator (RouteLocatorBuilder builder){
        log.info("Configuring Gateway routes");

        return builder.routes()
                .route("account-service", r->r
                        .path("/api/accounts/**")
                        .filters(f-> f.addRequestHeader("X-Gateway-Request","API Gateway")
                                .addResponseHeader("x-Gateway-Response","API Gateway"))
                        .uri("lb://account-service"))

                .route("transaction-service", r->r
                        .path("/api/transactions/**")
                        .filters(f-> f.addRequestHeader("X-Gateway-Request","API Gateway")
                                .addResponseHeader("x-Gateway-Response","API Gateway"))
                        .uri("lb://transaction-service"))


                .route("customer-service", r->r
                        .path("/api/customers/**")
                        .filters(f-> f.addRequestHeader("X-Gateway-Request","API Gateway")
                                .addResponseHeader("x-Gateway-Response","API Gateway"))
                        .uri("lb://customer-service"))
                .build();

    }
}
