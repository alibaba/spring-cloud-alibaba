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
import org.springframework.cloud.alibaba.dubbo.service.RestService;
import org.springframework.cloud.alibaba.dubbo.service.User;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * Dubbo Spring Cloud Bootstrap
 */
@EnableDiscoveryClient
@EnableAutoConfiguration
@EnableFeignClients
@RestController
public class DubboSpringCloudBootstrap {

    @Reference(version = "1.0.0")
    private RestService restService;

    @Autowired
    @Lazy
    private FeignRestService feignRestService;

    @Autowired
    @Lazy
    private DubboFeignRestService dubboFeignRestService;

    @Autowired
    @LoadBalanced
    private RestTemplate restTemplate;

    @GetMapping(value = "/dubbo/call/echo")
    public String dubboEcho(@RequestParam("message") String message) {
        return restService.param(message);
    }

    @GetMapping(value = "/feign/call/echo")
    public String feignEcho(@RequestParam("message") String message) {
        return feignRestService.param(message);
    }

    @GetMapping(value = "/feign-dubbo/call/echo")
    public String feignDubboEcho(@RequestParam("message") String message) {
        return dubboFeignRestService.param(message);
    }

    @FeignClient("spring-cloud-alibaba-dubbo")
    public interface FeignRestService {

        @GetMapping(value = "/param")
        String param(@RequestParam("param") String param);

        @PostMapping("/params")
        public String params(@RequestParam("b") String b, @RequestParam("a") int a);

    }

    @FeignClient("spring-cloud-alibaba-dubbo")
    @DubboTransported
    public interface DubboFeignRestService {

        @GetMapping(value = "/param")
        String param(@RequestParam("param") String param);

        @PostMapping("/params")
        public String params(@RequestParam("b") String paramB, @RequestParam("a") int paramA);
    }


    @Bean
    public ApplicationRunner paramRunner() {
        return arguments -> {

            // To call /param
            // Dubbo Service call
            System.out.println(restService.param("mercyblitz"));
            // Spring Cloud Open Feign REST Call (Dubbo Transported)
            System.out.println(dubboFeignRestService.param("mercyblitz"));
            // Spring Cloud Open Feign REST Call
            System.out.println(feignRestService.param("mercyblitz"));

            // To call /params
            // Dubbo Service call
            System.out.println(restService.params(1, "1"));
            // Spring Cloud Open Feign REST Call (Dubbo Transported)
            System.out.println(dubboFeignRestService.params("1", 1));
            // Spring Cloud Open Feign REST Call
            System.out.println(feignRestService.params("1", 1));
        };
    }

    @Bean
    public ApplicationRunner restTemplateRunner() {
        return arguments -> {

            ResponseEntity<String> entity = restTemplate.getForEntity("http://spring-cloud-alibaba-dubbo/param?param=小马哥", String.class);
            System.out.println(entity);

            Map<String, Object> data = new HashMap<>();
            data.put("id", 1);
            data.put("name", "小马哥");
            data.put("age", 33);
            User user = restTemplate.postForObject("http://spring-cloud-alibaba-dubbo/request/setBody/map", data, User.class);

            System.out.println(restTemplate.postForObject("http://spring-cloud-alibaba-dubbo/request/setBody/map", data, String.class));

            Map map = restTemplate.postForObject("http://spring-cloud-alibaba-dubbo/request/setBody/user", user, Map.class);
            System.out.println(map);
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



