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

package com.alibaba.cloud.routing.zuul.util;

import com.alibaba.cloud.commons.lang.StringUtils;
import com.netflix.zuul.context.RequestContext;

/**
 * @author yuluo
 * @author 1481556636@qq.com
 */

public final class RoutingZuulFilterResolver {

	private RoutingZuulFilterResolver() {
	}

	public static void setHeader(RequestContext context, String headerName,
			String headerValue, Boolean zuulHeaderPriority) {
		if (StringUtils.isEmpty(headerValue)) {
			return;
		}

		if (zuulHeaderPriority) {

			context.addZuulRequestHeader(headerName, headerValue);
		}
		else {

			String header = context.getRequest().getHeader(headerName);

			if (StringUtils.isEmpty(header)) {

				context.addZuulRequestHeader(headerName, headerValue);
			}

		}
	}

}
