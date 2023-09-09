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

package com.alibaba.cloud.consumer.resttemplate.interceptor;

import java.io.IOException;
import java.io.InputStream;

import com.alibaba.cloud.consumer.resttemplate.util.RestConsumerUtil;

import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;

/**
 * @author yuluo
 * @author 1481556636@qq.com
 */

@Component
public class RestRequestInterceptor implements ClientHttpRequestInterceptor {

	private static String serverPort = null;

	@Override
	public ClientHttpResponse intercept(HttpRequest request, byte[] body,
			ClientHttpRequestExecution execution) throws IOException {

		ClientHttpResponse response = execution.execute(request, body);
		InputStream responseBody = response.getBody();

		serverPort = RestConsumerUtil.getResult(responseBody).substring(21, 26);

		return response;
	}

	public String getServerPort() {

		return serverPort;
	}

}
