/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.cloud.alibaba.dubbo.bootstrap;

import com.alibaba.dubbo.config.annotation.Reference;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.alibaba.dubbo.annotation.DubboTransported;
import org.springframework.cloud.alibaba.dubbo.service.EchoService;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * Dubbo Spring Cloud Bootstrap
 */
@EnableDiscoveryClient
@EnableAutoConfiguration
@EnableFeignClients
@RestController
public class DubboSpringCloudBootstrap {

    @Reference(version = "1.0.0")
    private EchoService echoService;

    @Autowired
    @Lazy
    private FeignEchoService feignEchoService;

    @Autowired
    @Lazy
    private DubboFeignEchoService dubboFeignEchoService;

    @Autowired
    @LoadBalanced
    private RestTemplate restTemplate;

    @GetMapping(value = "/dubbo/call/echo")
    public String dubboEcho(@RequestParam("message") String message) {
        return echoService.echo(message);
    }

    @GetMapping(value = "/feign/call/echo")
    public String feignEcho(@RequestParam("message") String message) {
        return feignEchoService.echo(message);
    }

    @GetMapping(value = "/feign-dubbo/call/echo")
    public String feignDubboEcho(@RequestParam("message") String message) {
        return dubboFeignEchoService.echo(message);
    }

    @FeignClient("spring-cloud-alibaba-dubbo")
    public interface FeignEchoService {

        @GetMapping(value = "/echo", consumes = APPLICATION_JSON_VALUE)
        String echo(@RequestParam("message") String message);

    }

    @FeignClient("spring-cloud-alibaba-dubbo")
    @DubboTransported
    public interface DubboFeignEchoService {

        @GetMapping(value = "/echo", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_UTF8_VALUE)
        String echo(@RequestParam("message") String message);
    }

    @Bean
    public ApplicationRunner applicationRunner() {
        return arguments -> {
            // Dubbo Service call
            System.out.println(echoService.echo("mercyblitz"));
            // Spring Cloud Open Feign REST Call
            System.out.println(feignEchoService.echo("mercyblitz"));
            // Spring Cloud Open Feign REST Call (Dubbo Transported)
            System.out.println(dubboFeignEchoService.echo("mercyblitz"));
        };
    }

    @Bean
    public ApplicationRunner restTemplateRunner() {
        return arguments -> {
            System.out.println(restTemplate.getForEntity("http://spring-cloud-alibaba-dubbo/echo?message=小马哥", String.class));
            Map<String, Object> data = new HashMap<>();
            data.put("name", "小马哥");
            data.put("age", 33);
            data.put("height", 173);
            System.out.println(restTemplate.postForEntity("http://spring-cloud-alibaba-dubbo/toString", data, String.class));
        };
    }


    @Bean
    @LoadBalanced
    @DubboTransported
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    public static void main(String[] args) {
        new SpringApplicationBuilder(DubboSpringCloudBootstrap.class)
                .run(args);
    }
}



