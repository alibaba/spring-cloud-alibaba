package com.alibaba.cloud.dubbodemospringcloudgateway;

import com.alibaba.cloud.dubbo.metadata.repository.DubboServiceMetadataRepository;
import com.alibaba.cloud.dubbo.service.DubboGenericServiceExecutionContextFactory;
import com.alibaba.cloud.dubbo.service.DubboGenericServiceFactory;
import com.alibaba.cloud.dubbospringcloudgatewayadapter.DubboGatewayFilter;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
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
    public GlobalFilter dubboGatewayFilter(DubboServiceMetadataRepository repository,
                                           DubboGenericServiceFactory serviceFactory,
                                           DubboGenericServiceExecutionContextFactory contextFactory){

        return new DubboGatewayFilter(repository,serviceFactory,contextFactory);
    }

    @Bean
    public RouteLocator gatewayRoutes(RouteLocatorBuilder builder){

        return builder.routes()
                .route(r -> r.path("/**")
                .uri("http://localhost:8081")
                .id("first"))

                .build();

    }
}
