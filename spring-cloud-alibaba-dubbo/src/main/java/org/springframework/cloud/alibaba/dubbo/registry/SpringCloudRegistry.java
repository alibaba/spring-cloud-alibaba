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
package org.springframework.cloud.alibaba.dubbo.registry;

import org.apache.dubbo.common.URL;
import org.apache.dubbo.registry.NotifyListener;
import org.apache.dubbo.registry.RegistryFactory;

import org.springframework.cloud.alibaba.dubbo.metadata.repository.DubboServiceMetadataRepository;
import org.springframework.cloud.alibaba.dubbo.util.JSONUtils;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Collectors;

import static java.util.Collections.emptyMap;
import static org.apache.dubbo.common.Constants.APPLICATION_KEY;
import static org.springframework.util.CollectionUtils.isEmpty;

/**
 * Dubbo {@link RegistryFactory} uses Spring Cloud Service Registration abstraction, whose protocol is "spring-cloud"
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 */
public class SpringCloudRegistry extends AbstractSpringCloudRegistry {

    /**
     * The property name of Dubbo {@link URL URLs} metadata
     */
    public static final String DUBBO_URLS_METADATA_PROPERTY_NAME = "dubbo-urls";

    /**
     * The parameter name of the services of Dubbo Provider
     */
    public static final String DUBBO_PROVIDER_SERVICES_PARAM_NAME = "dubbo-provider-services";

    /**
     * All services of Dubbo Provider
     */
    public static final String ALL_DUBBO_PROVIDER_SERVICES = "*";

    private final DubboServiceMetadataRepository dubboServiceMetadataRepository;

    private final JSONUtils jsonUtils;

    private final Set<String> dubboProviderServices;

    private final Map<String, String> dubboServiceKeysCache;

    public SpringCloudRegistry(URL url, DiscoveryClient discoveryClient,
                               ScheduledExecutorService servicesLookupScheduler,
                               DubboServiceMetadataRepository dubboServiceMetadataRepository,
                               ConfigurableApplicationContext applicationContext) {
        super(url, discoveryClient, servicesLookupScheduler);
        this.dubboServiceMetadataRepository = dubboServiceMetadataRepository;
        this.jsonUtils = applicationContext.getBean(JSONUtils.class);
        this.dubboProviderServices = getDubboProviderServices();
        this.dubboServiceKeysCache = this.initDubboServiceKeysCache();
    }

    private Map<String, String> initDubboServiceKeysCache() {

        if (isEmpty(dubboProviderServices)) {
            return emptyMap();
        }

        Map<String, String> newCache = new HashMap<>();

        dubboProviderServices.stream()
                .map(this::getServiceInstances)
                .filter(this::isNotEmpty)
                .forEach(serviceInstances -> {
                    ServiceInstance serviceInstance = serviceInstances.get(0);
                    getURLs(serviceInstance).forEach(url -> {
                        String serviceKey = url.getServiceKey();
                        String serviceName = url.getParameter(APPLICATION_KEY);
                        newCache.put(serviceKey, serviceName);
                    });
                });

        return newCache;
    }

    private boolean isNotEmpty(Collection collection) {
        return !CollectionUtils.isEmpty(collection);
    }

    private List<URL> getURLs(ServiceInstance serviceInstance) {
        Map<String, String> metadata = serviceInstance.getMetadata();
        String dubboURLsJSON = metadata.get(DUBBO_URLS_METADATA_PROPERTY_NAME);
        List<String> urlValues = jsonUtils.toList(dubboURLsJSON);
        return urlValues.stream().map(URL::valueOf).collect(Collectors.toList());
    }

    private Set<String> getDubboProviderServices() {
        URL registryURL = getUrl();
        String services = registryURL.getParameter(DUBBO_PROVIDER_SERVICES_PARAM_NAME, ALL_DUBBO_PROVIDER_SERVICES);
        return ALL_DUBBO_PROVIDER_SERVICES.equalsIgnoreCase(services) ?
                getAllServiceNames() : StringUtils.commaDelimitedListToSet(services);
    }

    @Override
    protected void doRegister0(URL url) {
        dubboServiceMetadataRepository.registerURL(url);
    }

    @Override
    protected void doUnregister0(URL url) {
        dubboServiceMetadataRepository.unregisterURL(url);
    }

    @Override
    protected boolean supports(String serviceName) {
        return dubboProviderServices.contains(serviceName);
    }

    @Override
    protected String getServiceName(URL url) {
        String serviceKey = url.getServiceKey();
        return dubboServiceKeysCache.get(serviceKey);
    }

    @Override
    protected void notifySubscriber(URL url, NotifyListener listener, List<ServiceInstance> serviceInstances) {
        List<URL> urls = serviceInstances.stream().map(this::getURLs)
                .flatMap(List::stream)
                .collect(Collectors.toList());
        notify(url, listener, urls);
    }
}
