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
package org.springframework.cloud.alibaba.dubbo.metadata.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.alibaba.dubbo.http.matcher.RequestMetadataMatcher;
import org.springframework.cloud.alibaba.dubbo.metadata.DubboServiceMetadata;
import org.springframework.cloud.alibaba.dubbo.metadata.RequestMetadata;
import org.springframework.cloud.alibaba.dubbo.metadata.ServiceRestMetadata;
import org.springframework.cloud.alibaba.dubbo.service.DubboMetadataConfigService;
import org.springframework.cloud.alibaba.dubbo.service.DubboMetadataConfigServiceProxy;
import org.springframework.http.HttpRequest;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import static org.springframework.cloud.alibaba.dubbo.http.DefaultHttpRequest.builder;
import static org.springframework.util.CollectionUtils.isEmpty;

/**
 * Dubbo Service Metadata {@link Repository}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 */
@Repository
public class DubboServiceMetadataRepository {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Key is application name
     * Value is  Map<RequestMetadata, DubboServiceMetadata>
     */
    private Map<String, Map<RequestMetadataMatcher, DubboServiceMetadata>> repository = newHashMap();

    @Autowired
    private DubboMetadataConfigServiceProxy dubboMetadataConfigServiceProxy;

    /**
     * Initialize the specified service's Dubbo Service Metadata
     *
     * @param serviceName the service name
     */
    public void initialize(String serviceName) {

        if (repository.containsKey(serviceName)) {
            return;
        }

        Set<ServiceRestMetadata> serviceRestMetadataSet = getServiceRestMetadataSet(serviceName);

        if (isEmpty(serviceRestMetadataSet)) {
            if (logger.isWarnEnabled()) {
                logger.warn("The Spring application[name : {}] does not expose The REST metadata in the Dubbo services."
                        , serviceName);
            }
            return;
        }

        Map<RequestMetadataMatcher, DubboServiceMetadata> metadataMap = getMetadataMap(serviceName);

        for (ServiceRestMetadata serviceRestMetadata : serviceRestMetadataSet) {

            serviceRestMetadata.getMeta().forEach(restMethodMetadata -> {
                RequestMetadata requestMetadata = restMethodMetadata.getRequest();
                RequestMetadataMatcher matcher = new RequestMetadataMatcher(requestMetadata);
                DubboServiceMetadata metadata = new DubboServiceMetadata(serviceRestMetadata, restMethodMetadata);
                metadataMap.put(matcher, metadata);
            });
        }

        if (logger.isInfoEnabled()) {
            logger.info("The REST metadata in the dubbo services has been loaded in the Spring application[name : {}]", serviceName);
        }
    }

    /**
     * Get a {@link DubboServiceMetadata} by the specified service name if {@link RequestMetadata} matched
     *
     * @param serviceName     service name
     * @param requestMetadata {@link RequestMetadata} to be matched
     * @return {@link DubboServiceMetadata} if matched, or <code>null</code>
     */
    public DubboServiceMetadata get(String serviceName, RequestMetadata requestMetadata) {
        return match(repository, serviceName, requestMetadata);
    }

    private <T> T match(Map<String, Map<RequestMetadataMatcher, T>> repository, String serviceName,
                        RequestMetadata requestMetadata) {

        Map<RequestMetadataMatcher, T> map = repository.get(serviceName);

        T object = null;

        if (!isEmpty(map)) {
            RequestMetadataMatcher matcher = new RequestMetadataMatcher(requestMetadata);
            object = map.get(matcher);
            if (object == null) { // Can't match exactly
                // Require to match one by one
                HttpRequest request = builder()
                        .method(requestMetadata.getMethod())
                        .path(requestMetadata.getPath())
                        .params(requestMetadata.getParams())
                        .headers(requestMetadata.getHeaders())
                        .build();

                for (Map.Entry<RequestMetadataMatcher, T> entry : map.entrySet()) {
                    RequestMetadataMatcher possibleMatcher = entry.getKey();
                    if (possibleMatcher.match(request)) {
                        object = entry.getValue();
                        break;
                    }
                }
            }
        }

        if (object == null) {
            if (logger.isWarnEnabled()) {
                logger.warn("DubboServiceMetadata can't be found in the Spring application [%s] and %s",
                        serviceName, requestMetadata);
            }
        }

        return object;
    }

    private Map<RequestMetadataMatcher, DubboServiceMetadata> getMetadataMap(String serviceName) {
        return getMap(repository, serviceName);
    }

    private Set<ServiceRestMetadata> getServiceRestMetadataSet(String serviceName) {
        DubboMetadataConfigService dubboMetadataConfigService = dubboMetadataConfigServiceProxy.newProxy(serviceName);
        String serviceRestMetadataJsonConfig = dubboMetadataConfigService.getServiceRestMetadata();

        Set<ServiceRestMetadata> metadata;
        try {
            metadata = objectMapper.readValue(serviceRestMetadataJsonConfig,
                    TypeFactory.defaultInstance().constructCollectionType(LinkedHashSet.class, ServiceRestMetadata.class));
        } catch (Exception e) {
            if (logger.isErrorEnabled()) {
                logger.error(e.getMessage(), e);
            }
            metadata = Collections.emptySet();
        }
        return metadata;
    }

    private static <K, V> Map<K, V> getMap(Map<String, Map<K, V>> repository, String key) {
        return getOrDefault(repository, key, newHashMap());
    }

    private static <K, V> V getOrDefault(Map<K, V> source, K key, V defaultValue) {
        V value = source.get(key);
        if (value == null) {
            value = defaultValue;
            source.put(key, value);
        }
        return value;
    }

    private static <K, V> Map<K, V> newHashMap() {
        return new LinkedHashMap<>();
    }

}
