/*
 * Copyright 2013-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.cloud.dubbo.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import com.alibaba.cloud.dubbo.metadata.repository.ServiceInstanceSelector;
import com.alibaba.cloud.dubbo.util.DubboMetadataUtils;
import org.apache.dubbo.common.URL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;

import static java.lang.reflect.Proxy.newProxyInstance;
import static org.apache.dubbo.common.constants.CommonConstants.APPLICATION_KEY;
import static org.apache.dubbo.common.constants.CommonConstants.VERSION_KEY;

/**
 * The proxy of {@link DubboMetadataService}.
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 */
public class DubboMetadataServiceProxy implements BeanClassLoaderAware, DisposableBean {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	private final DubboGenericServiceFactory dubboGenericServiceFactory;

	private final DubboMetadataUtils dubboMetadataUtils;

	private final ServiceInstanceSelector serviceInstanceSelector;

	private final DiscoveryClient discoveryClient;

	private final Map<String, DubboMetadataService> dubboMetadataServiceCache = new ConcurrentHashMap<>();

	private ClassLoader classLoader;

	public DubboMetadataServiceProxy(
			DubboGenericServiceFactory dubboGenericServiceFactory,
			DubboMetadataUtils dubboMetadataUtils,
			ServiceInstanceSelector serviceInstanceSelector,
			DiscoveryClient discoveryClient) {
		this.dubboGenericServiceFactory = dubboGenericServiceFactory;
		this.dubboMetadataUtils = dubboMetadataUtils;
		this.serviceInstanceSelector = serviceInstanceSelector;
		this.discoveryClient = discoveryClient;
	}

	/**
	 * Remove {@link DubboMetadataService}'s Proxy by service name.
	 * @param serviceName the service name
	 */
	public void removeProxy(String serviceName) {
		dubboMetadataServiceCache.remove(serviceName);
		dubboGenericServiceFactory.destroy(serviceName);
	}

	/**
	 * Get the proxy of {@link DubboMetadataService} if possible.
	 * @param serviceInstances the instances of {@link DubboMetadataService}
	 * @return <code>null</code> if initialization can't be done
	 */
	public DubboMetadataService getProxy(List<ServiceInstance> serviceInstances) {

		DubboMetadataService dubboMetadataService = null;

		// attempt to get the proxy of DubboMetadataService in maximum times
		int attempts = serviceInstances.size();

		for (int i = 0; i < attempts; i++) {
			Optional<ServiceInstance> serviceInstance = select(serviceInstances);

			if (serviceInstance.isPresent()) {

				List<URL> dubboMetadataServiceURLs = getDubboMetadataServiceURLs(
						serviceInstance.get());

				for (URL dubboMetadataServiceURL : dubboMetadataServiceURLs) {
					dubboMetadataService = createProxyIfAbsent(dubboMetadataServiceURL);
					if (dubboMetadataService != null) {
						return dubboMetadataService;
					}
				}
			}
		}

		return dubboMetadataService;
	}

	/**
	 * Is the {@link DubboMetadataService}'s Proxy initialized or not.
	 * @param serviceName the service name
	 * @return <code>true</code> if initialized , or return <code>false</code>
	 */
	public boolean isInitialized(String serviceName) {
		return dubboMetadataServiceCache.containsKey(serviceName);
	}

	/**
	 * Create a {@link DubboMetadataService}'s Proxy If abstract.
	 * @param dubboMetadataServiceURL the {@link URL} of {@link DubboMetadataService}
	 * @return a {@link DubboMetadataService} proxy
	 */
	private DubboMetadataService createProxyIfAbsent(URL dubboMetadataServiceURL) {
		String serviceName = dubboMetadataServiceURL.getParameter(APPLICATION_KEY);
		String version = dubboMetadataServiceURL.getParameter(VERSION_KEY);
		// Initialize DubboMetadataService with right version
		return createProxyIfAbsent(serviceName, version);
	}

	/**
	 * Initializes {@link DubboMetadataService}'s Proxy.
	 * @param serviceName the service name
	 * @param version the service version
	 * @return a {@link DubboMetadataService} proxy
	 */
	private DubboMetadataService createProxyIfAbsent(String serviceName, String version) {
		return dubboMetadataServiceCache.computeIfAbsent(serviceName,
				name -> createProxy(name, version));
	}

	private Optional<ServiceInstance> select(List<ServiceInstance> serviceInstances) {
		return serviceInstanceSelector.select(serviceInstances);
	}

	private List<URL> getDubboMetadataServiceURLs(ServiceInstance serviceInstance) {
		return dubboMetadataUtils.getDubboMetadataServiceURLs(serviceInstance);
	}

	/**
	 * Get a proxy instance of {@link DubboMetadataService} via the specified service
	 * name.
	 * @param serviceName the service name
	 * @return a {@link DubboMetadataService} proxy
	 */
	public DubboMetadataService getProxy(String serviceName) {
		return dubboMetadataServiceCache.getOrDefault(serviceName,
				getProxy0(serviceName));
	}

	private DubboMetadataService getProxy0(String serviceName) {
		return getProxy(discoveryClient.getInstances(serviceName));
	}

	@Override
	public void setBeanClassLoader(ClassLoader classLoader) {
		this.classLoader = classLoader;
	}

	@Override
	public void destroy() throws Exception {
		dubboMetadataServiceCache.clear();
	}

	/**
	 * New a proxy instance of {@link DubboMetadataService} via the specified service
	 * name.
	 * @param serviceName the service name
	 * @param version the service version
	 * @return a {@link DubboMetadataService} proxy
	 */
	protected DubboMetadataService createProxy(String serviceName, String version) {

		if (logger.isInfoEnabled()) {
			logger.info(
					"The metadata of Dubbo service[name : {}] is about to be initialized",
					serviceName);
		}

		return (DubboMetadataService) newProxyInstance(classLoader,
				new Class[] { DubboMetadataService.class },
				new DubboMetadataServiceInvocationHandler(serviceName, version,
						dubboGenericServiceFactory));
	}

}
