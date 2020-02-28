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

package com.alibaba.cloud.sidecar.consul;

import java.util.List;

import com.alibaba.cloud.sidecar.SidecarAutoConfiguration;
import com.alibaba.cloud.sidecar.SidecarDiscoveryClient;
import com.alibaba.cloud.sidecar.SidecarProperties;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.cloud.client.serviceregistry.AutoServiceRegistrationProperties;
import org.springframework.cloud.consul.discovery.ConsulDiscoveryProperties;
import org.springframework.cloud.consul.discovery.HeartbeatProperties;
import org.springframework.cloud.consul.serviceregistry.ConsulAutoRegistration;
import org.springframework.cloud.consul.serviceregistry.ConsulAutoServiceRegistrationAutoConfiguration;
import org.springframework.cloud.consul.serviceregistry.ConsulManagementRegistrationCustomizer;
import org.springframework.cloud.consul.serviceregistry.ConsulRegistrationCustomizer;
import org.springframework.cloud.consul.serviceregistry.ConsulServiceRegistry;
import org.springframework.cloud.consul.serviceregistry.ConsulServiceRegistryAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author www.itmuch.com
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(ConsulServiceRegistryAutoConfiguration.class)
@AutoConfigureBefore({ ConsulAutoServiceRegistrationAutoConfiguration.class,
		SidecarAutoConfiguration.class })
public class SidecarConsulAutoConfiguration {

	@Bean
	public ConsulAutoRegistration consulRegistration(
			AutoServiceRegistrationProperties autoServiceRegistrationProperties,
			ConsulDiscoveryProperties properties, ApplicationContext applicationContext,
			ObjectProvider<List<ConsulRegistrationCustomizer>> registrationCustomizers,
			ObjectProvider<List<ConsulManagementRegistrationCustomizer>> managementRegistrationCustomizers,
			HeartbeatProperties heartbeatProperties,
			SidecarProperties sidecarProperties) {
		return SidecarConsulAutoRegistration.registration(
				autoServiceRegistrationProperties, properties, applicationContext,
				registrationCustomizers.getIfAvailable(),
				managementRegistrationCustomizers.getIfAvailable(), heartbeatProperties,
				sidecarProperties);
	}

	@Bean
	public SidecarDiscoveryClient sidecarDiscoveryClient(
			ConsulDiscoveryProperties properties, ConsulServiceRegistry serviceRegistry,
			ConsulAutoRegistration registration) {
		return new SidecarConsulDiscoveryClient(properties, serviceRegistry,
				registration);
	}

}
