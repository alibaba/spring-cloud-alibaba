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

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.utils.NetUtils;
import com.alibaba.dubbo.common.utils.UrlUtils;
import com.alibaba.dubbo.registry.NotifyListener;
import com.alibaba.dubbo.registry.RegistryFactory;
import com.alibaba.dubbo.registry.support.FailbackRegistry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.DefaultServiceInstance;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.serviceregistry.Registration;
import org.springframework.cloud.client.serviceregistry.ServiceRegistry;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.alibaba.dubbo.common.Constants.CONFIGURATORS_CATEGORY;
import static com.alibaba.dubbo.common.Constants.CONSUMERS_CATEGORY;
import static com.alibaba.dubbo.common.Constants.PROVIDERS_CATEGORY;
import static com.alibaba.dubbo.common.Constants.ROUTERS_CATEGORY;

/**
 * Dubbo {@link RegistryFactory} uses Spring Cloud Service Registration abstraction, whose protocol is "spring-cloud"
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 */
public class SpringCloudRegistry extends FailbackRegistry {

    /**
     * All supported categories
     */
    private static final String[] ALL_SUPPORTED_CATEGORIES = of(
            PROVIDERS_CATEGORY,
            CONSUMERS_CATEGORY,
            ROUTERS_CATEGORY,
            CONFIGURATORS_CATEGORY
    );

    private static final int CATEGORY_INDEX = 0;

//    private static final int PROTOCOL_INDEX = CATEGORY_INDEX + 1;

//    private static final int SERVICE_INTERFACE_INDEX = PROTOCOL_INDEX + 1;

    private static final int SERVICE_INTERFACE_INDEX = CATEGORY_INDEX + 1;

    private static final int SERVICE_VERSION_INDEX = SERVICE_INTERFACE_INDEX + 1;

    private static final int SERVICE_GROUP_INDEX = SERVICE_VERSION_INDEX + 1;

    private static final String WILDCARD = "*";

    /**
     * The separator for service name
     */
    private static final String SERVICE_NAME_SEPARATOR = ":";

    private final ServiceRegistry<Registration> serviceRegistry;

    private final DiscoveryClient discoveryClient;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * {@link ScheduledExecutorService} lookup service names(only for Dubbo-OPS)
     */
    private volatile ScheduledExecutorService scheduledExecutorService;

    /**
     * The interval in second of lookup service names(only for Dubbo-OPS)
     */
    private static final long LOOKUP_INTERVAL = Long.getLong("dubbo.service.names.lookup.interval", 30);

    public SpringCloudRegistry(URL url, ServiceRegistry<Registration> serviceRegistry,
                               DiscoveryClient discoveryClient) {
        super(url);
        this.serviceRegistry = serviceRegistry;
        this.discoveryClient = discoveryClient;
    }

    @Override
    protected void doRegister(URL url) {
        final String serviceName = getServiceName(url);
        final Registration registration = createRegistration(serviceName, url);
        serviceRegistry.register(registration);
    }

    @Override
    protected void doUnregister(URL url) {
        final String serviceName = getServiceName(url);
        final Registration registration = createRegistration(serviceName, url);
        this.serviceRegistry.deregister(registration);
    }

    @Override
    protected void doSubscribe(URL url, NotifyListener listener) {
        List<String> serviceNames = getServiceNames(url, listener);
        doSubscribe(url, listener, serviceNames);
    }

    @Override
    protected void doUnsubscribe(URL url, NotifyListener listener) {
        if (isAdminProtocol(url)) {
            shutdownServiceNamesLookup();
        }
    }

    @Override
    public boolean isAvailable() {
        return false;
    }

    private void shutdownServiceNamesLookup() {
        if (scheduledExecutorService != null) {
            scheduledExecutorService.shutdown();
        }
    }

    private Registration createRegistration(String serviceName, URL url) {
        return new DubboRegistration(createServiceInstance(serviceName, url));
    }

    private ServiceInstance createServiceInstance(String serviceName, URL url) {
        // Append default category if absent
        String category = url.getParameter(Constants.CATEGORY_KEY, Constants.DEFAULT_CATEGORY);
        URL newURL = url.addParameter(Constants.CATEGORY_KEY, category);
        newURL = newURL.addParameter(Constants.PROTOCOL_KEY, url.getProtocol());
        String ip = NetUtils.getLocalHost();
        int port = newURL.getParameter(Constants.BIND_PORT_KEY, url.getPort());
        DefaultServiceInstance serviceInstance = new DefaultServiceInstance(serviceName, ip, port, false);
        serviceInstance.getMetadata().putAll(new LinkedHashMap<>(newURL.getParameters()));
        return serviceInstance;
    }

    public static String getServiceName(URL url) {
        String category = url.getParameter(Constants.CATEGORY_KEY, Constants.DEFAULT_CATEGORY);
        return getServiceName(url, category);
    }

    private static String getServiceName(URL url, String category) {
        StringBuilder serviceNameBuilder = new StringBuilder(category);
        appendIfPresent(serviceNameBuilder, url, Constants.INTERFACE_KEY);
        appendIfPresent(serviceNameBuilder, url, Constants.VERSION_KEY);
        appendIfPresent(serviceNameBuilder, url, Constants.GROUP_KEY);
        return serviceNameBuilder.toString();
    }

    private static void appendIfPresent(StringBuilder target, URL url, String parameterName) {
        String parameterValue = url.getParameter(parameterName);
        appendIfPresent(target, parameterValue);
    }

    private static void appendIfPresent(StringBuilder target, String parameterValue) {
        if (StringUtils.hasText(parameterValue)) {
            target.append(SERVICE_NAME_SEPARATOR).append(parameterValue);
        }
    }

    private void filterServiceNames(List<String> serviceNames, URL url) {

        final String[] categories = getCategories(url);

        final String targetServiceInterface = url.getServiceInterface();

        final String targetVersion = url.getParameter(Constants.VERSION_KEY);

        final String targetGroup = url.getParameter(Constants.GROUP_KEY);

        filter(serviceNames, new Filter<String>() {
            @Override
            public boolean accept(String serviceName) {
                // split service name to segments
                // (required) segments[0] = category
                // (required) segments[1] = serviceInterface
                // (required) segments[2] = version
                // (optional) segments[3] = group
                String[] segments = getServiceSegments(serviceName);
                int length = segments.length;
                if (length < SERVICE_GROUP_INDEX) { // must present 4 segments or more
                    return false;
                }

                String category = getCategory(segments);
                if (Arrays.binarySearch(categories, category) > -1) { // no match category
                    return false;
                }

                String serviceInterface = getServiceInterface(segments);
                if (!WILDCARD.equals(targetServiceInterface) &&
                        !Objects.equals(targetServiceInterface, serviceInterface)) { // no match service interface
                    return false;
                }

                String version = getServiceVersion(segments);
                if (!WILDCARD.equals(targetVersion) &&
                        !Objects.equals(targetVersion, version)) { // no match service version
                    return false;
                }

                String group = getServiceGroup(segments);
                if (group != null && !WILDCARD.equals(targetGroup)
                        && !Objects.equals(targetGroup, group)) {  // no match service group
                    return false;
                }

                return true;
            }
        });
    }

    public static String[] getServiceSegments(String serviceName) {
        return StringUtils.delimitedListToStringArray(serviceName, SERVICE_NAME_SEPARATOR);
    }

    public static String getCategory(String[] segments) {
        return segments[CATEGORY_INDEX];
    }

//    public static String getProtocol(String[] segments) {
//        return segments[PROTOCOL_INDEX];
//    }

    public static String getServiceInterface(String[] segments) {
        return segments[SERVICE_INTERFACE_INDEX];
    }

    public static String getServiceVersion(String[] segments) {
        return segments[SERVICE_VERSION_INDEX];
    }

    public static String getServiceGroup(String[] segments) {
        return segments.length > SERVICE_GROUP_INDEX ? segments[SERVICE_GROUP_INDEX] : null;
    }

    /**
     * Get the categories from {@link URL}
     *
     * @param url {@link URL}
     * @return non-null array
     */
    private String[] getCategories(URL url) {
        return Constants.ANY_VALUE.equals(url.getServiceInterface()) ?
                ALL_SUPPORTED_CATEGORIES : of(Constants.DEFAULT_CATEGORY);
    }

    private List<String> getAllServiceNames() {
        return discoveryClient.getServices();
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
            scheduleServiceNamesLookup(url, listener);
            return getServiceNamesForOps(url);
        } else {
            return doGetServiceNames(url);
        }
    }


    private boolean isAdminProtocol(URL url) {
        return Constants.ADMIN_PROTOCOL.equals(url.getProtocol());
    }

    private void scheduleServiceNamesLookup(final URL url, final NotifyListener listener) {
        if (scheduledExecutorService == null) {
            scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
            scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    List<String> serviceNames = getAllServiceNames();
                    filter(serviceNames, new Filter<String>() {
                        @Override
                        public boolean accept(String serviceName) {
                            boolean accepted = false;
                            for (String category : ALL_SUPPORTED_CATEGORIES) {
                                String prefix = category + SERVICE_NAME_SEPARATOR;
                                if (StringUtils.startsWithIgnoreCase(serviceName, prefix)) {
                                    accepted = true;
                                    break;
                                }
                            }
                            return accepted;
                        }
                    });
                    doSubscribe(url, listener, serviceNames);
                }
            }, LOOKUP_INTERVAL, LOOKUP_INTERVAL, TimeUnit.SECONDS);
        }
    }

    private void doSubscribe(final URL url, final NotifyListener listener, final List<String> serviceNames) {
        for (String serviceName : serviceNames) {
            List<ServiceInstance> serviceInstances = discoveryClient.getInstances(serviceName);
            notifySubscriber(url, listener, serviceInstances);
            // TODO Support Update notification event
        }
    }

    private List<String> doGetServiceNames(URL url) {
        String[] categories = getCategories(url);
        List<String> serviceNames = new ArrayList<String>(categories.length);
        for (String category : categories) {
            final String serviceName = getServiceName(url, category);
            serviceNames.add(serviceName);
        }
        return serviceNames;
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
        filterServiceNames(serviceNames, url);
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
    private interface Filter<T> {

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
