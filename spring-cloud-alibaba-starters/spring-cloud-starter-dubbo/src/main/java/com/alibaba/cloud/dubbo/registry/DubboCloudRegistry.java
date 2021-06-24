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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import com.alibaba.cloud.commons.lang.StringUtils;
import com.alibaba.cloud.dubbo.metadata.RevisionResolver;
import com.alibaba.cloud.dubbo.metadata.repository.DubboServiceMetadataRepository;
import com.alibaba.cloud.dubbo.registry.event.ServiceInstancesChangedEvent;
import com.alibaba.cloud.dubbo.service.DubboMetadataService;
import com.alibaba.cloud.dubbo.service.DubboMetadataServiceProxy;
import com.alibaba.cloud.dubbo.util.DubboMetadataUtils;
import com.alibaba.cloud.dubbo.util.JSONUtils;
import org.apache.dubbo.common.URL;
import org.apache.dubbo.registry.NotifyListener;
import org.apache.dubbo.registry.support.FailbackRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;

import static java.util.Collections.emptyList;
import static org.apache.dubbo.common.constants.CommonConstants.CONSUMER;
import static org.apache.dubbo.common.constants.CommonConstants.GROUP_KEY;
import static org.apache.dubbo.common.constants.CommonConstants.PROVIDER;
import static org.apache.dubbo.common.constants.CommonConstants.PROVIDER_SIDE;
import static org.apache.dubbo.common.constants.CommonConstants.SIDE_KEY;
import static org.apache.dubbo.common.constants.RegistryConstants.CATEGORY_KEY;
import static org.apache.dubbo.registry.Constants.ADMIN_PROTOCOL;
import static org.apache.dubbo.registry.client.metadata.ServiceInstanceMetadataUtils.METADATA_SERVICE_URLS_PROPERTY_NAME;
import static org.springframework.util.StringUtils.hasText;

/**
 * Dubbo Cloud {@link FailbackRegistry} is based on Spring Cloud {@link DiscoveryClient}.
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @author <a href="mailto:chenxilzx1@gmail.com">theonefx</a>
 */
public class DubboCloudRegistry extends FailbackRegistry
		implements ApplicationListener<ServiceInstancesChangedEvent> {

	protected static final String DUBBO_METADATA_SERVICE_CLASS_NAME = DubboMetadataService.class
			.getName();

	protected final Logger logger = LoggerFactory.getLogger(getClass());

	private final DiscoveryClient discoveryClient;

	private final DubboServiceMetadataRepository repository;

	private final DubboMetadataServiceProxy dubboMetadataConfigServiceProxy;

	private final JSONUtils jsonUtils;

	private final DubboMetadataUtils dubboMetadataUtils;

	private final ConfigurableApplicationContext applicationContext;

	private final ReSubscribeManager reSubscribeManager;

	private final AtomicBoolean inited = new AtomicBoolean(false);

	/**
	 * {subscribedURL : ServiceSubscribeHandler}.
	 */
	private final Map<URL, GenearalServiceSubscribeHandler> urlSubscribeHandlerMap = new ConcurrentHashMap<>();

	/**
	 * {appName: MetadataServiceSubscribeHandler}.
	 */
	private final Map<String, MetadataServiceSubscribeHandler> metadataSubscribeHandlerMap = new ConcurrentHashMap<>();

	/**
	 * {appName : {revision: [instances]}}.
	 */
	private final Map<String, Map<String, List<ServiceInstance>>> serviceRevisionInstanceMap = new ConcurrentHashMap<>();

	public DubboCloudRegistry(URL url, DiscoveryClient discoveryClient,
			DubboServiceMetadataRepository repository,
			DubboMetadataServiceProxy dubboMetadataConfigServiceProxy,
			JSONUtils jsonUtils, ConfigurableApplicationContext applicationContext) {

		super(url);
		this.discoveryClient = discoveryClient;
		this.repository = repository;
		this.dubboMetadataConfigServiceProxy = dubboMetadataConfigServiceProxy;
		this.jsonUtils = jsonUtils;
		this.applicationContext = applicationContext;
		this.dubboMetadataUtils = getBean(DubboMetadataUtils.class);
		this.reSubscribeManager = new ReSubscribeManager(this);
	}

	private void preInit() {
		if (inited.compareAndSet(false, true)) {
			Set<String> subscribeApps = getServices(null);

			for (String appName : subscribeApps) {
				List<ServiceInstance> instances = discoveryClient.getInstances(appName);

				Map<String, List<ServiceInstance>> map = serviceRevisionInstanceMap
						.computeIfAbsent(appName, k -> new HashMap<>());

				for (ServiceInstance instance : instances) {
					String revision = RevisionResolver.getRevision(instance);
					List<ServiceInstance> list = map.computeIfAbsent(revision,
							k -> new ArrayList<>());
					list.add(instance);
				}

				if (map.size() == 0) {
					logger.debug("APP {} preInited, instance siez is zero!!", appName);
				}
				else {
					map.forEach((revision, list) -> logger.debug(
							"APP {} revision {} preInited, instance size = {}", appName,
							revision, list.size()));
				}
			}

			metadataSubscribeHandlerMap.forEach((url, handler) -> handler.init());
			urlSubscribeHandlerMap.forEach((url, handler) -> handler.init());
			repository.initializeMetadata();

			// meke sure everything prepared, then can listening
			// ServiceInstanceChangeEvent
			applicationContext.addApplicationListener(this);

			logger.info("DubboCloudRegistry preInit Done.");
		}
	}

	protected <T> T getBean(Class<T> beanClass) {
		return this.applicationContext.getBean(beanClass);
	}

	protected boolean shouldNotRegister(URL url) {
		String side = url.getParameter(SIDE_KEY);

		boolean should = PROVIDER_SIDE.equals(side); // Only register the Provider.

		if (logger.isDebugEnabled()) {
			if (!should) {
				logger.debug("The URL should NOT!! be registered & unregistered [{}] .",
						url);
			}
			else {
				logger.debug("The URL should be registered & unregistered [{}] .", url);
			}
		}

		return !should;
	}

	@Override
	public final void doRegister(URL url) {
		synchronized (this) {
			preInit();
			if (shouldNotRegister(url)) {
				return;
			}
			repository.exportURL(url);
		}
	}

	@Override
	public final void doUnregister(URL url) {
		synchronized (this) {
			preInit();
			if (shouldNotRegister(url)) {
				return;
			}
			repository.unexportURL(url);
		}
	}

	@Override
	public final void doSubscribe(URL url, NotifyListener listener) {
		synchronized (this) {
			preInit();
			if (isAdminURL(url)) {
				// TODO in future
				if (logger.isWarnEnabled()) {
					logger.warn(
							"This feature about admin will be supported in the future.");
				}
			}
			else if (isDubboMetadataServiceURL(url) && containsProviderCategory(url)) {
				// for DubboMetadataService
				String appName = getServiceName(url);
				MetadataServiceSubscribeHandler handler = new MetadataServiceSubscribeHandler(
						appName, url, listener, this, dubboMetadataUtils);
				if (inited.get()) {
					handler.init();
				}
				metadataSubscribeHandlerMap.put(appName, handler);
			}
			else if (isConsumerServiceURL(url)) {
				// for general Dubbo Services
				GenearalServiceSubscribeHandler handler = new GenearalServiceSubscribeHandler(
						url, listener, this, repository, jsonUtils,
						dubboMetadataConfigServiceProxy);
				if (inited.get()) {
					handler.init();
				}
				urlSubscribeHandlerMap.put(url, handler);
			}
		}
	}

	/**
	 * Process ServiceInstanceChangedEvent, refresh dubbo reference and metadata info.
	 */
	@Override
	public void onApplicationEvent(ServiceInstancesChangedEvent event) {

		String appName = event.getServiceName();

		List<ServiceInstance> instances = filter(event.getServiceInstances() != null
				? event.getServiceInstances() : Collections.emptyList());

		Set<String> subscribedServiceNames = getServices(null);

		if (!subscribedServiceNames.contains(appName)) {
			return;
		}

		if (instances.size() == 0) {
			logger.warn("APP {} instance changed, size changed zero!!!", appName);
		}
		else {
			logger.info("APP {} instance changed, size changed to {}", appName,
					instances.size());
		}
		// group by revision
		Map<String, List<ServiceInstance>> newGroup = instances.stream()
				.collect(Collectors.groupingBy(RevisionResolver::getRevision));

		synchronized (this) {

			Map<String, List<ServiceInstance>> oldGroup = serviceRevisionInstanceMap
					.computeIfAbsent(appName, k -> new HashMap<>());

			if (serviceInstanceNotChanged(oldGroup, newGroup)) {
				logger.debug("APP {} instance changed, but nothing different", appName);
				return;
			}

			try {

				// ensure that the service metadata is correct
				refreshServiceMetadataInfo(appName, instances);

				// then , refresh general service associated with current application
				refreshGeneralServiceInfo(appName, oldGroup, newGroup);

				// mark process successful
				reSubscribeManager.onRefreshSuccess(event);
			}
			catch (Exception e) {
				logger.error(String.format(
						"APP %s instance changed, handler faild, try resubscribe",
						appName), e);
				reSubscribeManager.onRefreshFail(event);
			}
		}
	}

	private void refreshGeneralServiceInfo(String appName,
			Map<String, List<ServiceInstance>> oldGroup,
			Map<String, List<ServiceInstance>> newGroup) {

		Set<URL> urls2refresh = new HashSet<>();

		// compare with local
		for (String revision : oldGroup.keySet()) {

			if (!newGroup.containsKey(revision)) {
				// all instances of this list with revision has losted
				urlSubscribeHandlerMap.forEach((url, handler) -> {
					if (handler.relatedWith(appName, revision)) {
						handler.removeAppNameWithRevision(appName, revision);
						urls2refresh.add(url);
					}
				});
				logger.debug("Subscription app {} revision {} has all losted", appName,
						revision);
			}
		}

		for (Map.Entry<String, List<ServiceInstance>> entry : newGroup.entrySet()) {
			String revision = entry.getKey();
			List<ServiceInstance> instanceList = entry.getValue();

			if (!oldGroup.containsKey(revision)) {
				// this instance list of revision not exists
				// should acquire urls
				urlSubscribeHandlerMap.forEach(
						(url, handler) -> handler.init(appName, revision, instanceList));
			}

			urlSubscribeHandlerMap.forEach((url, handler) -> {
				if (handler.relatedWith(appName, revision)) {
					urls2refresh.add(url);
				}
			});

			if (logger.isDebugEnabled()) {
				logger.debug("Subscription app {} revision {} changed, instance list {}",
						appName, revision,
						instanceList.stream().map(
								instance -> instance.getHost() + ":" + instance.getPort())
								.collect(Collectors.toList()));
			}
		}

		serviceRevisionInstanceMap.put(appName, newGroup);

		if (urls2refresh.size() == 0) {
			logger.debug("Subscription app {}, no urls will be refreshed", appName);
		}
		else {
			logger.debug("Subscription app {}, the following url will be refresh:{}",
					appName, urls2refresh.stream().map(URL::getServiceKey)
							.collect(Collectors.toList()));

			for (URL url : urls2refresh) {
				GenearalServiceSubscribeHandler handler = urlSubscribeHandlerMap.get(url);
				if (handler == null) {
					logger.warn("Subscription app {}, can't find handler for service {}",
							appName, url.getServiceKey());
					continue;
				}
				handler.refresh();
			}
		}
	}

	private void refreshServiceMetadataInfo(String serviceName,
			List<ServiceInstance> serviceInstances) {
		MetadataServiceSubscribeHandler handler = metadataSubscribeHandlerMap
				.get(serviceName);

		if (handler == null) {
			logger.warn("Subscription app {}, can't find metadata handler", serviceName);
			return;
		}
		handler.refresh(serviceInstances);
	}

	private boolean serviceInstanceNotChanged(Map<String, List<ServiceInstance>> oldGroup,
			Map<String, List<ServiceInstance>> newGroup) {
		if (newGroup.size() != oldGroup.size()) {
			return false;
		}

		for (Map.Entry<String, List<ServiceInstance>> entry : newGroup.entrySet()) {
			String appName = entry.getKey();
			List<ServiceInstance> newInstances = entry.getValue();

			if (!oldGroup.containsKey(appName)) {
				return false;
			}

			List<ServiceInstance> oldInstances = oldGroup.get(appName);
			if (newInstances.size() != oldInstances.size()) {
				return false;
			}

			boolean matched = newInstances.stream().allMatch(newInstance -> {

				for (ServiceInstance oldInstance : oldInstances) {
					if (instanceSame(newInstance, oldInstance)) {
						return true;
					}
				}

				return false;
			});
			if (!matched) {
				return false;
			}
		}

		return true;
	}

	private boolean instanceSame(ServiceInstance newInstance,
			ServiceInstance oldInstance) {
		if (!StringUtils.equals(newInstance.getInstanceId(),
				oldInstance.getInstanceId())) {
			return false;
		}
		if (!StringUtils.equals(newInstance.getHost(), oldInstance.getHost())) {
			return false;
		}
		if (!StringUtils.equals(newInstance.getServiceId(), oldInstance.getServiceId())) {
			return false;
		}
		if (!StringUtils.equals(newInstance.getScheme(), oldInstance.getScheme())) {
			return false;
		}
		if (oldInstance.getPort() != newInstance.getPort()) {
			return false;
		}

		if (!oldInstance.getMetadata().equals(newInstance.getMetadata())) {
			return false;
		}

		return true;
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
		if (subscribedServices.contains("*")) {
			subscribedServices = new HashSet<>(discoveryClient.getServices());
		}
		// TODO Add the filter feature
		return subscribedServices;
	}

	List<ServiceInstance> getServiceInstances(String serviceName) {
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

	// the group of DubboMetadataService is current application name
	private String getServiceName(URL subscribedURL) {
		return subscribedURL.getParameter(GROUP_KEY);
	}

	private boolean containsProviderCategory(URL subscribedURL) {
		String category = subscribedURL.getParameter(CATEGORY_KEY);
		return category != null && category.contains(PROVIDER);
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

	protected boolean isConsumerServiceURL(URL url) {
		return CONSUMER.equals(url.getProtocol());
	}

	public List<ServiceInstance> getServiceInstances(Map<String, Set<String>> providers) {
		List<ServiceInstance> instances = new ArrayList<>();

		providers.forEach((appName, revisions) -> {
			Map<String, List<ServiceInstance>> revisionMap = serviceRevisionInstanceMap
					.get(appName);
			if (revisionMap == null) {
				return;
			}
			for (String revision : revisions) {
				List<ServiceInstance> list = revisionMap.get(revision);
				if (list != null) {
					instances.addAll(list);
				}
			}
		});

		return instances;
	}

	public Map<String, Map<String, List<ServiceInstance>>> getServiceRevisionInstanceMap() {
		return serviceRevisionInstanceMap;
	}

}
