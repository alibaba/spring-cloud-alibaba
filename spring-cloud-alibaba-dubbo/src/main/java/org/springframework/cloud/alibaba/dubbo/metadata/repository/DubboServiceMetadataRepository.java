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

import com.alibaba.dubbo.config.spring.ReferenceBean;
import com.alibaba.dubbo.rpc.service.GenericService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.alibaba.dubbo.http.matcher.RequestMetadataMatcher;
import org.springframework.cloud.alibaba.dubbo.metadata.RequestMetadata;
import org.springframework.cloud.alibaba.dubbo.metadata.RestMethodMetadata;
import org.springframework.cloud.alibaba.dubbo.metadata.ServiceRestMetadata;
import org.springframework.cloud.alibaba.dubbo.metadata.service.MetadataConfigService;
import org.springframework.http.HttpRequest;
import org.springframework.stereotype.Repository;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import static org.springframework.cloud.alibaba.dubbo.http.DefaultHttpRequest.builder;
import static org.springframework.cloud.alibaba.dubbo.registry.SpringCloudRegistry.getServiceGroup;
import static org.springframework.cloud.alibaba.dubbo.registry.SpringCloudRegistry.getServiceInterface;
import static org.springframework.cloud.alibaba.dubbo.registry.SpringCloudRegistry.getServiceSegments;
import static org.springframework.cloud.alibaba.dubbo.registry.SpringCloudRegistry.getServiceVersion;
import static org.springframework.util.CollectionUtils.isEmpty;

/**
 * Dubbo Service Metadata {@link Repository}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 */
@Repository
public class DubboServiceMetadataRepository {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * Key is application name
     * Value is  Map<RequestMetadata, ReferenceBean<GenericService>>
     */
    private Map<String, Map<RequestMetadataMatcher, ReferenceBean<GenericService>>> referenceBeansRepository = newHashMap();

    /**
     * Key is application name
     * Value is  Map<RequestMetadata, RestMethodMetadata>
     */
    private Map<String, Map<RequestMetadataMatcher, RestMethodMetadata>> restMethodMetadataRepository = newHashMap();

    @Autowired
    private MetadataConfigService metadataConfigService;

    /**
     * Initialize the specified service's Dubbo Service Metadata
     *
     * @param serviceName the service name
     */
    public void initialize(String serviceName) {

        if (referenceBeansRepository.containsKey(serviceName)) {
            return;
        }

        Set<ServiceRestMetadata> serviceRestMetadataSet = metadataConfigService.getServiceRestMetadata(serviceName);

        if (isEmpty(serviceRestMetadataSet)) {
            if (logger.isWarnEnabled()) {
                logger.warn("The Spring Cloud application[name : {}] does not expose The REST metadata in the Dubbo services."
                        , serviceName);
            }
            return;
        }

        Map<RequestMetadataMatcher, ReferenceBean<GenericService>> genericServicesMap = getReferenceBeansMap(serviceName);

        Map<RequestMetadataMatcher, RestMethodMetadata> restMethodMetadataMap = getRestMethodMetadataMap(serviceName);

        for (ServiceRestMetadata serviceRestMetadata : serviceRestMetadataSet) {

            ReferenceBean<GenericService> referenceBean = adaptReferenceBean(serviceRestMetadata);

            serviceRestMetadata.getMeta().forEach(restMethodMetadata -> {
                RequestMetadata requestMetadata = restMethodMetadata.getRequest();
                RequestMetadataMatcher matcher = new RequestMetadataMatcher(requestMetadata);
                genericServicesMap.put(matcher, referenceBean);
                restMethodMetadataMap.put(matcher, restMethodMetadata);
            });
        }
    }

    public ReferenceBean<GenericService> getReferenceBean(String serviceName, RequestMetadata requestMetadata) {
        return match(referenceBeansRepository, serviceName, requestMetadata);
    }

    public RestMethodMetadata getRestMethodMetadata(String serviceName, RequestMetadata requestMetadata) {
        return match(restMethodMetadataRepository, serviceName, requestMetadata);
    }

    private static <T> T match(Map<String, Map<RequestMetadataMatcher, T>> repository, String serviceName,
                               RequestMetadata requestMetadata) {
        Map<RequestMetadataMatcher, T> map = repository.get(serviceName);
        if (isEmpty(map)) {
            return null;
        }
        RequestMetadataMatcher matcher = new RequestMetadataMatcher(requestMetadata);
        T object = map.get(matcher);
        if (object == null) { // Can't match exactly
            // Require to match one by one
            for (Map.Entry<RequestMetadataMatcher, T> entry : map.entrySet()) {
                RequestMetadataMatcher possibleMatcher = entry.getKey();
                HttpRequest request = builder()
                        .method(requestMetadata.getMethod())
                        .path(requestMetadata.getPath())
                        .params(requestMetadata.getParams())
                        .headers(requestMetadata.getHeaders())
                        .build();

                if (possibleMatcher.match(request)) {
                    object = entry.getValue();
                    break;
                }
            }
        }
        return object;
    }

    private ReferenceBean<GenericService> adaptReferenceBean(ServiceRestMetadata serviceRestMetadata) {
        String dubboServiceName = serviceRestMetadata.getName();
        String[] segments = getServiceSegments(dubboServiceName);
        String interfaceName = getServiceInterface(segments);
        String version = getServiceVersion(segments);
        String group = getServiceGroup(segments);

        ReferenceBean<GenericService> referenceBean = new ReferenceBean<GenericService>();
        referenceBean.setGeneric(true);
        referenceBean.setInterface(interfaceName);
        referenceBean.setVersion(version);
        referenceBean.setGroup(group);

        return referenceBean;
    }

    private Map<RequestMetadataMatcher, ReferenceBean<GenericService>> getReferenceBeansMap(String serviceName) {
        return getMap(referenceBeansRepository, serviceName);
    }

    private Map<RequestMetadataMatcher, RestMethodMetadata> getRestMethodMetadataMap(String serviceName) {
        return getMap(restMethodMetadataRepository, serviceName);
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
