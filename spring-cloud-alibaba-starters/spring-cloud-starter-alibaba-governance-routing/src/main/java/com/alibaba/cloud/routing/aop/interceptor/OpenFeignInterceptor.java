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

import com.alibaba.cloud.routing.constant.LabelRoutingConstants;
import com.alibaba.cloud.routing.context.LabelRoutingContext;
import com.alibaba.cloud.routing.properties.LabelRoutingProperties;
import com.alibaba.nacos.common.utils.StringUtils;
import feign.RequestInterceptor;
import feign.RequestTemplate;

import org.springframework.beans.factory.annotation.Value;

/**
 * @author yuluo
 * @author <a href="1481556636@qq.com"></a>
 */

public class OpenFeignInterceptor implements RequestInterceptor {

	/**
	 * Whether the core policy header is passed on Feign. When the global subscription is
	 * started, you can disable the core policy header delivery, which can save the size
	 * of the transmitted data and improve performance to certain extent.
	 */
	@Value("${" + LabelRoutingConstants.WebClient.FEIGN_HEADER_TRANSMISSION_ENABLED
			+ ":true}")
	protected Boolean feignCoreHeaderTransmissionEnabled;

	@Resource
	private LabelRoutingProperties properties;

	@Override
	public void apply(RequestTemplate request) {
		applyRequestHeader(request);

	}

	private void applyRequestHeader(RequestTemplate request) {
		Map<String, String> routingPropertiesMap = new HashMap<>();
		routingPropertiesMap.put(LabelRoutingConstants.SCA_ROUTING_SERVICE_ZONE,
				properties.getZone());
		LabelRoutingContext.getCurrentContext().setRoutingZone(properties.getZone());
		routingPropertiesMap.put(LabelRoutingConstants.SCA_ROUTING_SERVICE_REGION,
				properties.getRegion());
		LabelRoutingContext.getCurrentContext().setRoutingRegion(properties.getRegion());

		routingPropertiesMap.forEach((k, v) -> {
			if (StringUtils.isNotEmpty(k)
					&& !StringUtils.equals(k, LabelRoutingConstants.DEFAULT)) {
				request.header(k, v);
			}
		});
	}

}
