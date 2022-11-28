package com.alibaba.cloud.examples;

import org.springframework.cloud.client.discovery.ReactiveDiscoveryClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.Resource;

/**
 * Example of responsive discovery client.
 *
 * @author fangjian0423, MieAh
 */
@RestController
public class MyController {

    @Resource
    private ReactiveDiscoveryClient reactiveDiscoveryClient;

    @Resource
    private WebClient.Builder webClientBuilder;

    @GetMapping("/all-services")
    public Flux<String> allServices() {
        return reactiveDiscoveryClient.getInstances("service-provider")
                .map(serviceInstance -> serviceInstance.getHost() + ":"
                        + serviceInstance.getPort());
    }

    @GetMapping("/service-call/{name}")
    public Mono<String> serviceCall(@PathVariable("name") String name) {
        return webClientBuilder.build().get()
                .uri("http://service-provider/echo/" + name).retrieve()
                .bodyToMono(String.class);
    }

}
