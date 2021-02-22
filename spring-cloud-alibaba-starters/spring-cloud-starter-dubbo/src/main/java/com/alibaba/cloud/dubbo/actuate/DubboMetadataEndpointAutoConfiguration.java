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

package com.alibaba.cloud.dubbo.actuate;

import com.alibaba.cloud.dubbo.actuate.endpoint.DubboDiscoveryEndpoint;
import com.alibaba.cloud.dubbo.actuate.endpoint.DubboExportedURLsEndpoint;
import com.alibaba.cloud.dubbo.actuate.endpoint.DubboRestMetadataEndpoint;

import org.springframework.boot.actuate.autoconfigure.endpoint.condition.ConditionalOnAvailableEndpoint;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * Dubbo Metadata Endpoints Auto-{@link Configuration}.
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 */
@ConditionalOnClass(
		name = "org.springframework.boot.actuate.endpoint.annotation.Endpoint")
@PropertySource("classpath:/META-INF/dubbo/default/actuator-endpoints.properties")
@Configuration(proxyBeanMethods = false)
public class DubboMetadataEndpointAutoConfiguration {

	@Bean
	@ConditionalOnMissingBean
	@ConditionalOnAvailableEndpoint
	public DubboRestMetadataEndpoint dubboRestMetadataEndpoint() {
		return new DubboRestMetadataEndpoint();
	}

	@Bean
	@ConditionalOnMissingBean
	@ConditionalOnAvailableEndpoint
	public DubboDiscoveryEndpoint dubboDiscoveryEndpoint() {

		return new DubboDiscoveryEndpoint();
	}

	@Bean
	@ConditionalOnMissingBean
	@ConditionalOnAvailableEndpoint
	public DubboExportedURLsEndpoint dubboServiceMetadataEndpoint() {
		return new DubboExportedURLsEndpoint();
	}

}
