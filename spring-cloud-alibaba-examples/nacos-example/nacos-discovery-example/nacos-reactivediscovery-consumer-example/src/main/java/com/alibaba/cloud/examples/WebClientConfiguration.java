package com.alibaba.cloud.examples;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Configuration for web client.
 *
 * @author fangjian0423, MieAh
 */
public class WebClientConfiguration {

    @Bean
    @LoadBalanced
    public WebClient.Builder webClient() {
        return WebClient.builder();
    }
}
