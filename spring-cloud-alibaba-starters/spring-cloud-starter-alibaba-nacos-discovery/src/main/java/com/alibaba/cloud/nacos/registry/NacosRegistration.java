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

package com.alibaba.cloud.nacos.registry;

import java.net.URI;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import com.alibaba.nacos.api.naming.PreservedMetadataKeys;

import org.springframework.cloud.client.DefaultServiceInstance;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.ManagementServerPortUtils;
import org.springframework.cloud.client.serviceregistry.Registration;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

/**
 * @author xiaojing
 */
public class NacosRegistration implements Registration, ServiceInstance {

	/**
	 * The metadata key of management port.
	 */
	public static final String MANAGEMENT_PORT = "management.port";

	/**
	 * The metadata key of management context-path.
	 */
	public static final String MANAGEMENT_CONTEXT_PATH = "management.context-path";

	/**
	 * The metadata key of management address.
	 */
	public static final String MANAGEMENT_ADDRESS = "management.address";

	/**
	 * The metadata key of management endpoints web base path.
	 */
	public static final String MANAGEMENT_ENDPOINT_BASE_PATH = "management.endpoints.web.base-path";

	private List<NacosRegistrationCustomizer> registrationCustomizers;

	private NacosDiscoveryProperties nacosDiscoveryProperties;

	private ApplicationContext context;

	public NacosRegistration(List<NacosRegistrationCustomizer> registrationCustomizers,
			NacosDiscoveryProperties nacosDiscoveryProperties,
			ApplicationContext context) {
		this.registrationCustomizers = registrationCustomizers;
		this.nacosDiscoveryProperties = nacosDiscoveryProperties;
		this.context = context;
	}

	@PostConstruct
	public void init() {

		Map<String, String> metadata = nacosDiscoveryProperties.getMetadata();
		Environment env = context.getEnvironment();

		String endpointBasePath = env.getProperty(MANAGEMENT_ENDPOINT_BASE_PATH);
		if (!StringUtils.isEmpty(endpointBasePath)) {
			metadata.put(MANAGEMENT_ENDPOINT_BASE_PATH, endpointBasePath);
		}

		Integer managementPort = ManagementServerPortUtils.getPort(context);
		if (null != managementPort) {
			metadata.put(MANAGEMENT_PORT, managementPort.toString());
			String contextPath = env
					.getProperty("management.server.servlet.context-path");
			String address = env.getProperty("management.server.address");
			if (!StringUtils.isEmpty(contextPath)) {
				metadata.put(MANAGEMENT_CONTEXT_PATH, contextPath);
			}
			if (!StringUtils.isEmpty(address)) {
				metadata.put(MANAGEMENT_ADDRESS, address);
			}
		}

		if (null != nacosDiscoveryProperties.getHeartBeatInterval()) {
			metadata.put(PreservedMetadataKeys.HEART_BEAT_INTERVAL,
					nacosDiscoveryProperties.getHeartBeatInterval().toString());
		}
		if (null != nacosDiscoveryProperties.getHeartBeatTimeout()) {
			metadata.put(PreservedMetadataKeys.HEART_BEAT_TIMEOUT,
					nacosDiscoveryProperties.getHeartBeatTimeout().toString());
		}
		if (null != nacosDiscoveryProperties.getIpDeleteTimeout()) {
			metadata.put(PreservedMetadataKeys.IP_DELETE_TIMEOUT,
					nacosDiscoveryProperties.getIpDeleteTimeout().toString());
		}
		customize(registrationCustomizers, this);
	}

	private static void customize(
			List<NacosRegistrationCustomizer> registrationCustomizers,
			NacosRegistration registration) {
		if (registrationCustomizers != null) {
			for (NacosRegistrationCustomizer customizer : registrationCustomizers) {
				customizer.customize(registration);
			}
		}
	}

	@Override
	public String getServiceId() {
		return nacosDiscoveryProperties.getService();
	}

	@Override
	public String getHost() {
		return nacosDiscoveryProperties.getIp();
	}

	@Override
	public int getPort() {
		return nacosDiscoveryProperties.getPort();
	}

	public void setPort(int port) {
		this.nacosDiscoveryProperties.setPort(port);
	}

	@Override
	public boolean isSecure() {
		return nacosDiscoveryProperties.isSecure();
	}

	@Override
	public URI getUri() {
		return DefaultServiceInstance.getUri(this);
	}

	@Override
	public Map<String, String> getMetadata() {
		return nacosDiscoveryProperties.getMetadata();
	}

	public boolean isRegisterEnabled() {
		return nacosDiscoveryProperties.isRegisterEnabled();
	}

	public String getCluster() {
		return nacosDiscoveryProperties.getClusterName();
	}

	public float getRegisterWeight() {
		return nacosDiscoveryProperties.getWeight();
	}

	public NacosDiscoveryProperties getNacosDiscoveryProperties() {
		return nacosDiscoveryProperties;
	}

	@Override
	public String toString() {
		return "NacosRegistration{" + "nacosDiscoveryProperties="
				+ nacosDiscoveryProperties + '}';
	}

}
