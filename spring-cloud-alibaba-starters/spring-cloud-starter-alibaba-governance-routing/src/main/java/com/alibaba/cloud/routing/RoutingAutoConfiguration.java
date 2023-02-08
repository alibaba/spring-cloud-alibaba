/*
 * Copyright 2022-2023 the original author or authors.
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

package com.alibaba.cloud.routing;

import com.alibaba.cloud.routing.listener.RoutingDataListener;
import com.alibaba.cloud.routing.repository.FilterService;
import com.alibaba.cloud.routing.repository.RoutingDataRepository;

import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author HH
 * @since 2.2.10-RC1
 */
@Configuration(proxyBeanMethods = false)
@AutoConfigureOrder(RoutingAutoConfiguration.ROUTING_AUTO_CONFIG_ORDER)
public class RoutingAutoConfiguration {

	/**
	 * Order of label routing auto config.
	 */
	public static final int ROUTING_AUTO_CONFIG_ORDER = 10;

	@Bean
	@ConditionalOnMissingBean
	public RoutingDataRepository routingDataRepository() {
		return new RoutingDataRepository();
	}

	@Bean
	@ConditionalOnMissingBean
	public FilterService filterService() {
		return new FilterService();
	}

	@Bean
	public RoutingDataListener routingDataListener(
			RoutingDataRepository routingDataRepository, FilterService filterService) {
		return new RoutingDataListener(routingDataRepository, filterService);
	}

}
