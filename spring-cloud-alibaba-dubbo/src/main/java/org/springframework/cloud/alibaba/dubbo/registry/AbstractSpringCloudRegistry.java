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
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static java.util.Collections.singleton;
import static org.apache.dubbo.common.Constants.PROVIDER_SIDE;
import static org.apache.dubbo.common.Constants.SIDE_KEY;
import static org.springframework.util.ObjectUtils.isEmpty;

/**
 * Abstract Dubbo {@link RegistryFactory} uses Spring Cloud Service Registration abstraction, whose protocol is "spring-cloud"
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 */
public abstract class AbstractSpringCloudRegistry extends FailbackRegistry {

    /**
     * The parameter name of {@link #servicesLookupInterval}
     */
    public static final String SERVICES_LOOKUP_INTERVAL_PARAM_NAME = "dubbo.services.lookup.interval";

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * The interval in second of lookup service names(only for Dubbo-OPS)
     */
    private final long servicesLookupInterval;

    private final DiscoveryClient discoveryClient;

    protected final ScheduledExecutorService servicesLookupScheduler;

    public AbstractSpringCloudRegistry(URL url,
                                       DiscoveryClient discoveryClient,
                                       ScheduledExecutorService servicesLookupScheduler) {
        super(url);
        this.servicesLookupInterval = url.getParameter(SERVICES_LOOKUP_INTERVAL_PARAM_NAME, 60L);
        this.discoveryClient = discoveryClient;
        this.servicesLookupScheduler = servicesLookupScheduler;
    }

    protected boolean shouldRegister(URL url) {
        String side = url.getParameter(SIDE_KEY);

        boolean should = PROVIDER_SIDE.equals(side); // Only register the Provider.

        if (!should) {
            if (logger.isDebugEnabled()) {
                logger.debug("The URL[{}] should not be registered.", url.toString());
            }
        }

        return should;
    }

    @Override
    public final void doRegister(URL url) {
        if (!shouldRegister(url)) {
            return;
        }
        doRegister0(url);
    }

    /**
     * The sub-type should implement to register
     *
     * @param url {@link URL}
     */
    protected abstract void doRegister0(URL url);

    @Override
    public final void doUnregister(URL url) {
        if (!shouldRegister(url)) {
            return;
        }
        doUnregister0(url);
    }

    /**
     * The sub-type should implement to unregister
     *
     * @param url {@link URL}
     */
    protected abstract void doUnregister0(URL url);

    @Override
    public final void doSubscribe(URL url, NotifyListener listener) {
        Set<String> serviceNames = getServiceNames(url);
        doSubscribe(url, listener, serviceNames);
    }

    @Override
    public final void doUnsubscribe(URL url, NotifyListener listener) {
        if (isAdminProtocol(url)) {
            shutdownServiceNamesLookup();
        }
    }

    @Override
    public boolean isAvailable() {
        return !discoveryClient.getServices().isEmpty();
    }

    protected void shutdownServiceNamesLookup() {
        if (servicesLookupScheduler != null) {
            servicesLookupScheduler.shutdown();
        }
    }

    private void filterServiceNames(Collection<String> serviceNames) {
        filter(serviceNames, new Filter<String>() {
            @Override
            public boolean accept(String serviceName) {
                return supports(serviceName);
            }
        });
    }

    protected abstract boolean supports(String serviceName);

    protected final Set<String> getAllServiceNames() {
        return new LinkedHashSet<>(discoveryClient.getServices());
    }

    /**
     * Get the service names from the specified {@link URL url}
     *
     * @param url {@link URL}
     * @return non-null
     */
    private Set<String> getServiceNames(URL url) {
        if (isAdminProtocol(url)) {
            return getServiceNamesForOps(url);
        } else {
            return singleton(getServiceName(url));
        }
    }

    protected boolean isAdminProtocol(URL url) {
        return Constants.ADMIN_PROTOCOL.equals(url.getProtocol());
    }

    /**
     * Get the service names for Dubbo OPS
     *
     * @param url {@link URL}
     * @return non-null
     */
    protected Set<String> getServiceNamesForOps(URL url) {
        Set<String> serviceNames = getAllServiceNames();
        filterServiceNames(serviceNames);
        return serviceNames;
    }

    protected abstract String getServiceName(URL url);

    private void doSubscribe(final URL url, final NotifyListener listener, final Collection<String> serviceNames) {

        subscribe(url, listener, serviceNames);

        schedule(() -> {
            subscribe(url, listener, serviceNames);
        });
    }

    protected ScheduledFuture<?> schedule(Runnable runnable) {
        return this.servicesLookupScheduler.scheduleAtFixedRate(runnable, servicesLookupInterval,
                servicesLookupInterval, TimeUnit.SECONDS);
    }

    protected List<ServiceInstance> getServiceInstances(String serviceName) {
        return discoveryClient.getInstances(serviceName);
    }

    private void subscribe(final URL url, final NotifyListener listener, final Collection<String> serviceNames) {
        for (String serviceName : serviceNames) {
            List<ServiceInstance> serviceInstances = getServiceInstances(serviceName);
            if (!isEmpty(serviceInstances)) {
                notifySubscriber(url, listener, serviceInstances);
            }
        }
    }

    /**
     * Notify the Healthy {@link ServiceInstance service instance} to subscriber.
     *
     * @param url              {@link URL}
     * @param listener         {@link NotifyListener}
     * @param serviceInstances all {@link ServiceInstance instances}
     */
    protected abstract void notifySubscriber(URL url, NotifyListener listener, List<ServiceInstance> serviceInstances);

    protected void filterHealthyInstances(Collection<ServiceInstance> instances) {
        filter(instances, new Filter<ServiceInstance>() {
            @Override
            public boolean accept(ServiceInstance data) {
                // TODO check the details of status
//                return serviceRegistry.getStatus(new DubboRegistration(data)) != null;
                return true;
            }
        });
    }

    protected List<URL> buildURLs(URL consumerURL, Collection<ServiceInstance> serviceInstances) {
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
