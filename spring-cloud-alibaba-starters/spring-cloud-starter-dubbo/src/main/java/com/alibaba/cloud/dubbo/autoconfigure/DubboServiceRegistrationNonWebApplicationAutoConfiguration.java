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

import java.util.List;

import com.alibaba.cloud.dubbo.metadata.repository.DubboServiceMetadataRepository;
import com.alibaba.cloud.dubbo.registry.event.ServiceInstancePreRegisteredEvent;
import com.ecwid.consul.v1.agent.model.NewService;
import org.apache.dubbo.common.URL;
import org.apache.dubbo.config.spring.ServiceBean;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnNotWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.serviceregistry.Registration;
import org.springframework.cloud.client.serviceregistry.ServiceRegistry;
import org.springframework.cloud.consul.serviceregistry.ConsulAutoRegistration;
import org.springframework.cloud.consul.serviceregistry.ConsulRegistration;
import org.springframework.cloud.zookeeper.serviceregistry.ServiceInstanceRegistration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

import static com.alibaba.cloud.dubbo.autoconfigure.DubboServiceRegistrationAutoConfiguration.CONSUL_AUTO_SERVICE_AUTO_CONFIGURATION_CLASS_NAME;
import static com.alibaba.cloud.dubbo.autoconfigure.DubboServiceRegistrationAutoConfiguration.ZOOKEEPER_AUTO_SERVICE_AUTO_CONFIGURATION_CLASS_NAME;

/**
 * Dubbo Service Registration Auto-{@link Configuration} for Non-Web application.
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnNotWebApplication
@ConditionalOnProperty(value = "spring.cloud.service-registry.auto-registration.enabled",
		matchIfMissing = true)
@AutoConfigureAfter(DubboServiceRegistrationAutoConfiguration.class)
@Aspect
public class DubboServiceRegistrationNonWebApplicationAutoConfiguration {

	private static final String REST_PROTOCOL = "rest";

	@Autowired
	private ServiceRegistry serviceRegistry;

	@Autowired
	private Registration registration;

	private volatile Integer serverPort = null;

	private volatile boolean registered = false;

	@Autowired
	private DubboServiceMetadataRepository repository;

	@Around("execution(* org.springframework.cloud.client.serviceregistry.Registration.getPort())")
	public Object getPort(ProceedingJoinPoint pjp) throws Throwable {
		/**
		 * move setServerPort from onApplicationStarted() to here for this issue :
		 * https://github.com/alibaba/spring-cloud-alibaba/issues/1383
		 * @author <a href="mailto:chenxilzx1@gmail.com">theonefx</a>
		 */
		setServerPort();
		return serverPort != null ? serverPort : pjp.proceed();
	}

	@EventListener(ApplicationStartedEvent.class)
	public void onApplicationStarted() {
		register();
	}

	private void register() {
		if (registered) {
			return;
		}
		serviceRegistry.register(registration);
		registered = true;
	}

	/**
	 * Set web port from {@link ServiceBean#getExportedUrls() exported URLs} if "rest"
	 * protocol is present.
	 */
	private void setServerPort() {
		if (serverPort == null) {
			synchronized (DubboServiceRegistrationNonWebApplicationAutoConfiguration.class) {
				if (serverPort == null) {
					for (List<URL> urls : repository.getAllExportedUrls().values()) {
						urls.stream().filter(
								url -> REST_PROTOCOL.equalsIgnoreCase(url.getProtocol()))
								.findFirst().ifPresent(url -> {
									serverPort = url.getPort();
								});

						// If REST protocol is not present, use any applied port.
						if (serverPort == null) {
							urls.stream().findAny().ifPresent(url -> {
								serverPort = url.getPort();
							});
						}
					}
				}
			}
		}
	}

	@Configuration(proxyBeanMethods = false)
	@ConditionalOnBean(name = ZOOKEEPER_AUTO_SERVICE_AUTO_CONFIGURATION_CLASS_NAME)
	class ZookeeperConfiguration implements SmartInitializingSingleton {

		@Autowired
		private ServiceInstanceRegistration registration;

		@EventListener(ServiceInstancePreRegisteredEvent.class)
		public void onServiceInstancePreRegistered(
				ServiceInstancePreRegisteredEvent event) {
			setServerPort();
			registration.setPort(serverPort);
		}

		@Override
		public void afterSingletonsInstantiated() {
			// invoke getServiceInstance() method to trigger the ServiceInstance building
			// before register
			registration.getServiceInstance();
		}

	}

	@Configuration(proxyBeanMethods = false)
	@ConditionalOnBean(name = CONSUL_AUTO_SERVICE_AUTO_CONFIGURATION_CLASS_NAME)
	class ConsulConfiguration {

		/**
		 * Handle the pre-registered event of {@link ServiceInstance} for Consul.
		 * @param event {@link ServiceInstancePreRegisteredEvent}
		 */
		@EventListener(ServiceInstancePreRegisteredEvent.class)
		public void onServiceInstancePreRegistered(
				ServiceInstancePreRegisteredEvent event) {
			Registration registration = event.getSource();
			ConsulAutoRegistration consulRegistration = (ConsulAutoRegistration) registration;
			setPort(consulRegistration);
		}

		/**
		 * Set port on Non-Web Application.
		 * @param consulRegistration {@link ConsulRegistration}
		 */
		private void setPort(ConsulAutoRegistration consulRegistration) {
			int port = consulRegistration.getPort();
			NewService newService = consulRegistration.getService();
			if (newService.getPort() == null) {
				newService.setPort(port);
			}
		}

	}

}
