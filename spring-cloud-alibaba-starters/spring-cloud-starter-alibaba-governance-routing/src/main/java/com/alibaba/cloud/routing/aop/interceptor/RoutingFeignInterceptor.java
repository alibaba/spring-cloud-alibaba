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

package com.alibaba.cloud.routing.aop.interceptor;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import com.alibaba.cloud.commons.lang.StringUtils;
import com.alibaba.cloud.routing.constant.RoutingConstants;
import com.alibaba.cloud.routing.context.RoutingContext;
import com.alibaba.cloud.routing.properties.RoutingProperties;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Value;

/**
 * @author HH
 * @since 2.2.10-RC1
 */
public class RoutingFeignInterceptor implements RequestInterceptor {

	// Whether the core policy header is passed on Feign.
	// When the global subscription is started, you can disable the core policy header
	// delivery,
	// which can save the size of the transmitted data and improve performance to a
	// certain extent.
	@Value("${" + RoutingConstants.FEIGN_CORE_HEADER_TRANSMISSION_ENABLED + ":true}")
	protected Boolean feignCoreHeaderTransmissionEnabled;

	@Resource
	private RoutingProperties routingProperties;

	private static final Logger LOG = LoggerFactory
			.getLogger(RoutingFeignInterceptor.class);

	@Override
	public void apply(RequestTemplate requestTemplate) {

		applyHeader(requestTemplate);

	}

	private void applyHeader(RequestTemplate requestTemplate) {

		// Use map to simplify if... else statement
		Map<String, String> propertiesMap = new HashMap<>();
		propertiesMap.put(RoutingConstants.SCA_ROUTING_SERVICE_ZONE,
				routingProperties.getZone());
		RoutingContext.getCurrentContext().setZone(routingProperties.getZone());
		propertiesMap.put(RoutingConstants.SCA_ROUTING_SERVICE_REGION,
				routingProperties.getRegion());
		RoutingContext.getCurrentContext().setRegion(routingProperties.getRegion());

		propertiesMap.forEach((k, v) -> {
			if (StringUtils.isNotEmpty(k)
					&& !StringUtils.equals(k, RoutingConstants.DEFAULT)) {
				requestTemplate.header(k, v);
			}
		});

	}

}
