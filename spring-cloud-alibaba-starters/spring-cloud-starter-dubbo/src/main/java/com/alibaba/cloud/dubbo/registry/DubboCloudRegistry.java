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

package com.alibaba.cloud.dubbo.registry;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.alibaba.cloud.dubbo.metadata.repository.DubboServiceMetadataRepository;
import com.alibaba.cloud.dubbo.registry.event.ServiceInstancesChangedEvent;
import com.alibaba.cloud.dubbo.service.DubboGenericServiceFactory;
import com.alibaba.cloud.dubbo.service.DubboMetadataService;
import com.alibaba.cloud.dubbo.service.DubboMetadataServiceProxy;
import com.alibaba.cloud.dubbo.util.DubboMetadataUtils;
import com.alibaba.cloud.dubbo.util.JSONUtils;
import org.apache.dubbo.common.URL;
import org.apache.dubbo.common.URLBuilder;
import org.apache.dubbo.registry.NotifyListener;
import org.apache.dubbo.registry.support.FailbackRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.util.CollectionUtils;

import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static java.util.stream.StreamSupport.stream;
import static org.apache.dubbo.common.URLBuilder.from;
import static org.apache.dubbo.common.constants.CommonConstants.GROUP_KEY;
import static org.apache.dubbo.common.constants.CommonConstants.PID_KEY;
import static org.apache.dubbo.common.constants.CommonConstants.PROTOCOL_KEY;
import static org.apache.dubbo.common.constants.CommonConstants.PROVIDER;
import static org.apache.dubbo.common.constants.CommonConstants.PROVIDER_SIDE;
import static org.apache.dubbo.common.constants.CommonConstants.SIDE_KEY;
import static org.apache.dubbo.common.constants.CommonConstants.TIMESTAMP_KEY;
import static org.apache.dubbo.common.constants.CommonConstants.VERSION_KEY;
import static org.apache.dubbo.common.constants.RegistryConstants.CATEGORY_KEY;
import static org.apache.dubbo.common.constants.RegistryConstants.EMPTY_PROTOCOL;
import static org.apache.dubbo.common.utils.CollectionUtils.isEmpty;
import static org.apache.dubbo.registry.Constants.ADMIN_PROTOCOL;
import static org.apache.dubbo.registry.client.metadata.ServiceInstanceMetadataUtils.METADATA_SERVICE_URLS_PROPERTY_NAME;
import static org.springframework.util.StringUtils.hasText;

/**
 * Dubbo Cloud {@link FailbackRegistry} is based on Spring Cloud {@link DiscoveryClient}.
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 */
public class DubboCloudRegistry extends FailbackRegistry {

	/**
	 * The parameter name of {@link #servicesLookupInterval}.
	 */
	public static final String SERVICES_LOOKUP_INTERVAL_PARAM_NAME = "dubbo.services.lookup.interval";

	protected static final String DUBBO_METADATA_SERVICE_CLASS_NAME = DubboMetadataService.class
			.getName();

	/**
	 * Caches the IDs of {@link ApplicationListener}.
	 */
	private static final Set<String> registerListeners = new HashSet<>();

	protected final Logger logger = LoggerFactory.getLogger(getClass());

	private final DiscoveryClient discoveryClient;

	private final DubboServiceMetadataRepository repository;

	private final DubboMetadataServiceProxy dubboMetadataConfigServiceProxy;

	private final JSONUtils jsonUtils;

	private final DubboGenericServiceFactory dubboGenericServiceFactory;

	private final DubboMetadataUtils dubboMetadataUtils;

	/**
	 * The interval in second of lookup service names(only for Dubbo-OPS).
	 */
	private final long servicesLookupInterval;

	private final ConfigurableApplicationContext applicationContext;

	private final String currentApplicationName;

	public DubboCloudRegistry(URL url, DiscoveryClient discoveryClient,
			DubboServiceMetadataRepository repository,
			DubboMetadataServiceProxy dubboMetadataConfigServiceProxy,
			JSONUtils jsonUtils, DubboGenericServiceFactory dubboGenericServiceFactory,
			ConfigurableApplicationContext applicationContext) {

		super(url);
		this.servicesLookupInterval = url
				.getParameter(SERVICES_LOOKUP_INTERVAL_PARAM_NAME, 60L);
		this.discoveryClient = discoveryClient;
		this.repository = repository;
		this.dubboMetadataConfigServiceProxy = dubboMetadataConfigServiceProxy;
		this.jsonUtils = jsonUtils;
		this.dubboGenericServiceFactory = dubboGenericServiceFactory;
		this.applicationContext = applicationContext;
		this.dubboMetadataUtils = getBean(DubboMetadataUtils.class);
		this.currentApplicationName = dubboMetadataUtils.getCurrentApplicationName();
	}

	private <T> T getBean(Class<T> beanClass) {
		return this.applicationContext.getBean(beanClass);
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
		repository.exportURL(url);
	}

	@Override
	public final void doUnregister(URL url) {
		if (!shouldRegister(url)) {
			return;
		}
		repository.unexportURL(url);
	}

	@Override
	public final void doSubscribe(URL url, NotifyListener listener) {

		if (isAdminURL(url)) {
			// TODO in future
			if (logger.isWarnEnabled()) {
				logger.warn("This feature about admin will be supported in the future.");
			}
		}
		else if (isDubboMetadataServiceURL(url)) { // for DubboMetadataService
			subscribeDubboMetadataServiceURLs(url, listener);
		}
		else { // for general Dubbo Services
			subscribeURLs(url, listener);
		}
	}

	private void subscribeURLs(URL url, NotifyListener listener) {

		// Sync subscription
		subscribeURLs(url, getServices(url), listener);

		// Async subscription
		registerServiceInstancesChangedListener(url, event -> {

			Set<String> serviceNames = getServices(url);

			String serviceName = event.getServiceName();

			if (serviceNames.contains(serviceName)) {
				subscribeURLs(url, serviceNames, listener);
			}
		});
	}

	private void subscribeURLs(URL url, Set<String> serviceNames,
			NotifyListener listener) {

		List<URL> subscribedURLs = new LinkedList<>();

		serviceNames.forEach(serviceName -> {

			subscribeURLs(url, subscribedURLs, serviceName,
					() -> getServiceInstances(serviceName));

		});

		// Notify all
		notifyAllSubscribedURLs(url, subscribedURLs, listener);
	}

	private void registerServiceInstancesChangedListener(URL url,
			ApplicationListener<ServiceInstancesChangedEvent> listener) {
		String listenerId = generateId(url);
		if (registerListeners.add(listenerId)) {
			applicationContext.addApplicationListener(listener);
		}
	}

	private void subscribeURLs(URL subscribedURL, List<URL> subscribedURLs,
			String serviceName,
			Supplier<List<ServiceInstance>> serviceInstancesSupplier) {
		List<ServiceInstance> serviceInstances = serviceInstancesSupplier.get();
		subscribeURLs(subscribedURL, subscribedURLs, serviceName, serviceInstances);
	}

	private void subscribeURLs(URL subscribedURL, List<URL> subscribedURLs,
			String serviceName, List<ServiceInstance> serviceInstances) {

		if (CollectionUtils.isEmpty(serviceInstances)) {
			if (logger.isWarnEnabled()) {
				logger.warn(format("There is no instance in service[name : %s]",
						serviceName));
			}
		}

		List<URL> exportedURLs = getExportedURLs(subscribedURL, serviceName,
				serviceInstances);

		/**
		 * Add the exported URLs from {@link MetadataService}
		 */
		subscribedURLs.addAll(exportedURLs);
	}

	private List<URL> getExportedURLs(URL subscribedURL, String serviceName,
			List<ServiceInstance> serviceInstances) {

		List<ServiceInstance> validServiceInstances = filter(serviceInstances);

		// If there is no valid ServiceInstance, return empty result
		if (isEmpty(validServiceInstances)) {
			if (logger.isWarnEnabled()) {
				logger.warn(
						"There is no instance from service[name : {}], and then Dubbo Service[key : {}] will not be "
								+ "available , please make sure the further impact",
						serviceName, subscribedURL.getServiceKey());
			}
			return emptyList();
		}

		List<URL> subscribedURLs = cloneExportedURLs(subscribedURL, serviceInstances);

		// clear local service instances, help GC
		validServiceInstances.clear();

		return subscribedURLs;
	}

	/**
	 * Clone the subscribed URLs based on the template URLs.
	 * @param subscribedURL the URL to be subscribed
	 * @param serviceInstances the list of {@link ServiceInstance service instances}
	 * @return non-null
	 */
	private List<URL> cloneExportedURLs(URL subscribedURL,
			List<ServiceInstance> serviceInstances) {

		List<URL> clonedExportedURLs = new LinkedList<>();

		serviceInstances.forEach(serviceInstance -> {

			String host = serviceInstance.getHost();

			getTemplateExportedURLs(subscribedURL, serviceInstances).stream()
					.map(templateURL -> templateURL.removeParameter(TIMESTAMP_KEY))
					.map(templateURL -> templateURL.removeParameter(PID_KEY))
					.map(templateURL -> {
						String protocol = templateURL.getProtocol();
						int port = repository.getDubboProtocolPort(serviceInstance,
								protocol);
						if (Objects.equals(templateURL.getHost(), host)
								&& Objects.equals(templateURL.getPort(), port)) { // use
							// templateURL
							// if
							// equals
							return templateURL;
						}

						URLBuilder clonedURLBuilder = from(templateURL) // remove the
								// parameters from
								// the template
								// URL
								.setHost(host) // reset the host
								.setPort(port); // reset the port

						return clonedURLBuilder.build();
					}).forEach(clonedExportedURLs::add);
		});
		return clonedExportedURLs;
	}

	private List<URL> getTemplateExportedURLs(URL subscribedURL,
			List<ServiceInstance> serviceInstances) {

		DubboMetadataService dubboMetadataService = getProxy(serviceInstances);

		List<URL> templateExportedURLs = emptyList();

		if (dubboMetadataService != null) {
			templateExportedURLs = getExportedURLs(dubboMetadataService, subscribedURL);
		}
		else {
			if (logger.isWarnEnabled()) {
				logger.warn(
						"The metadata of Dubbo service[key : {}] still can't be found, it could effect the further "
								+ "Dubbo service invocation",
						subscribedURL.getServiceKey());
			}

		}

		return templateExportedURLs;
	}

	private DubboMetadataService getProxy(List<ServiceInstance> serviceInstances) {
		return dubboMetadataConfigServiceProxy.getProxy(serviceInstances);
	}

	private List<ServiceInstance> filter(Collection<ServiceInstance> serviceInstances) {
		return serviceInstances.stream().filter(this::isDubboServiceInstance)
				.collect(Collectors.toList());
	}

	private boolean isDubboServiceInstance(ServiceInstance serviceInstance) {
		Map<String, String> metadata = serviceInstance.getMetadata();
		return metadata.containsKey(METADATA_SERVICE_URLS_PROPERTY_NAME);
	}

	private Set<String> getServices(URL url) {
		Set<String> subscribedServices = repository.getSubscribedServices();
		// TODO Add the filter feature
		return subscribedServices;
	}

	private void notifyAllSubscribedURLs(URL url, List<URL> subscribedURLs,
			NotifyListener listener) {

		if (isEmpty(subscribedURLs)) {
			// Add the EMPTY_PROTOCOL URL
			subscribedURLs.add(emptyURL(url));

			if (isDubboMetadataServiceURL(url)) {
				// if meta service change, and serviceInstances is zero, will clean up
				// information about this client
				String serviceName = url.getParameter(GROUP_KEY);
				repository.removeMetadataAndInitializedService(serviceName, url);
			}
		}

		if (logger.isDebugEnabled()) {
			logger.debug("The subscribed URL[{}] will notify all URLs : {}", url,
					subscribedURLs);
		}

		// Notify all
		listener.notify(subscribedURLs);
	}

	private List<ServiceInstance> getServiceInstances(Iterable<String> serviceNames) {
		return stream(serviceNames.spliterator(), false).map(this::getServiceInstances)
				.flatMap(Collection::stream).collect(Collectors.toList());
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

	private String generateId(URL url) {
		return url.getServiceKey();
	}

	private URL emptyURL(URL url) {
		// issue : When the last service provider is closed, the client still periodically
		// connects to the last provider.n
		// fix https://github.com/alibaba/spring-cloud-alibaba/issues/1259
		return from(url).setProtocol(EMPTY_PROTOCOL).removeParameter(CATEGORY_KEY)
				.build();
	}

	private List<URL> getExportedURLs(DubboMetadataService dubboMetadataService,
			URL subscribedURL) {
		String serviceInterface = subscribedURL.getServiceInterface();
		String group = subscribedURL.getParameter(GROUP_KEY);
		String version = subscribedURL.getParameter(VERSION_KEY);
		// The subscribed protocol may be null
		String subscribedProtocol = subscribedURL.getParameter(PROTOCOL_KEY);
		String exportedURLsJSON = dubboMetadataService.getExportedURLs(serviceInterface,
				group, version);
		return jsonUtils.toURLs(exportedURLsJSON).stream()
				.filter(exportedURL -> subscribedProtocol == null
						|| subscribedProtocol.equalsIgnoreCase(exportedURL.getProtocol()))
				.collect(Collectors.toList());
	}

	private void subscribeDubboMetadataServiceURLs(URL subscribedURL,
			NotifyListener listener) {

		// Sync subscription
		subscribeDubboMetadataServiceURLs(subscribedURL, listener,
				getServiceName(subscribedURL));

		// Sync subscription
		if (containsProviderCategory(subscribedURL)) {
			registerServiceInstancesChangedListener(subscribedURL, event -> {

				String sourceServiceName = event.getServiceName();
				String serviceName = getServiceName(subscribedURL);

				if (Objects.equals(sourceServiceName, serviceName)) {
					subscribeDubboMetadataServiceURLs(subscribedURL, listener,
							sourceServiceName);
				}
			});
		}
	}

	private String getServiceName(URL subscribedURL) {
		return subscribedURL.getParameter(GROUP_KEY);
	}

	private void subscribeDubboMetadataServiceURLs(URL subscribedURL,
			NotifyListener listener, String serviceName) {

		String serviceInterface = subscribedURL.getServiceInterface();
		String version = subscribedURL.getParameter(VERSION_KEY);
		String protocol = subscribedURL.getParameter(PROTOCOL_KEY);

		List<ServiceInstance> serviceInstances = getServiceInstances(serviceName);

		List<URL> urls = dubboMetadataUtils.getDubboMetadataServiceURLs(serviceInstances,
				serviceInterface, version, protocol);

		notifyAllSubscribedURLs(subscribedURL, urls, listener);
	}

	// private void subscribeDubboMetadataServiceURLs(URL subscribedURL,
	// NotifyListener listener, Set<String> serviceNames) {
	//
	// String serviceInterface = subscribedURL.getServiceInterface();
	// String version = subscribedURL.getParameter(VERSION_KEY);
	// String protocol = subscribedURL.getParameter(PROTOCOL_KEY);
	//
	// List<ServiceInstance> serviceInstances = getServiceInstances(serviceNames);
	//
	// List<URL> urls = dubboMetadataUtils.getDubboMetadataServiceURLs(serviceInstances,
	// serviceInterface, version, protocol);
	//
	// notifyAllSubscribedURLs(subscribedURL, urls, listener);
	// }

	private boolean containsProviderCategory(URL subscribedURL) {
		String category = subscribedURL.getParameter(CATEGORY_KEY);
		return category == null ? false : category.contains(PROVIDER);
	}

	@Override
	public final void doUnsubscribe(URL url, NotifyListener listener) {
		// TODO
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
