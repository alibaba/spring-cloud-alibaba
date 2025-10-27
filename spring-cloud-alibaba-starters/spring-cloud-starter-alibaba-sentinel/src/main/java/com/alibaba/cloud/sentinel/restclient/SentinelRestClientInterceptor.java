/*
 * Copyright 2013-2025 the original author or authors.
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

package com.alibaba.cloud.sentinel.restclient;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.Tracer;
import com.alibaba.csp.sentinel.slots.block.BlockException;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;


public class SentinelRestClientInterceptor implements ClientHttpRequestInterceptor {

	public SentinelRestClientInterceptor() { }

	@Override
	public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {

		String resourceName = request.getMethod() + ":" + request.getURI();
		Entry entry = null;
		try {
			entry = SphU.entry(resourceName);

			ClientHttpResponse response = execution.execute(request, body);

			// 如果返回的是 5xx，明确告诉 Sentinel：这是异常
			if (response.getStatusCode().is5xxServerError()) {
				Tracer.traceEntry(
						new RuntimeException("Server error: " + response.getStatusCode().value()),
						entry
				);
			}

			return response;
		}
		catch (BlockException ex) {
			// 被 Sentinel 拦截（限流/熔断）
			return new ClientHttpResponse() {
				@Override
				public org.springframework.http.HttpStatusCode getStatusCode() {
					return HttpStatus.TOO_MANY_REQUESTS; // 429
				}

				@Override
				public String getStatusText() {
					return "Blocked by Sentinel: " + ex.getClass().getSimpleName();
				}

				@Override
				public void close() { }

				@Override
				public InputStream getBody() {
					return new ByteArrayInputStream(
							("Blocked by Sentinel: " + ex.getClass().getSimpleName())
									.getBytes(StandardCharsets.UTF_8));
				}

				@Override
				public HttpHeaders getHeaders() {
					HttpHeaders headers = new HttpHeaders();
					headers.setContentType(MediaType.TEXT_PLAIN);
					return headers;
				}
			};
		}
		catch (IOException | RuntimeException ex) {
			// 发生异常时上报给 Sentinel
			Tracer.traceEntry(ex, entry);
			throw ex;
		}
		finally {
			if (entry != null) {
				entry.exit();
			}
		}
	}
}
