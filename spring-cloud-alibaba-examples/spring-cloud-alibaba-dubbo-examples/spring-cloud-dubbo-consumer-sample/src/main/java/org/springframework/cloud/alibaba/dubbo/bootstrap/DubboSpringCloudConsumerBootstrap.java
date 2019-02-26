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
import org.springframework.beans.factory.annotation.Value;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;

/**
 * Dubbo Spring Cloud Consumer Bootstrap
 */
@EnableDiscoveryClient
@EnableAutoConfiguration
@EnableFeignClients
public class DubboSpringCloudConsumerBootstrap {

    @Reference(version = "1.0.0")
    private RestService restService;

    @Autowired
    @Lazy
    private FeignRestService feignRestService;

    @Autowired
    @Lazy
    private DubboFeignRestService dubboFeignRestService;

    @Value("${provider.application.name}")
    private String providerApplicationName;

    @Autowired
    @LoadBalanced
    private RestTemplate restTemplate;

    @FeignClient("${provider.application.name}")
    public interface FeignRestService {

        @GetMapping(value = "/param")
        String param(@RequestParam("param") String param);

        @PostMapping("/params")
        public String params(@RequestParam("b") String b, @RequestParam("a") int a);

        @PostMapping(value = "/request/body/map", produces = APPLICATION_JSON_UTF8_VALUE)
        User requestBody(@RequestParam("param") String param, @RequestBody Map<String, Object> data);

        @GetMapping("/headers")
        public String headers(@RequestHeader("h2") String header2,
                              @RequestHeader("h") String header,
                              @RequestParam("v") Integer value);

        @GetMapping("/path-variables/{p1}/{p2}")
        public String pathVariables(@PathVariable("p2") String path2,
                                    @PathVariable("p1") String path1,
                                    @RequestParam("v") String param);
    }

    @FeignClient("${provider.application.name}")
    @DubboTransported
    public interface DubboFeignRestService {

        @GetMapping(value = "/param")
        String param(@RequestParam("param") String param);

        @PostMapping("/params")
        String params(@RequestParam("b") String paramB, @RequestParam("a") int paramA);

        @PostMapping(value = "/request/body/map", produces = APPLICATION_JSON_UTF8_VALUE)
        User requestBody(@RequestParam("param") String param, @RequestBody Map<String, Object> data);

        @GetMapping("/headers")
        public String headers(@RequestHeader("h2") String header2,
                              @RequestParam("v") Integer value,
                              @RequestHeader("h") String header);

        @GetMapping("/path-variables/{p1}/{p2}")
        public String pathVariables(@RequestParam("v") String param,
                                    @PathVariable("p2") String path2,
                                    @PathVariable("p1") String path1);
    }

    @Bean
    public ApplicationRunner paramRunner() {
        return arguments -> {

            // To call /path-variables
            callPathVariables();

            // To call /headers
            callHeaders();

            // To call /param
            callParam();

            // To call /params
            callParams();

            // To call /request/body/map
            callRequestBodyMap();

        };
    }

    private void callPathVariables() {
        // Dubbo Service call
        System.out.println(restService.pathVariables("a", "b", "c"));
        // Spring Cloud Open Feign REST Call (Dubbo Transported)
        System.out.println(dubboFeignRestService.pathVariables("c", "b", "a"));
        // Spring Cloud Open Feign REST Call
        System.out.println(feignRestService.pathVariables("b", "a", "c"));

        // RestTemplate call
        System.out.println(restTemplate.getForEntity("http://" + providerApplicationName + "//path-variables/{p1}/{p2}?v=c", String.class, "a", "b"));
    }

    private void callHeaders() {
        // Dubbo Service call
        System.out.println(restService.headers("a", "b", 10));
        // Spring Cloud Open Feign REST Call (Dubbo Transported)
        System.out.println(dubboFeignRestService.headers("b", 10, "a"));
        // Spring Cloud Open Feign REST Call
        System.out.println(feignRestService.headers("b", "a", 10));
    }

    private void callParam() {
        // Dubbo Service call
        System.out.println(restService.param("mercyblitz"));
        // Spring Cloud Open Feign REST Call (Dubbo Transported)
        System.out.println(dubboFeignRestService.param("mercyblitz"));
        // Spring Cloud Open Feign REST Call
        System.out.println(feignRestService.param("mercyblitz"));
    }

    private void callParams() {
        // Dubbo Service call
        System.out.println(restService.params(1, "1"));
        // Spring Cloud Open Feign REST Call (Dubbo Transported)
        System.out.println(dubboFeignRestService.params("1", 1));
        // Spring Cloud Open Feign REST Call
        System.out.println(feignRestService.params("1", 1));

        // RestTemplate call
        System.out.println(restTemplate.getForEntity("http://" + providerApplicationName + "/param?param=小马哥", String.class));
    }

    private void callRequestBodyMap() {

        Map<String, Object> data = new HashMap<>();
        data.put("id", 1);
        data.put("name", "小马哥");
        data.put("age", 33);

        // Dubbo Service call
        System.out.println(restService.requestBodyMap(data, "Hello,World"));
        // Spring Cloud Open Feign REST Call (Dubbo Transported)
//        System.out.println(dubboFeignRestService.requestBody("Hello,World", data));
//         Spring Cloud Open Feign REST Call
        System.out.println(feignRestService.requestBody("Hello,World", data));

        // RestTemplate call
        System.out.println(restTemplate.postForObject("http://" + providerApplicationName + "/request/body/map?param=小马哥", data, User.class));
    }

    @Bean
    @LoadBalanced
    @DubboTransported
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    public static void main(String[] args) {
        new SpringApplicationBuilder(DubboSpringCloudConsumerBootstrap.class)
                .profiles("nacos")
                .run(args);
    }
}



