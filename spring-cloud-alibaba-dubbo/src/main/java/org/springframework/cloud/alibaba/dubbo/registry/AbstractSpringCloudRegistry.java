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
import org.apache.dubbo.registry.NotifyListener;
import org.apache.dubbo.registry.RegistryFactory;
import org.apache.dubbo.registry.support.FailbackRegistry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;
import static java.util.Collections.singleton;
import static org.apache.dubbo.common.Constants.PROVIDER_SIDE;
import static org.apache.dubbo.common.Constants.SIDE_KEY;
import static org.springframework.util.ObjectUtils.isEmpty;
import static org.springframework.util.StringUtils.hasText;

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

    private Set<String> filterServiceNames(Collection<String> serviceNames) {
        return new LinkedHashSet<>(filter(serviceNames, this::supports));
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
        return filterServiceNames(serviceNames);
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
        return hasText(serviceName) ? discoveryClient.getInstances(serviceName) : emptyList();
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

    protected <T> Collection<T> filter(Collection<T> collection, Predicate<T> filter) {
        return collection.stream()
                .filter(filter)
                .collect(Collectors.toList());
    }
}
