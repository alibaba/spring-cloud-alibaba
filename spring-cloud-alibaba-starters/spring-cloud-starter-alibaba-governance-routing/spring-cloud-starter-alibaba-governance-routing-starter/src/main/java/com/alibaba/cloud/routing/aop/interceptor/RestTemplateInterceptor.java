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

import com.alibaba.cloud.routing.constant.LabelRoutingConstants;
import com.alibaba.cloud.routing.context.LabelRoutingContext;
import com.alibaba.cloud.routing.properties.LabelRoutingProperties;
import com.alibaba.nacos.common.utils.StringUtils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

/**
 * @author yuluo
 * @author <a href="1481556636@qq.com"></a>
 */

public class RestTemplateInterceptor implements ClientHttpRequestInterceptor {

	/**
	 * Whether the RestTemplate core policy header is passed. When the global subscription
	 * is started, you can disable the core policy header delivery, which can save the
	 * size of the transmitted data and improve performance to certain extent
	 */
	@Value("${" + LabelRoutingConstants.REST_HEADER_TRANSMISSION_ENABLED + ":true}")
	protected Boolean restTemplateCoreHeaderTransmissionEnabled;

	@Resource
	private LabelRoutingProperties properties;

	@Override
	public ClientHttpResponse intercept(HttpRequest request, byte[] body,
			ClientHttpRequestExecution execution) throws IOException {

		applyHeader(request);

		return execution.execute(request, body);
	}

	private void applyHeader(HttpRequest request) {

		Map<String, String> routingPropertiesMap = new HashMap<>();
		HttpHeaders headers = request.getHeaders();

		routingPropertiesMap.put(LabelRoutingConstants.SCA_ROUTING_SERVICE_ZONE,
				properties.getZone());
		LabelRoutingContext.getCurrentContext().setRoutingZone(properties.getZone());
		routingPropertiesMap.put(LabelRoutingConstants.SCA_ROUTING_SERVICE_REGION,
				properties.getRegion());
		LabelRoutingContext.getCurrentContext().setRoutingRegion(properties.getRegion());

		routingPropertiesMap.forEach((k, v) -> {
			if (StringUtils.isNotEmpty(k)
					&& !StringUtils.equals(k, LabelRoutingConstants.DEFAULT)) {

				headers.add(k, v);
			}
		});

	}

}
