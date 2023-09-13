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

package com.alibaba.cloud.routing.zuul.filter.defaults;

import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;

import javax.annotation.Resource;

import com.alibaba.cloud.routing.constant.LabelRoutingConstants;
import com.alibaba.cloud.routing.properties.LabelRoutingProperties;
import com.alibaba.cloud.routing.zuul.constants.RoutingZuulConstants;
import com.alibaba.cloud.routing.zuul.context.LabelRoutingZuulContext;
import com.alibaba.cloud.routing.zuul.filter.LabelRoutingZuulFilter;
import com.alibaba.cloud.routing.zuul.util.LabelRoutingZuulFilterResolver;
import com.netflix.zuul.context.RequestContext;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants;

/**
 * @author yuluo
 * @author <a href="1481556636@qq.com"></a>
 */

public class DefaultLabelRoutingZuulFilter extends LabelRoutingZuulFilter {

	// Zuul filter order.
	@Value("${" + RoutingZuulConstants.ZUUL_ROUTE_FILTER_ORDER + ":"
			+ RoutingZuulConstants.ZUUL_ROUTE_FILTER_ORDER_VALUE + "}")
	protected Integer zuulFilterOrder;

	// Gateway rule priority switch.
	@Value("${" + RoutingZuulConstants.ZUUL_HEADER_PRIORITY + ":true}")
	protected Boolean zuulHeaderPriority;

	private static Map<String, String> routingPropertiesMap = new ConcurrentSkipListMap<>();

	@Resource
	private LabelRoutingProperties properties;

	@Override
	public String filterType() {

		return FilterConstants.PRE_TYPE;
	}

	@Override
	public int filterOrder() {
		return zuulFilterOrder;
	}

	@Override
	public boolean shouldFilter() {
		return true;
	}

	@Override
	public Object run() {

		RequestContext context = RequestContext.getCurrentContext();
		applyRequestHeader(context);

		return null;
	}

	private void applyRequestHeader(RequestContext context) {

		routingPropertiesMap.put(LabelRoutingConstants.SCA_ROUTING_SERVICE_ZONE,
				properties.getZone());
		LabelRoutingZuulContext.getCurrentContext().setZone(properties.getZone());
		routingPropertiesMap.put(LabelRoutingConstants.SCA_ROUTING_SERVICE_REGION,
				properties.getRegion());
		LabelRoutingZuulContext.getCurrentContext().setRegion(properties.getRegion());

		LabelRoutingZuulContext.getCurrentContext()
				.setHttpServletRequest(context.getRequest());

		routingPropertiesMap.forEach((k, v) -> LabelRoutingZuulFilterResolver.setHeader(context, k, v,
				zuulHeaderPriority));

	}

}
