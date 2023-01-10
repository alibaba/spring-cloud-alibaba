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

package com.alibaba.cloud.router;

import com.alibaba.cloud.router.listener.LabelRouteDataListener;
import com.alibaba.cloud.router.repository.FilterService;
import com.alibaba.cloud.router.repository.RouteDataRepository;

import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@AutoConfigureOrder(LabelRoutingAutoConfiguration.LABEL_ROUTING_AUTO_CONFIG_ORDER)
public class LabelRoutingAutoConfiguration {

	/**
	 * Order of label routing auto config.
	 */
	public static final int LABEL_ROUTING_AUTO_CONFIG_ORDER = 10;

	@Bean
	@ConditionalOnMissingBean
	public RouteDataRepository routeDataRepository() {
		return new RouteDataRepository();
	}

	@Bean
	@ConditionalOnMissingBean
	public FilterService filterService() {
		return new FilterService();
	}

	@Bean
	public LabelRouteDataListener labelRouteDataListener(
			RouteDataRepository routeDataRepository, FilterService filterService) {
		return new LabelRouteDataListener(routeDataRepository, filterService);
	}

}
