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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.cloud.sidecar.SidecarProperties;
import com.ecwid.consul.v1.agent.model.NewService;

import org.springframework.cloud.client.serviceregistry.AutoServiceRegistrationProperties;
import org.springframework.cloud.consul.discovery.ConsulDiscoveryProperties;
import org.springframework.cloud.consul.discovery.HeartbeatProperties;
import org.springframework.cloud.consul.serviceregistry.ConsulAutoRegistration;
import org.springframework.cloud.consul.serviceregistry.ConsulManagementRegistrationCustomizer;
import org.springframework.cloud.consul.serviceregistry.ConsulRegistrationCustomizer;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

/**
 * @author www.itmuch.com
 */
public class SidecarConsulAutoRegistration extends ConsulAutoRegistration {

	public SidecarConsulAutoRegistration(NewService service,
			AutoServiceRegistrationProperties autoServiceRegistrationProperties,
			ConsulDiscoveryProperties properties, ApplicationContext context,
			HeartbeatProperties heartbeatProperties,
			List<ConsulManagementRegistrationCustomizer> managementRegistrationCustomizers) {
		super(service, autoServiceRegistrationProperties, properties, context,
				heartbeatProperties, managementRegistrationCustomizers);
	}

	public static ConsulAutoRegistration registration(
			AutoServiceRegistrationProperties autoServiceRegistrationProperties,
			ConsulDiscoveryProperties properties, ApplicationContext context,
			List<ConsulRegistrationCustomizer> registrationCustomizers,
			List<ConsulManagementRegistrationCustomizer> managementRegistrationCustomizers,
			HeartbeatProperties heartbeatProperties,
			SidecarProperties sidecarProperties) {

		NewService service = new NewService();
		String appName = getAppName(properties, context.getEnvironment());
		service.setId(getInstanceId(sidecarProperties, context.getEnvironment()));
		if (!properties.isPreferAgentAddress()) {
			service.setAddress(sidecarProperties.getIp());
		}
		service.setName(normalizeForDns(appName));
		service.setTags(new ArrayList<>(properties.getTags()));
		service.setEnableTagOverride(properties.getEnableTagOverride());
		service.setMeta(getMetadata(properties));

		if (sidecarProperties.getPort() != null && sidecarProperties.getPort() > 0) {
			service.setPort(properties.getPort());
		}
		else if (properties.getPort() != null && properties.getPort() > 0) {
			service.setPort(properties.getPort());
		}
		else if (context.getEnvironment().getProperty("server.port") != null) {
			// set health check, use alibaba sidecar self's port rather than polyglot
			// app's port.
			service.setPort(
					Integer.valueOf(context.getEnvironment().getProperty("server.port")));
		}

		if (service.getPort() != null) {
			// we know the port and can set the check
			setCheck(service, autoServiceRegistrationProperties, properties, context,
					heartbeatProperties);
		}

		ConsulAutoRegistration registration = new ConsulAutoRegistration(service,
				autoServiceRegistrationProperties, properties, context,
				heartbeatProperties, managementRegistrationCustomizers);
		customize(registrationCustomizers, registration);
		return registration;
	}

	/**
	 * copyed from
	 * org.springframework.cloud.consul.serviceregistry.ConsulAutoRegistration#getMetadata.
	 */
	private static Map<String, String> getMetadata(ConsulDiscoveryProperties properties) {
		LinkedHashMap<String, String> metadata = new LinkedHashMap<>();
		if (!CollectionUtils.isEmpty(properties.getMetadata())) {
			metadata.putAll(properties.getMetadata());
		}

		// add metadata from other properties. See createTags above.
		if (!StringUtils.isEmpty(properties.getInstanceZone())) {
			metadata.put(properties.getDefaultZoneMetadataName(),
					properties.getInstanceZone());
		}
		if (!StringUtils.isEmpty(properties.getInstanceGroup())) {
			metadata.put("group", properties.getInstanceGroup());
		}

		// store the secure flag in the tags so that clients will be able to figure
		// out whether to use http or https automatically
		metadata.put("secure",
				Boolean.toString(properties.getScheme().equalsIgnoreCase("https")));

		return metadata;
	}

	public static String getInstanceId(SidecarProperties sidecarProperties,
			Environment environment) {
		return String.format("%s-%s-%s",
				environment.getProperty("spring.application.name"),
				sidecarProperties.getIp(), sidecarProperties.getPort());
	}

}
