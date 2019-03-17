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

import org.apache.dubbo.common.Constants;
import org.apache.dubbo.common.URL;
import org.apache.dubbo.common.utils.UrlUtils;
import org.apache.dubbo.registry.NotifyListener;
import org.apache.dubbo.registry.RegistryFactory;
import org.apache.dubbo.registry.support.FailbackRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.alibaba.dubbo.registry.handler.DubboRegistryServiceIdHandler;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.serviceregistry.Registration;
import org.springframework.cloud.client.serviceregistry.ServiceRegistry;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static java.util.Collections.singletonList;
import static org.apache.dubbo.common.Constants.CONFIGURATORS_CATEGORY;
import static org.apache.dubbo.common.Constants.CONSUMERS_CATEGORY;
import static org.apache.dubbo.common.Constants.PROVIDERS_CATEGORY;
import static org.apache.dubbo.common.Constants.PROVIDER_SIDE;
import static org.apache.dubbo.common.Constants.ROUTERS_CATEGORY;
import static org.apache.dubbo.common.Constants.SIDE_KEY;

/**
 * Dubbo {@link RegistryFactory} uses Spring Cloud Service Registration abstraction, whose protocol is "spring-cloud"
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 */
public class SpringCloudRegistry extends FailbackRegistry {

    /**
     * The parameter name of {@link #allServicesLookupInterval}
     */
    public static final String ALL_SERVICES_LOOKUP_INTERVAL_PARAM_NAME = "dubbo.all.services.lookup.interval";

    /**
     * The parameter name of {@link #registeredServicesLookupInterval}
     */
    public static final String REGISTERED_SERVICES_LOOKUP_INTERVAL_PARAM_NAME = "dubbo.registered.services.lookup.interval";

    /**
     * All supported categories
     */
    public static final String[] ALL_SUPPORTED_CATEGORIES = of(
            PROVIDERS_CATEGORY,
            CONSUMERS_CATEGORY,
            ROUTERS_CATEGORY,
            CONFIGURATORS_CATEGORY
    );

    private final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * The interval in second of lookup service names(only for Dubbo-OPS)
     */
    private final long allServicesLookupInterval;

    private final long registeredServicesLookupInterval;

    private final ServiceRegistry<Registration> serviceRegistry;

    private final RegistrationFactory registrationFactory;

    private final DiscoveryClient discoveryClient;

    private final DubboRegistryServiceIdHandler dubboRegistryServiceIdHandler;

    private final ScheduledExecutorService servicesLookupScheduler;

    private final ConfigurableApplicationContext applicationContext;

    public SpringCloudRegistry(URL url,
                               ServiceRegistry<Registration> serviceRegistry,
                               RegistrationFactory registrationFactory,
                               DiscoveryClient discoveryClient,
                               ScheduledExecutorService servicesLookupScheduler,
                               ConfigurableApplicationContext applicationContext) {
        super(url);
        this.allServicesLookupInterval = url.getParameter(ALL_SERVICES_LOOKUP_INTERVAL_PARAM_NAME, 30L);
        this.registeredServicesLookupInterval = url.getParameter(REGISTERED_SERVICES_LOOKUP_INTERVAL_PARAM_NAME, 300L);
        this.serviceRegistry = serviceRegistry;
        this.registrationFactory = registrationFactory;
        this.discoveryClient = discoveryClient;
        this.dubboRegistryServiceIdHandler = applicationContext.getBean(DubboRegistryServiceIdHandler.class);
        this.applicationContext = applicationContext;
        this.servicesLookupScheduler = servicesLookupScheduler;
    }

    protected boolean shouldRegister(Registration registration) {
        Map<String, String> metadata = registration.getMetadata();
        String side = metadata.get(SIDE_KEY);
        return PROVIDER_SIDE.equals(side); // Only register the Provider.
    }

    @Override
    public void doRegister(URL url) {
        final Registration registration = createRegistration(url);
        if (shouldRegister(registration)) {
            serviceRegistry.register(registration);
        }
    }

    @Override
    public void doUnregister(URL url) {
        final Registration registration = createRegistration(url);
        if (shouldRegister(registration)) {
            this.serviceRegistry.deregister(registration);
        }
    }

    @Override
    public void doSubscribe(URL url, NotifyListener listener) {
        List<String> serviceNames = getServiceNames(url, listener);
        doSubscribe(url, listener, serviceNames);
        this.servicesLookupScheduler.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                doSubscribe(url, listener, serviceNames);
            }
        }, registeredServicesLookupInterval, registeredServicesLookupInterval, TimeUnit.SECONDS);
    }

    @Override
    public void doUnsubscribe(URL url, NotifyListener listener) {
        if (isAdminProtocol(url)) {
            shutdownServiceNamesLookup();
        }
    }

    @Override
    public boolean isAvailable() {
        return !discoveryClient.getServices().isEmpty();
    }

    private void shutdownServiceNamesLookup() {
        if (servicesLookupScheduler != null) {
            servicesLookupScheduler.shutdown();
        }
    }

    private Registration createRegistration(URL url) {
        return registrationFactory.create(url, applicationContext);
    }

    private void filterServiceNames(List<String> serviceNames) {
        filter(serviceNames, new Filter<String>() {
            @Override
            public boolean accept(String serviceName) {
                return dubboRegistryServiceIdHandler.supports(serviceName);
            }
        });
    }

    private List<String> getAllServiceNames() {
        return new LinkedList<>(discoveryClient.getServices());
    }

    /**
     * Get the service names from the specified {@link URL url}
     *
     * @param url      {@link URL}
     * @param listener {@link NotifyListener}
     * @return non-null
     */
    private List<String> getServiceNames(URL url, NotifyListener listener) {
        if (isAdminProtocol(url)) {
            initAllServicesLookupScheduler(url, listener);
            return getServiceNamesForOps(url);
        } else {
            return singletonList(dubboRegistryServiceIdHandler.createServiceId(url));
        }
    }


    private boolean isAdminProtocol(URL url) {
        return Constants.ADMIN_PROTOCOL.equals(url.getProtocol());
    }

    private void initAllServicesLookupScheduler(final URL url, final NotifyListener listener) {
        servicesLookupScheduler.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                List<String> serviceNames = getAllServiceNames();
                filterServiceNames(serviceNames);
                doSubscribe(url, listener, serviceNames);
            }
        }, allServicesLookupInterval, allServicesLookupInterval, TimeUnit.SECONDS);
    }

    private void doSubscribe(final URL url, final NotifyListener listener, final List<String> serviceNames) {
        for (String serviceName : serviceNames) {
            List<ServiceInstance> serviceInstances = discoveryClient.getInstances(serviceName);
            notifySubscriber(url, listener, serviceInstances);
        }
    }

    /**
     * Notify the Healthy {@link ServiceInstance service instance} to subscriber.
     *
     * @param url              {@link URL}
     * @param listener         {@link NotifyListener}
     * @param serviceInstances all {@link ServiceInstance instances}
     */
    private void notifySubscriber(URL url, NotifyListener listener, List<ServiceInstance> serviceInstances) {
        List<ServiceInstance> healthyInstances = new LinkedList<ServiceInstance>(serviceInstances);
        // Healthy Instances
        filterHealthyInstances(healthyInstances);
        List<URL> urls = buildURLs(url, healthyInstances);
        this.notify(url, listener, urls);
    }

    private void filterHealthyInstances(Collection<ServiceInstance> instances) {
        filter(instances, new Filter<ServiceInstance>() {
            @Override
            public boolean accept(ServiceInstance data) {
                // TODO check the details of status
//                return serviceRegistry.getStatus(new DubboRegistration(data)) != null;
                return true;
            }
        });
    }

    private List<URL> buildURLs(URL consumerURL, Collection<ServiceInstance> serviceInstances) {
        if (serviceInstances.isEmpty()) {
            return Collections.emptyList();
        }
        List<URL> urls = new LinkedList<URL>();
        for (ServiceInstance serviceInstance : serviceInstances) {
            URL url = buildURL(serviceInstance);
            if (UrlUtils.isMatch(consumerURL, url)) {
                urls.add(url);
            }
        }
        return urls;
    }

    private URL buildURL(ServiceInstance serviceInstance) {
        URL url = new URL(serviceInstance.getMetadata().get(Constants.PROTOCOL_KEY),
                serviceInstance.getHost(),
                serviceInstance.getPort(),
                serviceInstance.getMetadata());
        return url;
    }

    /**
     * Get the service names for Dubbo OPS
     *
     * @param url {@link URL}
     * @return non-null
     */
    private List<String> getServiceNamesForOps(URL url) {
        List<String> serviceNames = getAllServiceNames();
        filterServiceNames(serviceNames);
        return serviceNames;
    }

    private <T> void filter(Collection<T> collection, Filter<T> filter) {
        Iterator<T> iterator = collection.iterator();
        while (iterator.hasNext()) {
            T data = iterator.next();
            if (!filter.accept(data)) { // remove if not accept
                iterator.remove();
            }
        }
    }

    private static <T> T[] of(T... values) {
        return values;
    }

    /**
     * A filter
     */
    public interface Filter<T> {

        /**
         * Tests whether or not the specified data should be accepted.
         *
         * @param data The data to be tested
         * @return <code>true</code> if and only if <code>data</code>
         * should be accepted
         */
        boolean accept(T data);

    }

}
