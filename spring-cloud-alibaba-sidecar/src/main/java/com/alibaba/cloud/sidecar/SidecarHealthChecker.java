/*
 * Copyright (C) 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.cloud.sidecar;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.Status;
import org.springframework.core.env.ConfigurableEnvironment;
import reactor.core.scheduler.Schedulers;

import java.util.concurrent.TimeUnit;

/**
 * @author www.itmuch.com
 */
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class SidecarHealthChecker {
    private final SidecarDiscoveryClient sidecarDiscoveryClient;
    private final HealthIndicator healthIndicator;
    private final SidecarProperties sidecarProperties;
    private final ConfigurableEnvironment environment;

    public void check() {
        Schedulers.single()
                .schedulePeriodically(
                        () -> {
                            String ip = sidecarProperties.getIp();
                            Integer port = sidecarProperties.getPort();

                            Status status = healthIndicator.health().getStatus();
                            String applicationName = environment.getProperty("spring.application.name");

                            if (status.equals(Status.UP)) {
                                this.sidecarDiscoveryClient.registerInstance(applicationName, ip, port);
                                log.debug("Health check success. register this instance. applicationName = {}, ip = {}, port = {}, status = {}",
                                        applicationName, ip, port, status
                                );
                            } else {
                                log.warn("Health check failed. unregister this instance. applicationName = {}, ip = {}, port = {}, status = {}",
                                        applicationName, ip, port, status
                                );
                                this.sidecarDiscoveryClient.deregisterInstance(applicationName, ip, port);
                            }

                        },
                        0,
                        30,
                        TimeUnit.SECONDS
                );
    }
}
