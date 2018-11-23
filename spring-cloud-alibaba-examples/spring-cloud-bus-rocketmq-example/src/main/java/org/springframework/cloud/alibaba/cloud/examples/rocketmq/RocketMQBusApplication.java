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
package org.springframework.cloud.alibaba.cloud.examples.rocketmq;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.bus.jackson.RemoteApplicationEventScan;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;

/**
 * RocketMQ Bus Spring Application
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 0.2.1
 */
@EnableAutoConfiguration
@RemoteApplicationEventScan(basePackages = "org.springframework.cloud.alibaba.cloud.examples.rocketmq")
public class RocketMQBusApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder(RocketMQBusApplication.class)
                .run(args);
    }

    /**
     * Publish the {@link UserRemoteApplicationEvent} to all instances of currentService.
     *
     * @param publisher      {@link ApplicationEventPublisher}
     * @param currentService Current application Name
     * @return {@link ApplicationRunner} instance
     */
    @Bean
    public ApplicationRunner publishEventRunner(ApplicationEventPublisher publisher,
                                                @Value("${spring.application.name}") String currentService) {
        return args -> {
            User user = new User();
            user.setName("Mercy Ma");
            for (int i = 1; i < 10; i++) {
                user.setId(Long.valueOf(i));
                publisher.publishEvent(new UserRemoteApplicationEvent(user, currentService, currentService + ":**"));
            }
        };
    }

    /**
     * Listener on the {@link UserRemoteApplicationEvent}
     *
     * @param event {@link UserRemoteApplicationEvent}
     */
    @EventListener
    public void onEvent(UserRemoteApplicationEvent event) {
        System.out.println("Listener on User : " + event.getUser());
    }
}
