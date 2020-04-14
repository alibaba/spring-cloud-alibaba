/*
 * Copyright 2013-2018 the original author or authors.
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

package com.alibaba.cloud.sentinel.custom;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;

import com.alibaba.cloud.sentinel.annotation.SentinelRestTemplate;
import com.alibaba.cloud.sentinel.rest.SentinelClientHttpResponse;
import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.Tracer;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeException;

import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.RestTemplate;

/**
 * Interceptor using by SentinelRestTemplate.
 *
 * @author <a href="mailto:fangjian0423@gmail.com">Jim</a>
 */
public class SentinelProtectInterceptor implements ClientHttpRequestInterceptor {

	private final SentinelRestTemplate sentinelRestTemplate;

	private final RestTemplate restTemplate;

	public SentinelProtectInterceptor(SentinelRestTemplate sentinelRestTemplate,
			RestTemplate restTemplate) {
		this.sentinelRestTemplate = sentinelRestTemplate;
		this.restTemplate = restTemplate;
	}

	@Override
	public ClientHttpResponse intercept(HttpRequest request, byte[] body,
			ClientHttpRequestExecution execution) throws IOException {
		URI uri = request.getURI();
		String hostResource = request.getMethod().toString() + ":" + uri.getScheme()
				+ "://" + uri.getHost()
				+ (uri.getPort() == -1 ? "" : ":" + uri.getPort());
		String hostWithPathResource = hostResource + uri.getPath();
		boolean entryWithPath = true;
		if (hostResource.equals(hostWithPathResource)) {
			entryWithPath = false;
		}
		Method urlCleanerMethod = BlockClassRegistry.lookupUrlCleaner(
				sentinelRestTemplate.urlCleanerClass(),
				sentinelRestTemplate.urlCleaner());
		if (urlCleanerMethod != null) {
			hostWithPathResource = (String) methodInvoke(urlCleanerMethod,
					hostWithPathResource);
		}

		Entry hostEntry = null;
		Entry hostWithPathEntry = null;
		ClientHttpResponse response;
		try {
			hostEntry = SphU.entry(hostResource, EntryType.OUT);
			if (entryWithPath) {
				hostWithPathEntry = SphU.entry(hostWithPathResource, EntryType.OUT);
			}
			response = execution.execute(request, body);
			if (this.restTemplate.getErrorHandler().hasError(response)) {
				if (entryWithPath) {
					Tracer.traceEntry(
							new IllegalStateException(
									"RestTemplate ErrorHandler has error"),
							hostWithPathEntry);
				}
				Tracer.traceEntry(
						new IllegalStateException("RestTemplate ErrorHandler has error"),
						hostEntry);
			}
		}
		catch (Throwable e) {
			if (!BlockException.isBlockException(e)) {
				if (entryWithPath) {
					Tracer.traceEntry(e, hostWithPathEntry);
				}
				Tracer.traceEntry(e, hostEntry);
				throw (IOException) e;
			}
			else {
				return handleBlockException(request, body, execution, (BlockException) e);
			}
		}
		finally {
			if (hostWithPathEntry != null) {
				hostWithPathEntry.exit();
			}
			if (hostEntry != null) {
				hostEntry.exit();
			}
		}
		return response;
	}

	private ClientHttpResponse handleBlockException(HttpRequest request, byte[] body,
			ClientHttpRequestExecution execution, BlockException ex) {
		Object[] args = new Object[] { request, body, execution, ex };
		// handle degrade
		if (isDegradeFailure(ex)) {
			Method fallbackMethod = extractFallbackMethod(sentinelRestTemplate.fallback(),
					sentinelRestTemplate.fallbackClass());
			if (fallbackMethod != null) {
				return (ClientHttpResponse) methodInvoke(fallbackMethod, args);
			}
			else {
				return new SentinelClientHttpResponse();
			}
		}
		// handle flow
		Method blockHandler = extractBlockHandlerMethod(
				sentinelRestTemplate.blockHandler(),
				sentinelRestTemplate.blockHandlerClass());
		if (blockHandler != null) {
			return (ClientHttpResponse) methodInvoke(blockHandler, args);
		}
		else {
			return new SentinelClientHttpResponse();
		}
	}

	private Object methodInvoke(Method method, Object... args) {
		try {
			return method.invoke(null, args);
		}
		catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
		catch (InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}

	private Method extractFallbackMethod(String fallback, Class<?> fallbackClass) {
		return BlockClassRegistry.lookupFallback(fallbackClass, fallback);
	}

	private Method extractBlockHandlerMethod(String block, Class<?> blockClass) {
		return BlockClassRegistry.lookupBlockHandler(blockClass, block);
	}

	private boolean isDegradeFailure(BlockException ex) {
		return ex instanceof DegradeException;
	}

}
