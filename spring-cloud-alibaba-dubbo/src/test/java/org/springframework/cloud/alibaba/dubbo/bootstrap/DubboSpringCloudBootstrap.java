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

import com.alibaba.dubbo.config.ApplicationConfig;
import com.alibaba.dubbo.config.RegistryConfig;
import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.spring.ReferenceBean;
import com.alibaba.dubbo.config.spring.ServiceBean;
import com.alibaba.dubbo.config.spring.context.event.ServiceBeanExportedEvent;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.alibaba.dubbo.service.EchoService;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Dubbo Spring Cloud Bootstrap
 */
@EnableDiscoveryClient
@EnableAutoConfiguration
@EnableFeignClients
@EnableScheduling
@RestController
public class DubboSpringCloudBootstrap {

    @Reference(version = "1.0.0")
    private EchoService echoService;

    @Autowired
    @Lazy
    private FeignEchoService feignEchoService;

    @GetMapping(value = "/call/echo")
    public String echo(@RequestParam("message") String message) {
        return feignEchoService.echo(message);
    }

    @FeignClient("spring-cloud-alibaba-dubbo")
    public interface FeignEchoService {

        @GetMapping(value = "/echo")
        String echo(@RequestParam("message") String message);
    }

    @Bean
    public ApplicationRunner applicationRunner() {
        return arguments -> {
            // Dubbo Service call
            System.out.println(echoService.echo("mercyblitz"));
            // Spring Cloud Open Feign REST Call
            System.out.println(feignEchoService.echo("mercyblitz"));
        };
    }


    @Autowired
    private ApplicationConfig applicationConfig;

    @Autowired
    private List<RegistryConfig> registries;

    @EventListener(ServiceBeanExportedEvent.class)
    public void onServiceBeanExportedEvent(ServiceBeanExportedEvent event) {
        ServiceBean serviceBean = event.getServiceBean();
        ReferenceBean referenceBean = new ReferenceBean();
        referenceBean.setApplication(applicationConfig);
        referenceBean.setRegistries(registries);
        referenceBean.setInterface(serviceBean.getInterfaceClass());
        referenceBean.setInterface(serviceBean.getInterface());
        referenceBean.setVersion(serviceBean.getVersion());
        referenceBean.setGroup(serviceBean.getGroup());
        Object object = referenceBean.get();
        System.out.println(object);
    }


    public static void main(String[] args) {
        new SpringApplicationBuilder(DubboSpringCloudBootstrap.class)
                .run(args);
    }
}



