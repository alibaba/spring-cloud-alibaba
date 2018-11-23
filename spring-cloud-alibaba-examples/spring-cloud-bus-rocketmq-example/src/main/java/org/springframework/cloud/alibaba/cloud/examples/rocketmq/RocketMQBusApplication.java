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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.bus.event.AckRemoteApplicationEvent;
import org.springframework.cloud.bus.jackson.RemoteApplicationEventScan;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * RocketMQ Bus Spring Application
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 0.2.1
 */
@RestController
@EnableAutoConfiguration
@RemoteApplicationEventScan(basePackages = "org.springframework.cloud.alibaba.cloud.examples.rocketmq")
public class RocketMQBusApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder(RocketMQBusApplication.class)
                .properties("server.port=0") // Random server port
                .properties("management.endpoints.web.exposure.include=*") // exposure includes all
                .properties("spring.cloud.bus.trace.enabled=true") // Enable trace
                .run(args);
    }

    @Autowired
    private ApplicationEventPublisher publisher;

    @Value("${spring.cloud.bus.id}")
    private String originService;

    @Value("${server.port}")
    private int localServerPort;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Publish the {@link UserRemoteApplicationEvent}
     *
     * @param name        the user name
     * @param destination the destination
     * @return If published
     */
    @GetMapping("/bus/event/publish/user")
    public boolean publish(@RequestParam String name, @RequestParam(required = false) String destination) {
        User user = new User();
        user.setId(System.currentTimeMillis());
        user.setName(name);
        publisher.publishEvent(new UserRemoteApplicationEvent(user, originService, destination));
        return true;
    }

    /**
     * Listener on the {@link UserRemoteApplicationEvent}
     *
     * @param event {@link UserRemoteApplicationEvent}
     */
    @EventListener
    public void onEvent(UserRemoteApplicationEvent event) {
        System.out.printf("Server [port : %d] listeners on %s\n", localServerPort, event.getUser());
    }

    @EventListener
    public void onAckEvent(AckRemoteApplicationEvent event) throws JsonProcessingException {
        System.out.printf("Server [port : %d] listeners on %s\n", localServerPort, objectMapper.writeValueAsString(event));
    }
}
