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
package org.springframework.cloud.alibaba.dubbo.metadata.service;

import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.exception.NacosException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.alibaba.dubbo.metadata.ServiceRestMetadata;
import org.springframework.cloud.alibaba.nacos.NacosConfigProperties;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import static com.alibaba.nacos.api.common.Constants.DEFAULT_GROUP;

/**
 * Nacos {@link MetadataConfigService}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 */
public class NacosMetadataConfigService implements MetadataConfigService {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private NacosConfigProperties nacosConfigProperties;

    private ConfigService configService;

    @PostConstruct
    public void init() {
        this.configService = nacosConfigProperties.configServiceInstance();
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    /**
     * Get the data Id of service rest metadata
     */
    private static String getServiceRestMetadataDataId(String serviceName) {
        return "metadata:rest:" + serviceName + ".json";
    }

    @Override
    public void publishServiceRestMetadata(String serviceName, Set<ServiceRestMetadata> serviceRestMetadata) {
        String dataId = getServiceRestMetadataDataId(serviceName);
        String json = writeValueAsString(serviceRestMetadata);
        try {
            configService.publishConfig(dataId, DEFAULT_GROUP, json);
        } catch (NacosException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Set<ServiceRestMetadata> getServiceRestMetadata(String serviceName) {
        Set<ServiceRestMetadata> metadata = Collections.emptySet();
        String dataId = getServiceRestMetadataDataId(serviceName);
        try {
            String json = configService.getConfig(dataId, DEFAULT_GROUP, 1000 * 3);
            metadata = objectMapper.readValue(json,
                    TypeFactory.defaultInstance().constructCollectionType(LinkedHashSet.class, ServiceRestMetadata.class));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return metadata;
    }

    private String writeValueAsString(Object object) {
        String content = null;
        try {
            content = objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(e);
        }
        return content;
    }
}
