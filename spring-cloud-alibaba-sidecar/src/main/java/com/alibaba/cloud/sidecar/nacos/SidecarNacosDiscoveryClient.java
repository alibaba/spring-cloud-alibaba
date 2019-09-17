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

package com.alibaba.cloud.sidecar.nacos;

import com.alibaba.cloud.sidecar.SidecarDiscoveryClient;
import com.alibaba.nacos.api.exception.NacosException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author www.itmuch.com
 */
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class SidecarNacosDiscoveryClient implements SidecarDiscoveryClient {
    private final SidecarNacosDiscoveryProperties sidecarNacosDiscoveryProperties;

    @Override
    public void registerInstance(String applicationName, String ip, Integer port) {
        try {
            this.sidecarNacosDiscoveryProperties.namingServiceInstance()
                    .registerInstance(applicationName, ip, port);
        } catch (NacosException e) {
            log.warn("nacos exception happens", e);
        }
    }

    @Override
    public void deregisterInstance(String applicationName, String ip, Integer port) {
        try {
            this.sidecarNacosDiscoveryProperties.namingServiceInstance()
                    .deregisterInstance(applicationName, ip, port);
        } catch (NacosException e) {
            log.warn("nacos exception happens", e);
        }
    }
}
