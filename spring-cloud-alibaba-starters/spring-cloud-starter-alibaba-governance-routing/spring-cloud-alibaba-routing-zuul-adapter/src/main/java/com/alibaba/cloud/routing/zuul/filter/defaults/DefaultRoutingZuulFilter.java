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

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import com.alibaba.cloud.routing.constant.RoutingConstants;
import com.alibaba.cloud.routing.properties.RoutingProperties;
import com.alibaba.cloud.routing.zuul.constants.RoutingZuulConstants;
import com.alibaba.cloud.routing.zuul.context.RoutingZuulContext;
import com.alibaba.cloud.routing.zuul.filter.RoutingZuulFilter;
import com.alibaba.cloud.routing.zuul.util.RoutingZuulFilterResolver;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants;

/**
 * @author yuluo
 * @author 1481556636@qq.com
 */

public class DefaultRoutingZuulFilter extends RoutingZuulFilter {

	// Filter order.
	@Value("${" + RoutingZuulConstants.ZUUL_ROUTE_FILTER_ORDER + ":"
			+ RoutingZuulConstants.ZUUL_ROUTE_FILTER_ORDER_VALUE + "}")
	protected Integer filterOrder;

	// Gateway rule priority switch.
	@Value("${" + RoutingZuulConstants.ZUUL_HEADER_PRIORITY + ":true}")
	protected Boolean zuulHeaderPriority;

	@Resource
	private RoutingProperties routingProperties;

	@Override
	public String filterType() {

		return FilterConstants.PRE_TYPE;
	}

	@Override
	public int filterOrder() {
		return filterOrder;
	}

	@Override
	public boolean shouldFilter() {
		return true;
	}

	@Override
	public Object run() throws ZuulException {

		RequestContext context = RequestContext.getCurrentContext();

		// 处理内部Header的转发
		applyHeader(context);

		return null;
	}

	private void applyHeader(RequestContext context) {

		// Use map to simplify if... else statement
		Map<String, String> propertiesMap = new HashMap<>();
		propertiesMap.put(RoutingConstants.SCA_ROUTING_SERVICE_ZONE,
				routingProperties.getZone());
		RoutingZuulContext.getCurrentContext().setZone(routingProperties.getZone());
		propertiesMap.put(RoutingConstants.SCA_ROUTING_SERVICE_REGION,
				routingProperties.getRegion());
		RoutingZuulContext.getCurrentContext().setRegion(routingProperties.getRegion());

		RoutingZuulContext.getCurrentContext()
				.setHttpServletRequest(context.getRequest());

		propertiesMap.forEach((k, v) -> RoutingZuulFilterResolver.setHeader(context, k, v,
				zuulHeaderPriority));

	}

}
