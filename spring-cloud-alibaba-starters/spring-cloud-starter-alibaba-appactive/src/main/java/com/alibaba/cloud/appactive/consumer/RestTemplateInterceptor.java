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

import java.io.IOException;

import com.alibaba.cloud.appactive.common.UriContext;
import com.alibaba.cloud.appactive.constant.AppactiveConstants;
import io.appactive.java.api.base.AppContextClient;
import io.appactive.support.log.LogUtil;
import org.slf4j.Logger;

import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

/**
 * @author raozihao, mageekchiu
 * @author <a href="mailto:zihaorao@gmail.com">Steve</a>
 */
public class RestTemplateInterceptor implements ClientHttpRequestInterceptor {

	private static final Logger logger = LogUtil.getLogger();

	@Override
	public ClientHttpResponse intercept(HttpRequest request, byte[] body,
			ClientHttpRequestExecution execution) throws IOException {

		request.getHeaders().add(AppactiveConstants.ROUTER_ID_HEADER_KEY,
				AppContextClient.getRouteId());
		UriContext.setUriPath(request.getURI().getPath());

		ClientHttpResponse response = execution.execute(request, body);

		logger.info("RestTemplateInterceptor uri {} for request {} got cleared",
				UriContext.getUriPath(), request.getURI());
		UriContext.clearContext();
		return response;
	}

}
