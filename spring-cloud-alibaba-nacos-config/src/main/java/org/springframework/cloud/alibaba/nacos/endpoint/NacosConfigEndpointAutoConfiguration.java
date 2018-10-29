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

package org.springframework.cloud.alibaba.nacos.endpoint;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.endpoint.condition.ConditionalOnEnabledEndpoint;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.cloud.alibaba.nacos.NacosConfigProperties;
import org.springframework.cloud.alibaba.nacos.NacosPropertySourceRepository;
import org.springframework.cloud.alibaba.nacos.refresh.NacosRefreshHistory;
import org.springframework.context.annotation.Bean;

/**
 * @author xiaojing
 */
@ConditionalOnWebApplication
@ConditionalOnClass(value = Endpoint.class)
public class NacosConfigEndpointAutoConfiguration {

	@Autowired
	private NacosConfigProperties nacosConfigProperties;

	@Autowired
	private NacosRefreshHistory nacosRefreshHistory;

	@Autowired
	private NacosPropertySourceRepository nacosPropertySourceRepository;

	@ConditionalOnMissingBean
	@ConditionalOnEnabledEndpoint
	@Bean
	public NacosConfigEndpoint nacosConfigEndpoint() {
		return new NacosConfigEndpoint(nacosConfigProperties, nacosRefreshHistory,
				nacosPropertySourceRepository);
	}

	@Bean
	public NacosConfigHealthIndicator nacosConfigHealthIndicator(
			NacosPropertySourceRepository nacosPropertySourceRepository) {
		return new NacosConfigHealthIndicator(nacosConfigProperties,
				nacosPropertySourceRepository,
				nacosConfigProperties.configServiceInstance());
	}
}
