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

package com.alibaba.cloud.dubbo.autoconfigure;

import java.util.*;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.alibaba.cloud.dubbo.env.DubboCloudProperties;
import com.alibaba.cloud.dubbo.metadata.repository.DubboServiceMetadataRepository;
import com.alibaba.cloud.dubbo.registry.AbstractSpringCloudRegistry;
import com.alibaba.cloud.dubbo.registry.event.ServiceInstancesChangedEvent;
import com.alibaba.cloud.dubbo.registry.event.SubscribedServicesChangedEvent;
import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import com.alibaba.cloud.nacos.NacosServiceManager;
import com.alibaba.cloud.nacos.discovery.NacosWatch;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.listener.NamingEvent;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.netflix.discovery.CacheRefreshedEvent;
import com.netflix.discovery.shared.Applications;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.listen.Listenable;
import org.apache.curator.framework.listen.ListenerContainer;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;
import org.apache.curator.framework.recipes.cache.TreeCacheListener;
import org.apache.dubbo.common.URL;
import org.apache.dubbo.registry.NotifyListener;
import org.apache.zookeeper.Watcher;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.discovery.event.HeartbeatEvent;
import org.springframework.cloud.client.discovery.event.InstanceRegisteredEvent;
import org.springframework.cloud.consul.discovery.ConsulCatalogWatch;
import org.springframework.cloud.netflix.eureka.CloudEurekaClient;
import org.springframework.cloud.zookeeper.discovery.ZookeeperDiscoveryProperties;
import org.springframework.cloud.zookeeper.discovery.ZookeeperServiceWatch;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.ReflectionUtils;

import static com.alibaba.cloud.dubbo.autoconfigure.DubboServiceDiscoveryAutoConfiguration.CONSUL_DISCOVERY_AUTO_CONFIGURATION_CLASS_NAME;
import static com.alibaba.cloud.dubbo.autoconfigure.DubboServiceDiscoveryAutoConfiguration.NACOS_DISCOVERY_AUTO_CONFIGURATION_CLASS_NAME;
import static com.alibaba.cloud.dubbo.autoconfigure.DubboServiceDiscoveryAutoConfiguration.ZOOKEEPER_DISCOVERY_AUTO_CONFIGURATION_CLASS_NAME;
import static com.alibaba.cloud.dubbo.autoconfigure.DubboServiceRegistrationAutoConfiguration.EUREKA_CLIENT_AUTO_CONFIGURATION_CLASS_NAME;
import static com.alibaba.cloud.nacos.discovery.NacosServiceDiscovery.hostToServiceInstanceList;
import static org.springframework.util.StringUtils.hasText;

/**
 * Dubbo Service Discovery Auto {@link Configuration} (after
 * {@link DubboServiceRegistrationAutoConfiguration}).
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see DubboServiceRegistrationAutoConfiguration
 * @see Configuration
 * @see DiscoveryClient
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(name = "org.springframework.cloud.client.discovery.DiscoveryClient")
@ConditionalOnProperty(name = "spring.cloud.discovery.enabled", matchIfMissing = true)
@AutoConfigureAfter(name = { EUREKA_CLIENT_AUTO_CONFIGURATION_CLASS_NAME,
		ZOOKEEPER_DISCOVERY_AUTO_CONFIGURATION_CLASS_NAME,
		CONSUL_DISCOVERY_AUTO_CONFIGURATION_CLASS_NAME,
		NACOS_DISCOVERY_AUTO_CONFIGURATION_CLASS_NAME }, value = {
				DubboServiceRegistrationAutoConfiguration.class })
public class DubboServiceDiscoveryAutoConfiguration {

	/**
	 * ZookeeperDiscoveryAutoConfiguration.
	 */
	public static final String ZOOKEEPER_DISCOVERY_AUTO_CONFIGURATION_CLASS_NAME = "org.springframework.cloud.zookeeper.discovery.ZookeeperDiscoveryAutoConfiguration";

	/**
	 * ConsulDiscoveryClientConfiguration.
	 */
	public static final String CONSUL_DISCOVERY_AUTO_CONFIGURATION_CLASS_NAME = "org.springframework.cloud.consul.discovery.ConsulDiscoveryClientConfiguration";

	/**
	 * NacosDiscoveryAutoConfiguration.
	 */
	public static final String NACOS_DISCOVERY_AUTO_CONFIGURATION_CLASS_NAME = "com.alibaba.cloud.nacos.discovery.NacosDiscoveryAutoConfiguration";

	private final DubboServiceMetadataRepository dubboServiceMetadataRepository;

	private final Logger logger = LoggerFactory.getLogger(getClass());

	private final ApplicationEventPublisher applicationEventPublisher;

	private final DiscoveryClient discoveryClient;

	private final AtomicReference<Object> heartbeatState = new AtomicReference<>();

	private List<ServiceInstance> oldServiceInstances = new ArrayList<>();

	/**
	 * @see #defaultHeartbeatEventChangePredicate()
	 */
	private final ObjectProvider<Predicate<HeartbeatEvent>> heartbeatEventChangedPredicate;

	@Value("${spring.application.name:${dubbo.application.name:application}}")
	private String currentApplicationName;

	public DubboServiceDiscoveryAutoConfiguration(
			DubboServiceMetadataRepository dubboServiceMetadataRepository,
			ApplicationEventPublisher applicationEventPublisher,
			DiscoveryClient discoveryClient,
			ObjectProvider<Predicate<HeartbeatEvent>> heartbeatEventChangedPredicate) {
		this.dubboServiceMetadataRepository = dubboServiceMetadataRepository;
		this.applicationEventPublisher = applicationEventPublisher;
		this.discoveryClient = discoveryClient;
		this.heartbeatEventChangedPredicate = heartbeatEventChangedPredicate;
	}

	/**
	 * Dispatch a {@link ServiceInstancesChangedEvent}.
	 * @param serviceName the name of service
	 * @param serviceInstances the {@link ServiceInstance instances} of some service
	 * @see AbstractSpringCloudRegistry#registerServiceInstancesChangedEventListener(URL,
	 * NotifyListener)
	 */
	private void dispatchServiceInstancesChangedEvent(String serviceName,
			List<ServiceInstance> serviceInstances) {
		if (!hasText(serviceName) || Objects.equals(currentApplicationName, serviceName)
				|| serviceInstances == null) {
			return;
		}

		ServiceInstancesChangedEvent event = new ServiceInstancesChangedEvent(serviceName,
				serviceInstances);
		if (logger.isInfoEnabled()) {
			logger.info(
					"The event of the service instances[name : {} , size : {}] change is about to be dispatched",
					serviceName, serviceInstances.size());
		}
		applicationEventPublisher.publishEvent(event);
	}

	private List<ServiceInstance> getInstances(String serviceName) {
		return discoveryClient.getInstances(serviceName);
	}

	/**
	 * Dispatch a {@link ServiceInstancesChangedEvent} when the {@link HeartbeatEvent}
	 * raised, use for these scenarios:
	 * <ul>
	 * <li>Eureka : {@link CloudEurekaClient#onCacheRefreshed()} publishes a
	 * {@link HeartbeatEvent} instead of {@link CacheRefreshedEvent}</li>
	 * <li>Zookeeper :
	 * {@link ZookeeperServiceWatch#childEvent(CuratorFramework, TreeCacheEvent)}
	 * publishes a {@link HeartbeatEvent} when
	 * {@link ZookeeperDiscoveryProperties#getRoot() the services' root path} has been
	 * changed</li>
	 * <li>Consul : {@link ConsulCatalogWatch#catalogServicesWatch()} publishes a
	 * {@link HeartbeatEvent} when
	 * <a href="https://www.consul.io/api/features/blocking.html">Consul's Blocking
	 * Queries response</a></li>
	 * <li>Nacos : {@link NacosWatch#nacosServicesWatch()} publishes a
	 * {@link HeartbeatEvent} under a {@link TaskScheduler} every
	 * {@link NacosDiscoveryProperties#getWatchDelay()} milliseconds</li>
	 * </ul>
	 * <p>
	 * In order to reduce the duplicated handling for
	 * {@link ServiceInstancesChangedEvent},
	 * {@link #defaultHeartbeatEventChangePredicate()} method providers the default
	 * implementation to detect whether the {@link HeartbeatEvent#getValue() state} is
	 * changed or not. If and only if changed, the
	 * {@link #dispatchServiceInstancesChangedEvent(String, Collection)} will be executed.
	 * <p>
	 * <b>Note : </b> Spring Cloud {@link HeartbeatEvent} has a critical flaw that can't
	 * figure out which service was changed precisely, it's just used for a notification
	 * that the {@link DubboServiceMetadataRepository#getSubscribedServices() subscribed
	 * services} used to {@link NotifyListener#notify(List) notify} the Dubbo consumer
	 * cached {@link URL URLs}. Because of some {@link DiscoveryClient} implementations
	 * have the better and fine-grained the event mechanism for service instances change,
	 * thus {@link HeartbeatEvent} handle will be ignored in these scenarios:
	 * <ul>
	 * <li>Zookeeper : {@link Watcher}, see
	 * {@link ZookeeperConfiguration#heartbeatEventChangedPredicate()}</li>
	 * <li>Nacos : {@link com.alibaba.nacos.api.naming.listener.EventListener} , see
	 * {@link NacosConfiguration#heartbeatEventChangedPredicate()}</li>
	 * </ul>
	 * <p>
	 * If the customized {@link DiscoveryClient} also providers the similar mechanism, the
	 * implementation could declare a Spring Bean of {@link Predicate} of
	 * {@link HeartbeatEvent} to override {@link #defaultHeartbeatEventChangePredicate()
	 * default one}.
	 * @param event the instance of {@link HeartbeatEvent}
	 * @see HeartbeatEvent
	 */
	@EventListener(HeartbeatEvent.class)
	public void onHeartbeatEvent(HeartbeatEvent event) {
		/**
		 * Try to re-initialize the subscribed services, in order to sense the change of
		 * services if {@link DubboCloudProperties#getSubscribedServices()} is wildcard
		 * that indicates all services should be subscribed.
		 */
		Stream<String> subscribedServices = dubboServiceMetadataRepository
				.initSubscribedServices();

		heartbeatEventChangedPredicate.ifAvailable(predicate -> {
			if (predicate.test(event)) {
				// Dispatch ServiceInstancesChangedEvent for each service
				subscribedServices.forEach(serviceName -> {

					List<ServiceInstance> newServiceInstances = getInstances(serviceName);
					List<ServiceInstance> changedServiceInstances = newServiceInstances
							.stream().filter(this::filter).collect(Collectors.toList());
					oldServiceInstances = newServiceInstances;
					dispatchServiceInstancesChangedEvent(serviceName, changedServiceInstances);
				});
			}
		});
	}

	private boolean filter(ServiceInstance serviceInstance) {
		for (ServiceInstance oldServiceInstance : oldServiceInstances) {
			if(oldServiceInstance.equals(serviceInstance)){
				return false;
			}
		}
		return true;
	}

	/**
	 * The default {@link Predicate} implementation of {@link HeartbeatEvent} based on the
	 * comparison between previous and current {@link HeartbeatEvent#getValue() state
	 * value}, the {@link DiscoveryClient} implementation may override current.
	 * @return {@link Predicate} {@link HeartbeatEvent}
	 * @see EurekaConfiguration#heartbeatEventChangedPredicate()
	 */
	@Bean
	@ConditionalOnMissingBean
	public Predicate<HeartbeatEvent> defaultHeartbeatEventChangePredicate() {
		return event -> {
			Object oldState = heartbeatState.get();
			Object newState = event.getValue();
			return heartbeatState.compareAndSet(oldState, newState)
					&& !Objects.equals(oldState, newState);
		};
	}

	/**
	 * Eureka Customized Configuration.
	 */
	@Configuration(proxyBeanMethods = false)
	@ConditionalOnBean(name = EUREKA_CLIENT_AUTO_CONFIGURATION_CLASS_NAME)
	public class EurekaConfiguration {

		private final AtomicReference<String> appsHashCodeCache = new AtomicReference<>();

		/**
		 * Compare {@link Applications#getAppsHashCode() apps hash code} between last
		 * {@link Applications} and current.
		 *
		 * @see Applications#getAppsHashCode()
		 * @return HeartbeatEvent Predicate
		 */
		@Bean
		public Predicate<HeartbeatEvent> heartbeatEventChangedPredicate() {
			return event -> {
				String oldAppsHashCode = appsHashCodeCache.get();
				CloudEurekaClient cloudEurekaClient = (CloudEurekaClient) event
						.getSource();
				Applications applications = cloudEurekaClient.getApplications();
				String appsHashCode = applications.getAppsHashCode();
				return appsHashCodeCache.compareAndSet(oldAppsHashCode, appsHashCode)
						&& !Objects.equals(oldAppsHashCode, appsHashCode);
			};
		}

	}

	/**
	 * Zookeeper Customized Configuration.
	 */
	@Configuration(proxyBeanMethods = false)
	@ConditionalOnBean(name = ZOOKEEPER_DISCOVERY_AUTO_CONFIGURATION_CLASS_NAME)
	@Aspect
	public class ZookeeperConfiguration
			implements ApplicationListener<InstanceRegisteredEvent> {

		/**
		 * The pointcut expression for
		 * {@link ZookeeperServiceWatch#childEvent(CuratorFramework, TreeCacheEvent)}.
		 */
		public static final String CHILD_EVENT_POINTCUT_EXPRESSION = "execution(void org.springframework.cloud.zookeeper.discovery.ZookeeperServiceWatch.childEvent(..)) && args(client,event)";

		/**
		 * The path separator of Zookeeper node.
		 */
		public static final String NODE_PATH_SEPARATOR = "/";

		/**
		 * The path variable name for the name of service.
		 */
		private static final String SERVICE_NAME_PATH_VARIABLE_NAME = "serviceName";

		/**
		 * The path variable name for the id of {@link ServiceInstance service instance}.
		 */
		private static final String SERVICE_INSTANCE_ID_PATH_VARIABLE_NAME = "serviceInstanceId";

		private final ZookeeperServiceWatch zookeeperServiceWatch;

		private final String rootPath;

		private final AntPathMatcher pathMatcher;

		/**
		 * Ant Path pattern for {@link ServiceInstance} :
		 * <p>
		 * <p>
		 * ${{@link #rootPath}}/{serviceName}/{serviceInstanceId}.
		 *
		 * @see #rootPath
		 * @see #SERVICE_NAME_PATH_VARIABLE_NAME
		 * @see #SERVICE_INSTANCE_ID_PATH_VARIABLE_NAME
		 */
		private final String serviceInstancePathPattern;

		/**
		 * The {@link ThreadLocal} holds the processed service name.
		 */
		private final ThreadLocal<String> processedServiceNameThreadLocal;

		private List<ServiceInstance> oldServiceInstances = new ArrayList<>();

		ZookeeperConfiguration(ZookeeperDiscoveryProperties zookeeperDiscoveryProperties,
				ZookeeperServiceWatch zookeeperServiceWatch) {
			this.zookeeperServiceWatch = zookeeperServiceWatch;
			this.rootPath = zookeeperDiscoveryProperties.getRoot();
			this.pathMatcher = new AntPathMatcher(NODE_PATH_SEPARATOR);
			this.serviceInstancePathPattern = rootPath + NODE_PATH_SEPARATOR + "{"
					+ SERVICE_NAME_PATH_VARIABLE_NAME + "}" + NODE_PATH_SEPARATOR + "{"
					+ SERVICE_INSTANCE_ID_PATH_VARIABLE_NAME + "}";
			this.processedServiceNameThreadLocal = new ThreadLocal<>();
		}

		/**
		 * Zookeeper uses {@link TreeCacheEvent} to trigger
		 * {@link #dispatchServiceInstancesChangedEvent(String, Collection)} , thus
		 * {@link HeartbeatEvent} handle is always ignored.
		 * @return <code>false</code> forever
		 */
		@Bean
		public Predicate<HeartbeatEvent> heartbeatEventChangedPredicate() {
			return event -> false;
		}

		/**
		 * Handle on {@link InstanceRegisteredEvent} after
		 * {@link ZookeeperServiceWatch#onApplicationEvent(InstanceRegisteredEvent)}.
		 * @param event {@link InstanceRegisteredEvent}
		 * @see #reattachTreeCacheListeners()
		 */
		@Override
		public void onApplicationEvent(InstanceRegisteredEvent event) {
			reattachTreeCacheListeners();
		}

		/**
		 * Re-attach the {@link TreeCacheListener TreeCacheListeners}.
		 */
		private void reattachTreeCacheListeners() {

			TreeCache treeCache = zookeeperServiceWatch.getCache();

			Listenable<TreeCacheListener> listenable = treeCache.getListenable();

			/**
			 * All registered TreeCacheListeners except {@link ZookeeperServiceWatch}.
			 * Usually, "otherListeners" will be empty because Spring Cloud Zookeeper only
			 * adds "zookeeperServiceWatch" bean as {@link TreeCacheListener}.
			 */
			List<TreeCacheListener> otherListeners = new LinkedList<>();

			if (listenable instanceof ListenerContainer) {
				ListenerContainer<TreeCacheListener> listenerContainer = (ListenerContainer) listenable;
				listenerContainer.forEach(listener -> {
					/**
					 * Even though "listener" is an instance of
					 * {@link ZookeeperServiceWatch}, "zookeeperServiceWatch" bean that
					 * was enhanced by AOP is different from the former, thus it's
					 * required to exclude "listener".
					 */
					if (!(listener instanceof ZookeeperServiceWatch)) {
						otherListeners.add(listener);
					}
					return null;
				});

				// remove all TreeCacheListeners temporarily
				listenerContainer.clear();
				// re-store zookeeperServiceWatch, and make sure it's first one
				// now "beforeChildEvent" is available for Spring AOP
				listenerContainer.addListener(zookeeperServiceWatch);
				// re-store others
				otherListeners.forEach(listenerContainer::addListener);
			}
			else {
				if (logger.isWarnEnabled()) {
					logger.warn(
							"Tell me which version Curator framework current application used? I will do better :D");
				}
			}
		}

		/**
		 * Try to {@link #dispatchServiceInstancesChangedEvent(String, Collection)
		 * dispatch} {@link ServiceInstancesChangedEvent} before
		 * {@link ZookeeperServiceWatch#childEvent(CuratorFramework, TreeCacheEvent)}
		 * execution if required.
		 * @param client {@link CuratorFramework}
		 * @param event {@link TreeCacheEvent}
		 */
		@Before(CHILD_EVENT_POINTCUT_EXPRESSION)
		public void beforeChildEvent(CuratorFramework client, TreeCacheEvent event) {
			if (supportsEventType(event)) {
				String serviceName = resolveServiceName(event);
				List<ServiceInstance> newServiceInstances = getInstances(serviceName);
				List<ServiceInstance> changedServiceInstances = newServiceInstances
						.stream().filter(this::filter).collect(Collectors.toList());
				oldServiceInstances = newServiceInstances;
				if (hasText(serviceName)) {
					dispatchServiceInstancesChangedEvent(serviceName,
							changedServiceInstances);
				}
			}
		}

		@After(CHILD_EVENT_POINTCUT_EXPRESSION)
		public void afterChildEvent(CuratorFramework client, TreeCacheEvent event) {
		}

		/**
		 * Resolve the name of service.
		 * @param event {@link TreeCacheEvent}
		 * @return If the Zookeeper's {@link ChildData#getPath() node path} that was
		 * notified comes from {@link ServiceInstance the service instance}, return it's
		 * parent path as the service name, or return <code>null</code>
		 */
		private String resolveServiceName(TreeCacheEvent event) {
			ChildData childData = event.getData();
			String path = childData.getPath();
			if (logger.isDebugEnabled()) {
				logger.debug("ZK node[path : {}] event type : {}", path, event.getType());
			}

			String serviceName = null;

			if (pathMatcher.match(serviceInstancePathPattern, path)) {
				Map<String, String> variables = pathMatcher
						.extractUriTemplateVariables(serviceInstancePathPattern, path);
				serviceName = variables.get(SERVICE_NAME_PATH_VARIABLE_NAME);
			}

			return serviceName;
		}

		/**
		 * The {@link TreeCacheEvent#getType() event type} is supported or not.
		 * @param event {@link TreeCacheEvent}
		 * @return the rule is same as
		 * {@link ZookeeperServiceWatch#childEvent(CuratorFramework, TreeCacheEvent)}
		 * method
		 */
		private boolean supportsEventType(TreeCacheEvent event) {
			TreeCacheEvent.Type eventType = event.getType();
			return eventType.equals(TreeCacheEvent.Type.NODE_ADDED)
					|| eventType.equals(TreeCacheEvent.Type.NODE_REMOVED)
					|| eventType.equals(TreeCacheEvent.Type.NODE_UPDATED);
		}

		public boolean filter(ServiceInstance serviceInstance) {
			for (ServiceInstance oldServiceInstance : oldServiceInstances) {
				if (oldServiceInstance.equals(serviceInstance)) {
					return false;
				}
			}
			return true;
		}

	}

	/**
	 * Consul Customized Configuration.
	 */
	@Configuration(proxyBeanMethods = false)
	@ConditionalOnBean(name = CONSUL_DISCOVERY_AUTO_CONFIGURATION_CLASS_NAME)
	class ConsulConfiguration {

	}

	/**
	 * Nacos Customized Configuration.
	 */
	@Configuration(proxyBeanMethods = false)
	@ConditionalOnBean(name = NACOS_DISCOVERY_AUTO_CONFIGURATION_CLASS_NAME)
	class NacosConfiguration {

		private final NamingService namingService;

		private final NacosDiscoveryProperties nacosDiscoveryProperties;

		/**
		 * the set of services is listening.
		 */
		private final Set<String> listeningServices;

		NacosConfiguration(NacosServiceManager nacosServiceManager,
				NacosDiscoveryProperties nacosDiscoveryProperties) {
			this.namingService = nacosServiceManager
					.getNamingService(nacosDiscoveryProperties.getNacosProperties());
			this.nacosDiscoveryProperties = nacosDiscoveryProperties;
			this.listeningServices = new ConcurrentSkipListSet<>();
		}

		/**
		 * Nacos uses {@link EventListener} to trigger.
		 * {@link #dispatchServiceInstancesChangedEvent(String, Collection)} , thus
		 * {@link HeartbeatEvent} handle is always ignored
		 * @return <code>false</code> forever
		 */
		@Bean
		public Predicate<HeartbeatEvent> heartbeatEventChangedPredicate() {
			return event -> false;
		}

		@EventListener(SubscribedServicesChangedEvent.class)
		public void onSubscribedServicesChangedEvent(SubscribedServicesChangedEvent event)
				throws Exception {
			// subscribe EventListener for each service
			event.getNewSubscribedServices().forEach(this::subscribeEventListener);
		}

		private List<Instance> oldInstances = new ArrayList<>();

		private void subscribeEventListener(String serviceName) {
			if (listeningServices.add(serviceName)) {
				try {
					String group = nacosDiscoveryProperties.getGroup();
					namingService.subscribe(serviceName, group, event -> {
						// why twice when instance register
						if (event instanceof NamingEvent) {
							NamingEvent namingEvent = (NamingEvent) event;
							List<Instance> changedInstances = namingEvent.getInstances()
									.stream().filter(this::filter)
									.collect(Collectors.toList());
							oldInstances = namingEvent.getInstances();
							List<ServiceInstance> serviceInstances = hostToServiceInstanceList(
									changedInstances, serviceName);
							dispatchServiceInstancesChangedEvent(serviceName,
									serviceInstances);
						}
					});
				}
				catch (NacosException e) {
					ReflectionUtils.rethrowRuntimeException(e);
				}
			}
		}

		public boolean filter(Instance instance) {
			for (Instance oldInstance : oldInstances) {
				if (oldInstance.equals(instance)) {
					return false;
				}
			}
			return true;
		}

	}

}
