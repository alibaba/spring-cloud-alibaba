package com.alibaba.cloud.dubbodemospringcloudgateway;

import com.alibaba.cloud.dubbospringcloudgatewayadapter.DubboGatewayFilter;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

@Configuration
public class GatewayClass {

    @Bean
    @Order(-1)
    public GlobalFilter dubboGatewayFilter(){
        return new DubboGatewayFilter();
    }

    @Bean
    public RouteLocator gatewayRoutes(RouteLocatorBuilder builder){

        return builder.routes()
                .route(r -> r.path("/placeholder/**")
                .uri("http://localhost:8081")
                .id("first"))

                .build();

    }
}
