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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import com.alibaba.cloud.routing.constant.RoutingConstants;
import com.alibaba.cloud.routing.context.RoutingContext;
import com.alibaba.cloud.routing.properties.RoutingProperties;
import com.alibaba.nacos.common.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

/**
 * @author yuluo
 * @author 1481556636@qq.com
 */

public class RoutingRestTemplateInterceptor implements ClientHttpRequestInterceptor {

	// Whether the RestTemplate core policy header is passed.
	// When the global subscription is started, you can disable the core policy header
	// delivery,
	// which can save the size of the transmitted data and improve performance to a
	// certain extent
	@Value("${" + RoutingConstants.REST_CORE_HEADER_TRANSMISSION_ENABLED + ":true}")
	protected Boolean restTemplateCoreHeaderTransmissionEnabled;

	private static final Logger log = LoggerFactory
			.getLogger(RoutingRestTemplateInterceptor.class);

	@Resource
	private RoutingProperties routingProperties;

	@Override
	public ClientHttpResponse intercept(HttpRequest request, byte[] body,
			ClientHttpRequestExecution execution) throws IOException {

		applyHeader(request);

		return execution.execute(request, body);
	}

	private void applyHeader(HttpRequest request) {

		HttpHeaders headers = request.getHeaders();

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
				headers.add(k, v);
			}
		});

	}

}
