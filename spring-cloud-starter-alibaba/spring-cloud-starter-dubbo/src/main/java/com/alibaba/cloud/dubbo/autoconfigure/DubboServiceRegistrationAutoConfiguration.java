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

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.alibaba.cloud.dubbo.autoconfigure.condition.MissingSpringCloudRegistryConfigPropertyCondition;
import com.alibaba.cloud.dubbo.metadata.repository.DubboServiceMetadataRepository;
import com.alibaba.cloud.dubbo.registry.DubboServiceRegistrationEventPublishingAspect;
import com.alibaba.cloud.dubbo.registry.event.ServiceInstancePreRegisteredEvent;
import com.ecwid.consul.v1.agent.model.NewService;
import com.netflix.appinfo.InstanceInfo;
import org.apache.dubbo.config.RegistryConfig;
import org.apache.dubbo.config.spring.ServiceBean;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.serviceregistry.Registration;
import org.springframework.cloud.consul.serviceregistry.ConsulRegistration;
import org.springframework.cloud.netflix.eureka.serviceregistry.EurekaAutoServiceRegistration;
import org.springframework.cloud.netflix.eureka.serviceregistry.EurekaRegistration;
import org.springframework.cloud.netflix.eureka.serviceregistry.EurekaServiceRegistry;
import org.springframework.context.SmartLifecycle;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.event.EventListener;

import static com.alibaba.cloud.dubbo.autoconfigure.DubboServiceRegistrationAutoConfiguration.CONSUL_AUTO_SERVICE_AUTO_CONFIGURATION_CLASS_NAME;
import static com.alibaba.cloud.dubbo.autoconfigure.DubboServiceRegistrationAutoConfiguration.EUREKA_CLIENT_AUTO_CONFIGURATION_CLASS_NAME;
import static com.alibaba.cloud.dubbo.registry.SpringCloudRegistryFactory.ADDRESS;
import static com.alibaba.cloud.dubbo.registry.SpringCloudRegistryFactory.PROTOCOL;
import static org.springframework.util.ObjectUtils.isEmpty;

/**
 * Dubbo Service Registration Auto-{@link Configuration}.
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 */
@Configuration(proxyBeanMethods = false)
@Import({ DubboServiceRegistrationEventPublishingAspect.class })
@ConditionalOnProperty(value = "spring.cloud.service-registry.auto-registration.enabled",
		matchIfMissing = true)
@AutoConfigureAfter(name = { EUREKA_CLIENT_AUTO_CONFIGURATION_CLASS_NAME,
		CONSUL_AUTO_SERVICE_AUTO_CONFIGURATION_CLASS_NAME,
		"org.springframework.cloud.client.serviceregistry.AutoServiceRegistrationAutoConfiguration" },
		value = { DubboMetadataAutoConfiguration.class })
public class DubboServiceRegistrationAutoConfiguration {

	/**
	 * EurekaClientAutoConfiguration.
	 */
	public static final String EUREKA_CLIENT_AUTO_CONFIGURATION_CLASS_NAME = "org.springframework.cloud.netflix.eureka.EurekaClientAutoConfiguration";

	/**
	 * ConsulAutoServiceRegistrationAutoConfiguration.
	 */
	public static final String CONSUL_AUTO_SERVICE_AUTO_CONFIGURATION_CLASS_NAME = "org.springframework.cloud.consul.serviceregistry.ConsulAutoServiceRegistrationAutoConfiguration";

	/**
	 * ConsulAutoRegistration.
	 */
	public static final String CONSUL_AUTO_SERVICE_AUTO_REGISTRATION_CLASS_NAME = "org.springframework.cloud.consul.serviceregistry.ConsulAutoRegistration";

	/**
	 * ZookeeperAutoServiceRegistrationAutoConfiguration.
	 */
	public static final String ZOOKEEPER_AUTO_SERVICE_AUTO_CONFIGURATION_CLASS_NAME = "org.springframework.cloud.zookeeper.serviceregistry.ZookeeperAutoServiceRegistrationAutoConfiguration";

	private static final Logger logger = LoggerFactory
			.getLogger(DubboServiceRegistrationAutoConfiguration.class);

	@Autowired
	private DubboServiceMetadataRepository dubboServiceMetadataRepository;

	@Bean
	@Conditional({ MissingSpringCloudRegistryConfigPropertyCondition.class })
	public RegistryConfig defaultSpringCloudRegistryConfig() {
		return new RegistryConfig(ADDRESS, PROTOCOL);
	}

	@EventListener(ServiceInstancePreRegisteredEvent.class)
	public void onServiceInstancePreRegistered(ServiceInstancePreRegisteredEvent event) {
		Registration registration = event.getSource();
		attachDubboMetadataServiceMetadata(registration);
	}

	private void attachDubboMetadataServiceMetadata(Registration registration) {
		if (registration == null) {
			return;
		}
		synchronized (registration) {
			Map<String, String> metadata = registration.getMetadata();
			attachDubboMetadataServiceMetadata(metadata);
		}
	}

	private void attachDubboMetadataServiceMetadata(Map<String, String> metadata) {
		Map<String, String> serviceMetadata = dubboServiceMetadataRepository
				.getDubboMetadataServiceMetadata();
		if (!isEmpty(serviceMetadata)) {
			metadata.putAll(serviceMetadata);
		}
	}

	@Configuration(proxyBeanMethods = false)
	@ConditionalOnBean(name = EUREKA_CLIENT_AUTO_CONFIGURATION_CLASS_NAME)
	@Aspect
	class EurekaConfiguration implements SmartInitializingSingleton {

		@Autowired
		private ObjectProvider<Collection<ServiceBean>> serviceBeans;

		@EventListener(ServiceInstancePreRegisteredEvent.class)
		public void onServiceInstancePreRegistered(
				ServiceInstancePreRegisteredEvent event) {
			Registration registration = event.getSource();
			EurekaRegistration eurekaRegistration = EurekaRegistration.class
					.cast(registration);
			InstanceInfo instanceInfo = eurekaRegistration.getApplicationInfoManager()
					.getInfo();
			attachDubboMetadataServiceMetadata(instanceInfo.getMetadata());
		}

		/**
		 * {@link EurekaServiceRegistry} will register current {@link ServiceInstance
		 * service instance} on {@link EurekaAutoServiceRegistration#start()} execution(in
		 * {@link SmartLifecycle#start() start phase}), thus this method must
		 * {@link ServiceBean#export() export} all {@link ServiceBean ServiceBeans} in
		 * advance.
		 */
		@Override
		public void afterSingletonsInstantiated() {
			Collection<ServiceBean> serviceBeans = this.serviceBeans.getIfAvailable();
			if (!isEmpty(serviceBeans)) {
				serviceBeans.forEach(ServiceBean::export);
			}
		}

	}

	@Configuration(proxyBeanMethods = false)
	@ConditionalOnBean(name = CONSUL_AUTO_SERVICE_AUTO_CONFIGURATION_CLASS_NAME)
	@AutoConfigureOrder
	class ConsulConfiguration {

		/**
		 * Handle the pre-registered event of {@link ServiceInstance} for Consul.
		 * @param event {@link ServiceInstancePreRegisteredEvent}
		 */
		@EventListener(ServiceInstancePreRegisteredEvent.class)
		public void onServiceInstancePreRegistered(
				ServiceInstancePreRegisteredEvent event) {
			Registration registration = event.getSource();
			Class<?> registrationClass = AopUtils.getTargetClass(registration);
			String registrationClassName = registrationClass.getName();
			if (CONSUL_AUTO_SERVICE_AUTO_REGISTRATION_CLASS_NAME
					.equalsIgnoreCase(registrationClassName)) {
				ConsulRegistration consulRegistration = (ConsulRegistration) registration;
				attachURLsIntoMetadata(consulRegistration);
			}
		}

		private void attachURLsIntoMetadata(ConsulRegistration consulRegistration) {
			NewService newService = consulRegistration.getService();
			Map<String, String> serviceMetadata = dubboServiceMetadataRepository
					.getDubboMetadataServiceMetadata();
			if (!isEmpty(serviceMetadata)) {
				List<String> tags = newService.getTags();
				for (Map.Entry<String, String> entry : serviceMetadata.entrySet()) {
					tags.add(entry.getKey() + "=" + entry.getValue());
				}
			}
		}

	}

}
