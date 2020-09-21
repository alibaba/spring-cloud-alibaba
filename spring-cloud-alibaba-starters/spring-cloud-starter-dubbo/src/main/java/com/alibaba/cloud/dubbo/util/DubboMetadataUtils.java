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

package com.alibaba.cloud.dubbo.util;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import com.alibaba.cloud.dubbo.service.DubboMetadataService;
import org.apache.dubbo.common.URL;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.core.env.Environment;

import static java.lang.String.format;
import static org.apache.dubbo.common.constants.CommonConstants.VERSION_KEY;
import static org.apache.dubbo.registry.client.metadata.ServiceInstanceMetadataUtils.METADATA_SERVICE_URLS_PROPERTY_NAME;
import static org.springframework.util.StringUtils.hasText;

/**
 * The utilities class of Dubbo Metadata.
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 */
public class DubboMetadataUtils {

	/**
	 * The {@link String#format(String, Object...) pattern} of dubbo protocols port.
	 */
	public static final String DUBBO_PROTOCOLS_PORT_PROPERTY_NAME_PATTERN = "dubbo.protocols.%s.port";

	private final JSONUtils jsonUtils;

	private final Environment environment;

	private final String currentApplicationName;

	public DubboMetadataUtils(JSONUtils jsonUtils, Environment environment) {
		this.jsonUtils = jsonUtils;
		this.environment = environment;
		this.currentApplicationName = environment.getProperty("spring.application.name",
				environment.getProperty("dubbo.application.name", "application"));
	}

	/**
	 * Get the current application name.
	 * @return non-null
	 */
	public String getCurrentApplicationName() {
		return currentApplicationName;
	}

	/**
	 * Get the {@link URL urls} that {@link DubboMetadataService} exported by the
	 * specified {@link ServiceInstance}.
	 * @param serviceInstance {@link ServiceInstance}
	 * @return the mutable {@link URL urls}
	 */
	public List<URL> getDubboMetadataServiceURLs(ServiceInstance serviceInstance) {
		Map<String, String> metadata = serviceInstance.getMetadata();
		String dubboURLsJSON = metadata.get(METADATA_SERVICE_URLS_PROPERTY_NAME);
		return jsonUtils.toURLs(dubboURLsJSON);
	}

	/**
	 * Get the {@link URL urls} that {@link DubboMetadataService} exported by the
	 * specified {@link ServiceInstance ServiceInstances}.
	 * @param serviceInstances the list of {@link ServiceInstance ServiceInstances}
	 * @param serviceInterface the interface name of Dubbo service
	 * @param version the version of Dubbo service
	 * @param protocol the protocol that Dubbo Service exports
	 * @return the mutable {@link URL urls}
	 */
	public List<URL> getDubboMetadataServiceURLs(List<ServiceInstance> serviceInstances,
			String serviceInterface, String version, String protocol) {
		return serviceInstances.stream().map(this::getDubboMetadataServiceURLs)
				.flatMap(List::stream)
				.filter(url -> protocol == null
						|| Objects.equals(protocol, url.getProtocol()))
				.filter(url -> Objects.equals(serviceInterface,
						url.getServiceInterface()))
				.filter(url -> Objects.equals(version, url.getParameter(VERSION_KEY)))
				.collect(Collectors.toList());
	}

	/**
	 * Get the property name of Dubbo Protocol.
	 * @param protocol Dubbo Protocol
	 * @return non-null
	 */
	public String getDubboProtocolPropertyName(String protocol) {
		return format(DUBBO_PROTOCOLS_PORT_PROPERTY_NAME_PATTERN, protocol);
	}

	public Integer getDubboProtocolPort(ServiceInstance serviceInstance,
			String protocol) {
		String protocolProperty = getDubboProtocolPropertyName(protocol);
		Map<String, String> metadata = serviceInstance.getMetadata();
		String protocolPort = metadata.get(protocolProperty);
		return hasText(protocolPort) ? Integer.valueOf(protocolPort) : null;
	}

}
