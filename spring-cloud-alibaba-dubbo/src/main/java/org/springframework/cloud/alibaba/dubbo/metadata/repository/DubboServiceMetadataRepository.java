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

import org.apache.dubbo.common.URL;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.alibaba.dubbo.env.DubboCloudProperties;
import org.springframework.cloud.alibaba.dubbo.http.matcher.RequestMetadataMatcher;
import org.springframework.cloud.alibaba.dubbo.metadata.DubboRestServiceMetadata;
import org.springframework.cloud.alibaba.dubbo.metadata.RequestMetadata;
import org.springframework.cloud.alibaba.dubbo.metadata.ServiceRestMetadata;
import org.springframework.cloud.alibaba.dubbo.service.DubboMetadataConfigService;
import org.springframework.cloud.alibaba.dubbo.service.DubboMetadataConfigServiceProxy;
import org.springframework.cloud.alibaba.dubbo.util.JSONUtils;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.HttpRequest;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.apache.dubbo.common.Constants.APPLICATION_KEY;
import static org.springframework.cloud.alibaba.dubbo.env.DubboCloudProperties.ALL_DUBBO_SERVICES;
import static org.springframework.cloud.alibaba.dubbo.http.DefaultHttpRequest.builder;
import static org.springframework.util.CollectionUtils.isEmpty;

/**
 * Dubbo Service Metadata {@link Repository}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 */
@Repository
public class DubboServiceMetadataRepository {

    /**
     * The property name of Dubbo {@link URL URLs} metadata
     */
    public static final String DUBBO_URLS_METADATA_PROPERTY_NAME = "dubbo.urls";

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final Set<URL> registeredURLs = new LinkedHashSet<>();

    private final Map<String, String> dubboServiceKeysRepository = new HashMap<>();

    /**
     * Key is application name
     * Value is  Map<RequestMetadata, DubboRestServiceMetadata>
     */
    private Map<String, Map<RequestMetadataMatcher, DubboRestServiceMetadata>> dubboRestServiceMetadataRepository = newHashMap();

    private Set<String> subscribedServices;

    @Autowired
    private DubboCloudProperties dubboCloudProperties;

    @Autowired
    private DubboMetadataConfigServiceProxy dubboMetadataConfigServiceProxy;

    @Autowired
    private DiscoveryClient discoveryClient;

    @Autowired
    private JSONUtils jsonUtils;

    @Value("${spring.application.name}")
    private String currentApplicationName;

    @PostConstruct
    public void init() {
        initSubscribedServices();
        initDubboServiceKeysRepository();
        retainAvailableSubscribedServices();
        initDubboRestServiceMetadataRepository();
    }

    /**
     * The specified service is subscribe or not
     *
     * @param serviceName the service name
     * @return
     */
    public boolean isSubscribedService(String serviceName) {
        return subscribedServices.contains(serviceName);
    }

    /**
     * Get the service name by the {@link URL#getServiceKey() service key}
     *
     * @param url {@link URL}
     * @return the service name if found
     */
    public String getServiceName(URL url) {
        return getServiceName(url.getServiceKey());
    }

    /**
     * Get the service name by the {@link URL#getServiceKey() service key}
     *
     * @param serviceKey the {@link URL#getServiceKey() service key}
     * @return the service name if found
     */
    public String getServiceName(String serviceKey) {
        return dubboServiceKeysRepository.get(serviceKey);
    }

    public void registerURL(URL url) {
        this.registeredURLs.add(url);
    }

    public void unregisterURL(URL url) {
        this.registeredURLs.remove(url);
    }

    public Collection<URL> getRegisteredUrls() {
        return Collections.unmodifiableSet(registeredURLs);
    }

    /**
     * Build the {@link URL urls} by the specified {@link ServiceInstance}
     *
     * @param serviceInstance {@link ServiceInstance}
     * @return the mutable {@link URL urls}
     */
    public List<URL> buildURLs(ServiceInstance serviceInstance) {
        Map<String, String> metadata = serviceInstance.getMetadata();
        String dubboURLsJSON = metadata.get(DUBBO_URLS_METADATA_PROPERTY_NAME);
        List<String> urlValues = jsonUtils.toList(dubboURLsJSON);
        return urlValues.stream().map(URL::valueOf).collect(Collectors.toList());
    }

    /**
     * Initialize the specified service's {@link ServiceRestMetadata}
     *
     * @param serviceName the service name
     */
    public void initialize(String serviceName) {

        if (dubboRestServiceMetadataRepository.containsKey(serviceName)) {
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

        Map<RequestMetadataMatcher, DubboRestServiceMetadata> metadataMap = getMetadataMap(serviceName);

        for (ServiceRestMetadata serviceRestMetadata : serviceRestMetadataSet) {

            serviceRestMetadata.getMeta().forEach(restMethodMetadata -> {
                RequestMetadata requestMetadata = restMethodMetadata.getRequest();
                RequestMetadataMatcher matcher = new RequestMetadataMatcher(requestMetadata);
                DubboRestServiceMetadata metadata = new DubboRestServiceMetadata(serviceRestMetadata, restMethodMetadata);
                metadataMap.put(matcher, metadata);
            });
        }

        if (logger.isInfoEnabled()) {
            logger.info("The REST metadata in the dubbo services has been loaded in the Spring application[name : {}]", serviceName);
        }
    }

    /**
     * Get a {@link DubboRestServiceMetadata} by the specified service name if {@link RequestMetadata} matched
     *
     * @param serviceName     service name
     * @param requestMetadata {@link RequestMetadata} to be matched
     * @return {@link DubboRestServiceMetadata} if matched, or <code>null</code>
     */
    public DubboRestServiceMetadata get(String serviceName, RequestMetadata requestMetadata) {
        return match(dubboRestServiceMetadataRepository, serviceName, requestMetadata);
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

    private Map<RequestMetadataMatcher, DubboRestServiceMetadata> getMetadataMap(String serviceName) {
        return getMap(dubboRestServiceMetadataRepository, serviceName);
    }

    private Set<ServiceRestMetadata> getServiceRestMetadataSet(String serviceName) {
        DubboMetadataConfigService dubboMetadataConfigService = dubboMetadataConfigServiceProxy.newProxy(serviceName);

        Set<ServiceRestMetadata> metadata = Collections.emptySet();
        try {
            String serviceRestMetadataJsonConfig = dubboMetadataConfigService.getServiceRestMetadata();
            metadata = objectMapper.readValue(serviceRestMetadataJsonConfig,
                    TypeFactory.defaultInstance().constructCollectionType(LinkedHashSet.class, ServiceRestMetadata.class));
        } catch (Exception e) {
            if (logger.isErrorEnabled()) {
                logger.error(e.getMessage(), e);
            }
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

    private void initSubscribedServices() {
        // If subscribes all services
        if (ALL_DUBBO_SERVICES.equalsIgnoreCase(dubboCloudProperties.getSubscribedServices())) {
            subscribedServices = new HashSet<>(discoveryClient.getServices());
        } else {
            subscribedServices = new HashSet<>(dubboCloudProperties.subscribedServices());
        }

        excludeSelf(subscribedServices);
    }

    private void excludeSelf(Set<String> subscribedServices) {
        subscribedServices.remove(currentApplicationName);
    }

    private void initDubboServiceKeysRepository() {
        subscribedServices.stream()
                .map(discoveryClient::getInstances)
                .filter(this::isNotEmpty)
                .forEach(serviceInstances -> {
                    ServiceInstance serviceInstance = serviceInstances.get(0);
                    buildURLs(serviceInstance).forEach(url -> {
                        String serviceKey = url.getServiceKey();
                        String serviceName = url.getParameter(APPLICATION_KEY);
                        dubboServiceKeysRepository.put(serviceKey, serviceName);
                    });
                });
    }

    private void retainAvailableSubscribedServices() {
        // dubboServiceKeysRepository.values() returns the available services(possible duplicated ones)
        subscribedServices = new HashSet<>(dubboServiceKeysRepository.values());
    }

    private void initDubboRestServiceMetadataRepository() {
        subscribedServices.forEach(this::initialize);
    }

    private boolean isNotEmpty(Collection collection) {
        return !CollectionUtils.isEmpty(collection);
    }
}
