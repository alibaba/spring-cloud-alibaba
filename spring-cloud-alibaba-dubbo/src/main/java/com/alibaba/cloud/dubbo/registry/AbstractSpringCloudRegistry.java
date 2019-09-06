/*
 * Copyright (C) 2018 the original author or authors.
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
package com.alibaba.cloud.dubbo.registry;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.apache.dubbo.common.URLBuilder.from;
import static org.apache.dubbo.common.constants.CommonConstants.GROUP_KEY;
import static org.apache.dubbo.common.constants.CommonConstants.PROTOCOL_KEY;
import static org.apache.dubbo.common.constants.CommonConstants.PROVIDER_SIDE;
import static org.apache.dubbo.common.constants.CommonConstants.SIDE_KEY;
import static org.apache.dubbo.common.constants.CommonConstants.VERSION_KEY;
import static org.apache.dubbo.common.constants.RegistryConstants.EMPTY_PROTOCOL;
import static org.apache.dubbo.registry.Constants.ADMIN_PROTOCOL;
import static org.springframework.util.StringUtils.hasText;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.dubbo.common.URL;
import org.apache.dubbo.registry.NotifyListener;
import org.apache.dubbo.registry.RegistryFactory;
import org.apache.dubbo.registry.support.FailbackRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.util.CollectionUtils;

import com.alibaba.cloud.dubbo.metadata.repository.DubboServiceMetadataRepository;
import com.alibaba.cloud.dubbo.registry.event.ServiceInstancesChangedEvent;
import com.alibaba.cloud.dubbo.service.DubboMetadataService;
import com.alibaba.cloud.dubbo.service.DubboMetadataServiceProxy;
import com.alibaba.cloud.dubbo.util.JSONUtils;

/**
 * Abstract Dubbo {@link RegistryFactory} uses Spring Cloud Service Registration
 * abstraction, whose protocol is "spring-cloud"
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 */
public abstract class AbstractSpringCloudRegistry extends FailbackRegistry {

	/**
	 * The parameter name of {@link #servicesLookupInterval}
	 */
	public static final String SERVICES_LOOKUP_INTERVAL_PARAM_NAME = "dubbo.services.lookup.interval";

	protected static final String DUBBO_METADATA_SERVICE_CLASS_NAME = DubboMetadataService.class
			.getName();
	/**
	 * Caches the IDs of {@link ApplicationListener}
	 */
	private static final Set<String> registerListeners = new HashSet<>();

	protected final Logger logger = LoggerFactory.getLogger(getClass());

	/**
	 * The interval in second of lookup service names(only for Dubbo-OPS)
	 */
	private final long servicesLookupInterval;

	private final DiscoveryClient discoveryClient;

	private final DubboServiceMetadataRepository repository;

	private final DubboMetadataServiceProxy dubboMetadataConfigServiceProxy;

	private final JSONUtils jsonUtils;

	private final ConfigurableApplicationContext applicationContext;

	public AbstractSpringCloudRegistry(URL url, DiscoveryClient discoveryClient,
			DubboServiceMetadataRepository dubboServiceMetadataRepository,
			DubboMetadataServiceProxy dubboMetadataConfigServiceProxy,
			JSONUtils jsonUtils, ConfigurableApplicationContext applicationContext) {
		super(url);
		this.servicesLookupInterval = url
				.getParameter(SERVICES_LOOKUP_INTERVAL_PARAM_NAME, 60L);
		this.discoveryClient = discoveryClient;
		this.repository = dubboServiceMetadataRepository;
		this.dubboMetadataConfigServiceProxy = dubboMetadataConfigServiceProxy;
		this.jsonUtils = jsonUtils;
		this.applicationContext = applicationContext;
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

		if (isAdminURL(url)) {
			// TODO in future
		}
		else if (isDubboMetadataServiceURL(url)) { // for DubboMetadataService
			subscribeDubboMetadataServiceURLs(url, listener);
		}
		else { // for general Dubbo Services
			subscribeDubboServiceURLs(url, listener);
		}
	}

	protected void subscribeDubboServiceURLs(URL url, NotifyListener listener) {

		doSubscribeDubboServiceURLs(url, listener);

		registerServiceInstancesChangedEventListener(url, listener);
	}

	/**
	 * Register a {@link ApplicationListener listener} for
	 * {@link ServiceInstancesChangedEvent}
	 *
	 * @param url {@link URL}
	 * @param listener {@link NotifyListener}
	 */
	private void registerServiceInstancesChangedEventListener(URL url,
			NotifyListener listener) {
		String listenerId = generateId(url);
		if (registerListeners.add(listenerId)) {
			applicationContext.addApplicationListener(
					new ApplicationListener<ServiceInstancesChangedEvent>() {
						@Override
						public void onApplicationEvent(
								ServiceInstancesChangedEvent event) {
							String serviceName = event.getServiceName();
							Collection<ServiceInstance> serviceInstances = event
									.getServiceInstances();
							subscribeDubboServiceURL(url, listener, serviceName,
									s -> serviceInstances);
						}
					});
		}
	}

	private void doSubscribeDubboServiceURLs(URL url, NotifyListener listener) {

		Set<String> subscribedServices = repository.getSubscribedServices();
		// Sync
		subscribedServices.forEach(service -> subscribeDubboServiceURL(url, listener,
				service, this::getServiceInstances));
	}

	protected void subscribeDubboServiceURL(URL url, NotifyListener listener,
			String serviceName,
			Function<String, Collection<ServiceInstance>> serviceInstancesFunction) {

		if (logger.isInfoEnabled()) {
			logger.info(
					"The Dubbo Service URL[ID : {}] is being subscribed for service[name : {}]",
					generateId(url), serviceName);
		}

		List<URL> allSubscribedURLs = new LinkedList<>();

		Collection<ServiceInstance> serviceInstances = serviceInstancesFunction
				.apply(serviceName);

		if (CollectionUtils.isEmpty(serviceInstances)) {
			dubboMetadataConfigServiceProxy.removeProxy(serviceName);
			repository.removeInitializedService(serviceName);
			repository.removeServiceMetadata(serviceName);
			if (logger.isWarnEnabled()) {
				logger.warn(
						"There is no instance from service[name : {}], and then Dubbo Service[key : {}] will not be "
								+ "available , please make sure the further impact",
						serviceName, url.getServiceKey());
			}
			/**
			 * URLs with {@link RegistryConstants#EMPTY_PROTOCOL}
			 */
			allSubscribedURLs.addAll(emptyURLs(url));
			if (logger.isDebugEnabled()) {
				logger.debug("The subscribed URL[{}] will notify all URLs : {}", url,
						allSubscribedURLs);
			}
			listener.notify(allSubscribedURLs);
			return;
		}

		DubboMetadataService dubboMetadataService = dubboMetadataConfigServiceProxy
				.getProxy(serviceName);

		if (dubboMetadataService == null) { // If not found, try to initialize
			if (logger.isInfoEnabled()) {
				logger.info(
						"The metadata of Dubbo service[key : {}] can't be found when the subscribed service[name : {}], "
								+ "and then try to initialize it",
						url.getServiceKey(), serviceName);
			}
			repository.initializeMetadata(serviceName);
			dubboMetadataService = dubboMetadataConfigServiceProxy.getProxy(serviceName);
		}

		if (dubboMetadataService == null) { // It makes sure not-found, return immediately
			if (logger.isWarnEnabled()) {
				logger.warn(
						"The metadata of Dubbo service[key : {}] still can't be found, it could effect the further "
								+ "Dubbo service invocation",
						url.getServiceKey());
			}
			return;
		}

		List<URL> exportedURLs = getExportedURLs(dubboMetadataService, url);

		for (URL exportedURL : exportedURLs) {
			String protocol = exportedURL.getProtocol();
			List<URL> subscribedURLs = new LinkedList<>();
			serviceInstances.forEach(serviceInstance -> {
				Integer port = repository.getDubboProtocolPort(serviceInstance, protocol);
				String host = serviceInstance.getHost();
				if (port == null) {
					if (logger.isWarnEnabled()) {
						logger.warn(
								"The protocol[{}] port of Dubbo  service instance[host : {}] "
										+ "can't be resolved",
								protocol, host);
					}
				}
				else {
					URL subscribedURL = new URL(protocol, host, port,
							exportedURL.getParameters());
					subscribedURLs.add(subscribedURL);
				}
			});

			allSubscribedURLs.addAll(subscribedURLs);
		}

		if (logger.isDebugEnabled()) {
			logger.debug("The subscribed URL[{}] will notify all URLs : {}", url,
					allSubscribedURLs);
		}

		listener.notify(allSubscribedURLs);
	}

	private String generateId(URL url) {
		return url.toString(VERSION_KEY, GROUP_KEY, PROTOCOL_KEY);
	}

	private List<URL> emptyURLs(URL url) {
		return asList(from(url).setProtocol(EMPTY_PROTOCOL).build());
	}

	private List<ServiceInstance> getServiceInstances(String serviceName) {
		return hasText(serviceName) ? doGetServiceInstances(serviceName) : emptyList();
	}

	private List<ServiceInstance> doGetServiceInstances(String serviceName) {
		List<ServiceInstance> serviceInstances = emptyList();
		try {
			serviceInstances = discoveryClient.getInstances(serviceName);
		}
		catch (Exception e) {
			if (logger.isErrorEnabled()) {
				logger.error(e.getMessage(), e);
			}
		}
		return serviceInstances;
	}

	private List<URL> getExportedURLs(DubboMetadataService dubboMetadataService,
			URL url) {
		String serviceInterface = url.getServiceInterface();
		String group = url.getParameter(GROUP_KEY);
		String version = url.getParameter(VERSION_KEY);
		// The subscribed protocol may be null
		String subscribedProtocol = url.getParameter(PROTOCOL_KEY);
		String exportedURLsJSON = dubboMetadataService.getExportedURLs(serviceInterface,
				group, version);
		return jsonUtils.toURLs(exportedURLsJSON).stream()
				.filter(exportedURL -> subscribedProtocol == null
						|| subscribedProtocol.equalsIgnoreCase(exportedURL.getProtocol()))
				.collect(Collectors.toList());
	}

	private void subscribeDubboMetadataServiceURLs(URL url, NotifyListener listener) {
		String serviceInterface = url.getServiceInterface();
		String group = url.getParameter(GROUP_KEY);
		String version = url.getParameter(VERSION_KEY);
		String protocol = url.getParameter(PROTOCOL_KEY);
		List<URL> urls = repository.findSubscribedDubboMetadataServiceURLs(
				serviceInterface, group, version, protocol);
		listener.notify(urls);
	}

	@Override
	public final void doUnsubscribe(URL url, NotifyListener listener) {
		if (isAdminURL(url)) {
		}
	}

	@Override
	public boolean isAvailable() {
		return !discoveryClient.getServices().isEmpty();
	}

	protected boolean isAdminURL(URL url) {
		return ADMIN_PROTOCOL.equals(url.getProtocol());
	}

	protected boolean isDubboMetadataServiceURL(URL url) {
		return DUBBO_METADATA_SERVICE_CLASS_NAME.equals(url.getServiceInterface());
	}
}
