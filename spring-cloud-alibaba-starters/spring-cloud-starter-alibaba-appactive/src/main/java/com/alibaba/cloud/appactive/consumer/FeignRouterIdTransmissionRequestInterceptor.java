/*
 * Copyright 2013-2023 the original author or authors.
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

package com.alibaba.cloud.appactive.consumer;

import javax.servlet.http.HttpServletRequest;

import com.alibaba.cloud.appactive.common.UriContext;
import com.alibaba.cloud.appactive.constant.AppactiveConstants;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import io.appactive.java.api.base.AppContextClient;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * @author raozihao, mageekchiu
 * @author <a href="mailto:zihaorao@gmail.com">Steve</a>
 */
public class FeignRouterIdTransmissionRequestInterceptor implements RequestInterceptor {

	@Override
	public void apply(RequestTemplate requestTemplate) {
		ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder
				.getRequestAttributes();
		if (requestAttributes == null) {
			return;
		}
		HttpServletRequest request = requestAttributes.getRequest();
		if (request == null) {
			return;
		}
		requestTemplate.header(AppactiveConstants.ROUTER_ID_HEADER_KEY,
				AppContextClient.getRouteId());
		// store uri for routing filter
		UriContext.setUriPath(requestTemplate.url());
	}

}
