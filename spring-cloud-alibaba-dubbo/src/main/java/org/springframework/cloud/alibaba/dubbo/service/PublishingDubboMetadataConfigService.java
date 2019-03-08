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
package org.springframework.cloud.alibaba.dubbo.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.cloud.alibaba.dubbo.metadata.ServiceRestMetadata;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import java.util.LinkedHashSet;
import java.util.Set;

import static org.springframework.util.ObjectUtils.isEmpty;

/**
 * Publishing {@link DubboMetadataConfigService} implementation
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 */
public class PublishingDubboMetadataConfigService implements DubboMetadataConfigService {

    /**
     * A Map to store REST metadata temporary, its' key is the special service name for a Dubbo service,
     * the value is a JSON content of JAX-RS or Spring MVC REST metadata from the annotated methods.
     */
    private final Set<ServiceRestMetadata> serviceRestMetadata = new LinkedHashSet<>();

    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostConstruct
    public void init() {
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    /**
     * Publish the {@link Set} of {@link ServiceRestMetadata}
     *
     * @param serviceRestMetadataSet the {@link Set} of {@link ServiceRestMetadata}
     */
    public void publishServiceRestMetadata(Set<ServiceRestMetadata> serviceRestMetadataSet) {
        for (ServiceRestMetadata serviceRestMetadata : serviceRestMetadataSet) {
            if (!CollectionUtils.isEmpty(serviceRestMetadata.getMeta())) {
                this.serviceRestMetadata.add(serviceRestMetadata);
            }
        }
    }

    @Override
    public String getServiceRestMetadata() {
        String serviceRestMetadataJsonConfig = null;
        try {
            if (!isEmpty(serviceRestMetadata)) {
                serviceRestMetadataJsonConfig = objectMapper.writeValueAsString(serviceRestMetadata);
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return serviceRestMetadataJsonConfig;
    }
}
