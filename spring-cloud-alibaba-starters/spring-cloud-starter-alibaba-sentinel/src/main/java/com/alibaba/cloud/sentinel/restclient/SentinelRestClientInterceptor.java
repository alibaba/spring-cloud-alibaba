/*
 * Copyright 2013-present the original author or authors.
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

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;

import com.alibaba.cloud.sentinel.annotation.SentinelRestClient;
import com.alibaba.cloud.sentinel.custom.BlockClassRegistry;
import com.alibaba.cloud.sentinel.rest.SentinelClientHttpResponse;
import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.Tracer;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

/**
 * {@link ClientHttpRequestInterceptor} for integrating Sentinel with Spring's
 * {@link org.springframework.web.client.RestClient}.
 *
 * <p>
 * This interceptor creates two levels of Sentinel resources for each request:
 * <ul>
 * <li><b>Host-level resource</b>: {@code METHOD:scheme://host[:port]},
 * e.g. {@code GET:https://httpbin.org}</li>
 * <li><b>Path-level resource</b>: {@code METHOD:scheme://host[:port]/path},
 * e.g. {@code GET:https://httpbin.org/get}</li>
 * </ul>
 *
 * <p>
 * Supports optional customizations via {@link SentinelRestClient} annotation:
 * <ul>
 * <li>{@code urlCleaner}: a static method to clean/normalize the path-level
 * resource name (e.g. convert {@code /users/123} to {@code /users/{id}})</li>
 * <li>{@code blockHandler}: a static method for handling flow-control blocking
 * (returns a fallback response)</li>
 * <li>{@code fallback}: a static method for handling circuit-breaking (degrade)
 * blocking</li>
 * </ul>
 *
 * @author QHT, uuuyuqi
 * @see SentinelRestClient
 * @see BlockClassRegistry
 */
public class SentinelRestClientInterceptor implements ClientHttpRequestInterceptor {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(SentinelRestClientInterceptor.class);

	private final SentinelRestClient sentinelRestClient;

	public SentinelRestClientInterceptor(SentinelRestClient sentinelRestClient) {
		this.sentinelRestClient = sentinelRestClient;
	}

	@Override
	public ClientHttpResponse intercept(HttpRequest request, byte[] body,
			ClientHttpRequestExecution execution) throws IOException {

		URI uri = request.getURI();
		String hostResource = request.getMethod().toString() + ":" + uri.getScheme()
				+ "://" + uri.getHost()
				+ (uri.getPort() == -1 ? "" : ":" + uri.getPort());
		String hostWithPathResource = hostResource + uri.getPath();

		boolean entryWithPath = !hostResource.equals(hostWithPathResource);

		// Apply URL cleaner if configured
		Method urlCleanerMethod = BlockClassRegistry.lookupUrlCleaner(
				sentinelRestClient.urlCleanerClass(),
				sentinelRestClient.urlCleaner());
		if (urlCleanerMethod != null) {
			hostWithPathResource = (String) methodInvoke(urlCleanerMethod,
					hostWithPathResource);
		}

		Entry hostEntry = null;
		Entry hostWithPathEntry = null;
		try {
			hostEntry = SphU.entry(hostResource, EntryType.OUT);
			if (entryWithPath) {
				hostWithPathEntry = SphU.entry(hostWithPathResource, EntryType.OUT);
			}

			ClientHttpResponse response = execution.execute(request, body);

			// Report 5xx server errors to Sentinel as exceptions
			// Use Tracer.trace() to trace to the current entry context (path-level entry)
			// so that degrade rules configured for path-level resources can work correctly
			if (response.getStatusCode().is5xxServerError()) {
				RuntimeException ex = new RuntimeException("Server error: "
						+ response.getStatusCode().value());
				Tracer.trace(ex);
				LOGGER.debug("Traced 5xx error for resource: {}, exception: {}",
						hostWithPathResource, ex.getMessage());
			}

			return response;
		}
		catch (BlockException ex) {
			LOGGER.debug("RestClient request blocked by Sentinel: resource={}, rule={}",
					entryWithPath ? hostWithPathResource : hostResource,
					ex.getClass().getSimpleName());
			return handleBlockException(request, body, execution, ex);
		}
		catch (IOException | RuntimeException ex) {
			Tracer.traceEntry(ex, hostEntry);
			throw ex;
		}
		finally {
			if (hostWithPathEntry != null) {
				hostWithPathEntry.exit();
			}
			if (hostEntry != null) {
				hostEntry.exit();
			}
		}
	}

	private ClientHttpResponse handleBlockException(HttpRequest request, byte[] body,
			ClientHttpRequestExecution execution, BlockException ex) {
		Object[] args = new Object[] { request, body, execution, ex };
		// Degrade (circuit breaking) → use fallback if configured
		if (ex instanceof DegradeException) {
			Method fallbackMethod = BlockClassRegistry.lookupFallback(
					sentinelRestClient.fallbackClass(),
					sentinelRestClient.fallback());
			if (fallbackMethod != null) {
				return (ClientHttpResponse) methodInvoke(fallbackMethod, args);
			}
			else {
				return new SentinelClientHttpResponse("RestClient request block by sentinel");
			}
		}
		else {
			// Flow control → use blockHandler if configured
			Method blockHandlerMethod = BlockClassRegistry.lookupBlockHandler(
					sentinelRestClient.blockHandlerClass(),
					sentinelRestClient.blockHandler());
			if (blockHandlerMethod != null) {
				return (ClientHttpResponse) methodInvoke(blockHandlerMethod, args);
			}
			else {
				return new SentinelClientHttpResponse("RestClient request block by sentinel");
			}
		}
	}

	private Object methodInvoke(Method method, Object... args) {
		try {
			return method.invoke(null, args);
		}
		catch (IllegalAccessException | InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}

}
