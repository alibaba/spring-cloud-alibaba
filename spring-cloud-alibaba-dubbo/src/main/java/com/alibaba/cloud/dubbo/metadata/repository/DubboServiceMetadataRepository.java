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
package com.alibaba.cloud.dubbo.metadata.repository;

import org.apache.dubbo.common.URL;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import com.alibaba.cloud.dubbo.env.DubboCloudProperties;
import com.alibaba.cloud.dubbo.http.matcher.RequestMetadataMatcher;
import com.alibaba.cloud.dubbo.metadata.DubboRestServiceMetadata;
import com.alibaba.cloud.dubbo.metadata.RequestMetadata;
import com.alibaba.cloud.dubbo.metadata.ServiceRestMetadata;
import com.alibaba.cloud.dubbo.service.DubboMetadataService;
import com.alibaba.cloud.dubbo.service.DubboMetadataServiceExporter;
import com.alibaba.cloud.dubbo.service.DubboMetadataServiceProxy;
import com.alibaba.cloud.dubbo.util.JSONUtils;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.commons.util.InetUtils;
import org.springframework.http.HttpRequest;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import javax.annotation.PostConstruct;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.lang.String.valueOf;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableMap;
import static java.util.Collections.unmodifiableSet;
import static org.apache.dubbo.common.Constants.APPLICATION_KEY;
import static org.apache.dubbo.common.Constants.VERSION_KEY;
import static com.alibaba.cloud.dubbo.env.DubboCloudProperties.ALL_DUBBO_SERVICES;
import static com.alibaba.cloud.dubbo.http.DefaultHttpRequest.builder;
import static org.springframework.util.CollectionUtils.isEmpty;
import static org.springframework.util.StringUtils.hasText;

/**
 * Dubbo Service Metadata {@link Repository}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 */
@Repository
public class DubboServiceMetadataRepository {

    /**
     * The prefix of {@link DubboMetadataService} : "dubbo.metadata-service."
     */
    public static final String DUBBO_METADATA_SERVICE_PREFIX = "dubbo.metadata-service.";

    /**
     * The {@link URL URLs} property name of {@link DubboMetadataService} : "dubbo.metadata-service.urls"
     */
    public static final String DUBBO_METADATA_SERVICE_URLS_PROPERTY_NAME = DUBBO_METADATA_SERVICE_PREFIX + "urls";

    /**
     * The {@link String#format(String, Object...) pattern} of dubbo protocols port
     */
    public static final String DUBBO_PROTOCOLS_PORT_PROPERTY_NAME_PATTERN = "dubbo.protocols.%s.port";

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final ObjectMapper objectMapper = new ObjectMapper();

    // =================================== Registration =================================== //

    /**
     * All exported {@link URL urls} {@link Map} whose key is the return value of {@link URL#getServiceKey()} method
     * and value is the {@link List} of {@link URL URLs}
     */
    private final MultiValueMap<String, URL> allExportedURLs = new LinkedMultiValueMap<>();

    // ==================================================================================== //


    // =================================== Subscription =================================== //

    private Set<String> subscribedServices;

    /**
     * The subscribed {@link URL urls} {@link Map} of {@link DubboMetadataService},
     * whose key is the return value of {@link URL#getServiceKey()} method and value is the {@link List} of
     * {@link URL URLs}
     */
    private final MultiValueMap<String, URL> subscribedDubboMetadataServiceURLs = new LinkedMultiValueMap<>();

    // ==================================================================================== //


    // =================================== REST Metadata ================================== //

    /**
     * A Map to store REST metadata temporary, its' key is the special service name for a Dubbo service,
     * the value is a JSON content of JAX-RS or Spring MVC REST metadata from the annotated methods.
     */
    private final Set<ServiceRestMetadata> serviceRestMetadata = new LinkedHashSet<>();

    /**
     * Key is application name
     * Value is  Map<RequestMetadata, DubboRestServiceMetadata>
     */
    private Map<String, Map<RequestMetadataMatcher, DubboRestServiceMetadata>> dubboRestServiceMetadataRepository = newHashMap();

    // ==================================================================================== //


    // =================================== Dependencies =================================== //

    @Autowired
    private DubboCloudProperties dubboCloudProperties;

    @Autowired
    private DubboMetadataServiceProxy dubboMetadataConfigServiceProxy;

    @Autowired
    private DiscoveryClient discoveryClient;

    @Autowired
    private JSONUtils jsonUtils;

    @Autowired
    private InetUtils inetUtils;

    @Value("${spring.application.name}")
    private String currentApplicationName;

    @Autowired
    private DubboMetadataServiceExporter dubboMetadataServiceExporter;

    // ==================================================================================== //


    @PostConstruct
    public void init() {
        // Keep the order in following invocations
        initSubscribedServices();
        initSubscribedDubboMetadataServices();
        initDubboRestServiceMetadataRepository();
    }

    /**
     * Get the metadata {@link Map} of {@link DubboMetadataService}
     *
     * @return non-null read-only {@link Map}
     */
    public Map<String, String> getDubboMetadataServiceMetadata() {

        List<URL> dubboMetadataServiceURLs = dubboMetadataServiceExporter.export();

        // remove the exported URLs of DubboMetadataService
        removeDubboMetadataServiceURLs(dubboMetadataServiceURLs);

        Map<String, String> metadata = newHashMap();

        addDubboMetadataServiceURLsMetadata(metadata, dubboMetadataServiceURLs);
        addDubboProtocolsPortMetadata(metadata);

        return Collections.unmodifiableMap(metadata);
    }

    private void removeDubboMetadataServiceURLs(List<URL> dubboMetadataServiceURLs) {
        dubboMetadataServiceURLs.forEach(this::unexportURL);
    }

    private void addDubboMetadataServiceURLsMetadata(Map<String, String> metadata, List<URL> dubboMetadataServiceURLs) {
        String dubboMetadataServiceURLsJSON = jsonUtils.toJSON(dubboMetadataServiceURLs);
        metadata.put(DUBBO_METADATA_SERVICE_URLS_PROPERTY_NAME, dubboMetadataServiceURLsJSON);
    }

    private void addDubboProtocolsPortMetadata(Map<String, String> metadata) {

        allExportedURLs.values()
                .stream()
                .flatMap(v -> v.stream())
                .forEach(url -> {
                    String protocol = url.getProtocol();
                    String propertyName = getDubboProtocolPropertyName(protocol);
                    String propertyValue = valueOf(url.getPort());
                    metadata.put(propertyName, propertyValue);
                });
    }

    /**
     * Get the property name of Dubbo Protocol
     *
     * @param protocol Dubbo Protocol
     * @return non-null
     */
    public String getDubboProtocolPropertyName(String protocol) {
        return format(DUBBO_PROTOCOLS_PORT_PROPERTY_NAME_PATTERN, protocol);
    }

    /**
     * Publish the {@link Set} of {@link ServiceRestMetadata}
     *
     * @param serviceRestMetadataSet the {@link Set} of {@link ServiceRestMetadata}
     */
    public void publishServiceRestMetadata(Set<ServiceRestMetadata> serviceRestMetadataSet) {
        for (ServiceRestMetadata serviceRestMetadata : serviceRestMetadataSet) {
            if (!isEmpty(serviceRestMetadata.getMeta())) {
                this.serviceRestMetadata.add(serviceRestMetadata);
            }
        }
    }

    /**
     * Get the {@link Set} of {@link ServiceRestMetadata}
     *
     * @return non-null read-only {@link Set}
     */
    public Set<ServiceRestMetadata> getServiceRestMetadata() {
        return unmodifiableSet(serviceRestMetadata);
    }

//    /**
//     * Get The subscribed {@link DubboMetadataService}'s {@link URL URLs}
//     *
//     * @return non-null read-only {@link List}
//     */
//    public List<URL> getSubscribedDubboMetadataServiceURLs() {
//        return Collections.unmodifiableList(subscribedDubboMetadataServiceURLs);
//    }

    public List<URL> findSubscribedDubboMetadataServiceURLs(String serviceName, String group, String version,
                                                            String protocol) {
        String serviceKey = URL.buildKey(serviceName, group, version);
        List<URL> urls = subscribedDubboMetadataServiceURLs.get(serviceKey);

        if (isEmpty(urls)) {
            return emptyList();
        }

        return hasText(protocol) ?
                urls.stream().filter(url -> url.getProtocol().equalsIgnoreCase(protocol)).collect(Collectors.toList()) :
                unmodifiableList(urls)
                ;
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

    public void exportURL(URL url) {
        URL actualURL = url;
        InetUtils.HostInfo hostInfo = inetUtils.findFirstNonLoopbackHostInfo();
        String ipAddress = hostInfo.getIpAddress();
        // To use InetUtils to set IP if they are different
        // issue : https://github.com/spring-cloud-incubator/spring-cloud-alibaba/issues/589
        if (!Objects.equals(url.getHost(), ipAddress)) {
            actualURL = url.setHost(ipAddress);
        }
        this.allExportedURLs.add(actualURL.getServiceKey(), actualURL);
    }

    public void unexportURL(URL url) {
        String key = url.getServiceKey();
        // NPE issue : https://github.com/spring-cloud-incubator/spring-cloud-alibaba/issues/591
        List<URL> urls = allExportedURLs.get(key);
        if (!isEmpty(urls)) {
            urls.remove(url);
            allExportedURLs.addAll(key, urls);
        }
    }

    /**
     * Get all exported {@link URL urls}.
     *
     * @return non-null read-only
     */
    public Map<String, List<URL>> getAllExportedUrls() {
        return unmodifiableMap(allExportedURLs);
    }

    /**
     * Get all exported {@link URL#getServiceKey() service keys}
     *
     * @return non-null read-only
     */
    public Set<String> getAllServiceKeys() {
        return allExportedURLs.keySet();
    }

    /**
     * Get the {@link URL urls} that {@link DubboMetadataService} exported by the specified {@link ServiceInstance}
     *
     * @param serviceInstance {@link ServiceInstance}
     * @return the mutable {@link URL urls}
     */
    public List<URL> getDubboMetadataServiceURLs(ServiceInstance serviceInstance) {
        Map<String, String> metadata = serviceInstance.getMetadata();
        String dubboURLsJSON = metadata.get(DUBBO_METADATA_SERVICE_URLS_PROPERTY_NAME);
        return jsonUtils.toURLs(dubboURLsJSON);
    }

    public Integer getDubboProtocolPort(ServiceInstance serviceInstance, String protocol) {
        String protocolProperty = getDubboProtocolPropertyName(protocol);
        Map<String, String> metadata = serviceInstance.getMetadata();
        String protocolPort = metadata.get(protocolProperty);
        return hasText(protocolPort) ? Integer.valueOf(protocolPort) : null;
    }

    public List<URL> getExportedURLs(String serviceInterface, String group, String version) {
        String serviceKey = URL.buildKey(serviceInterface, group, version);
        return allExportedURLs.getOrDefault(serviceKey, Collections.emptyList());
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

    public Set<String> getSubscribedServices() {
        return Collections.unmodifiableSet(subscribedServices);
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

        Set<ServiceRestMetadata> metadata = emptySet();

        DubboMetadataService dubboMetadataService = dubboMetadataConfigServiceProxy.getProxy(serviceName);

        if (dubboMetadataService != null) {
            try {
                String serviceRestMetadataJsonConfig = dubboMetadataService.getServiceRestMetadata();
                if (hasText(serviceRestMetadataJsonConfig)) {
                    metadata = objectMapper.readValue(serviceRestMetadataJsonConfig,
                            TypeFactory.defaultInstance().constructCollectionType(LinkedHashSet.class, ServiceRestMetadata.class));
                }
            } catch (Exception e) {
                if (logger.isErrorEnabled()) {
                    logger.error(e.getMessage(), e);
                }
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
        if (ALL_DUBBO_SERVICES.equals(dubboCloudProperties.getSubscribedServices())) {
            List<String> services = discoveryClient.getServices();
            subscribedServices = new HashSet<>(services);
            if (logger.isWarnEnabled()) {
                logger.warn("Current application will subscribe all services(size:{}) in registry, " +
                                "a lot of memory and CPU cycles may be used, " +
                                "thus it's strongly recommend you using the externalized property '{}' " +
                                "to specify the services",
                        subscribedServices.size(), "dubbo.cloud.subscribed-services");
            }
        } else {
            subscribedServices = new HashSet<>(dubboCloudProperties.subscribedServices());
        }

        excludeSelf(subscribedServices);
    }

    private void excludeSelf(Set<String> subscribedServices) {
        subscribedServices.remove(currentApplicationName);
    }

    private void initSubscribedDubboMetadataServices() {
        // clear subscribedDubboMetadataServiceURLs
        subscribedDubboMetadataServiceURLs.clear();

        subscribedServices.stream()
                .map(discoveryClient::getInstances)
                .filter(this::isNotEmpty)
                .forEach(serviceInstances -> {
                    ServiceInstance serviceInstance = serviceInstances.get(0);
                    getDubboMetadataServiceURLs(serviceInstance).forEach(dubboMetadataServiceURL -> {
                        initSubscribedDubboMetadataServiceURLs(dubboMetadataServiceURL);
                        initDubboMetadataServiceProxy(dubboMetadataServiceURL);
                    });
                });
    }

    private void initSubscribedDubboMetadataServiceURLs(URL dubboMetadataServiceURL) {
        // add subscriptions
        String serviceKey = dubboMetadataServiceURL.getServiceKey();
        subscribedDubboMetadataServiceURLs.add(serviceKey, dubboMetadataServiceURL);
    }

    private void initDubboMetadataServiceProxy(URL dubboMetadataServiceURL) {
        String serviceName = dubboMetadataServiceURL.getParameter(APPLICATION_KEY);
        String version = dubboMetadataServiceURL.getParameter(VERSION_KEY);
        // Initialize DubboMetadataService with right version
        dubboMetadataConfigServiceProxy.initProxy(serviceName, version);
    }

    private void initDubboRestServiceMetadataRepository() {
        subscribedServices.forEach(this::initialize);
    }

    private boolean isNotEmpty(Collection collection) {
        return !CollectionUtils.isEmpty(collection);
    }
}
