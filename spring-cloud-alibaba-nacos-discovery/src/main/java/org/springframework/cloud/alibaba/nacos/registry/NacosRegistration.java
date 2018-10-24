/*
 * Copyright (C) 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.alibaba.nacos.registry;

import org.springframework.cloud.alibaba.nacos.NacosDiscoveryProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.DefaultServiceInstance;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.ManagementServerPortUtils;
import org.springframework.cloud.client.serviceregistry.Registration;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.util.Map;

import javax.annotation.PostConstruct;

import com.alibaba.nacos.api.naming.NamingService;

/**
 * @author xiaojing
 */
public class NacosRegistration implements Registration, ServiceInstance {

	private static final String MANAGEMENT_PORT = "management.port";
	private static final String MANAGEMENT_CONTEXT_PATH = "management.context-path";
	private static final String MANAGEMENT_ADDRESS = "management.address";

	@Autowired
	private NacosDiscoveryProperties nacosDiscoveryProperties;

	@Autowired
	private ApplicationContext context;

	@PostConstruct
	public void init() {

		Environment env = context.getEnvironment();
		Integer managementPort = ManagementServerPortUtils.getPort(context);
		if (null != managementPort) {
			Map<String, String> metadata = nacosDiscoveryProperties.getMetadata();
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

	public NamingService getNacosNamingService() {
		return nacosDiscoveryProperties.namingServiceInstance();
	}

	public void setNacosDiscoveryProperties(
			NacosDiscoveryProperties nacosDiscoveryProperties) {
		this.nacosDiscoveryProperties = nacosDiscoveryProperties;
	}

	@Override
	public String toString() {
		return "NacosRegistration{" + "nacosDiscoveryProperties="
				+ nacosDiscoveryProperties + '}';
	}
}
