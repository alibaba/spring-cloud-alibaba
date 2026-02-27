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
import java.net.URI;
import java.util.function.BiFunction;
import java.util.function.Function;

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
 * Supports optional customizations:
 * <ul>
 * <li>{@code urlCleaner}: a function to clean/normalize the path-level resource
 * name
 * (e.g. convert {@code /users/123} to {@code /users/{id}})</li>
 * <li>{@code blockHandler}: a handler for flow-control blocking (returns a
 * fallback response)</li>
 * <li>{@code fallback}: a handler for circuit-breaking (degrade) blocking</li>
 * </ul>
 *
 * @author QHT, uuuyuqi
 * @see SentinelRestClientAutoConfiguration
 */
public class SentinelRestClientInterceptor implements ClientHttpRequestInterceptor {

	private static final Logger log = LoggerFactory.getLogger(SentinelRestClientInterceptor.class);

	private final Function<String, String> urlCleaner;

	private final BiFunction<HttpRequest, BlockException, ClientHttpResponse> blockHandler;

	private final BiFunction<HttpRequest, BlockException, ClientHttpResponse> fallback;

	public SentinelRestClientInterceptor() {
		this(null, null, null);
	}

	public SentinelRestClientInterceptor(
			Function<String, String> urlCleaner,
			BiFunction<HttpRequest, BlockException, ClientHttpResponse> blockHandler,
			BiFunction<HttpRequest, BlockException, ClientHttpResponse> fallback) {
		this.urlCleaner = urlCleaner;
		this.blockHandler = blockHandler;
		this.fallback = fallback;
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
		if (urlCleaner != null) {
			hostWithPathResource = urlCleaner.apply(hostWithPathResource);
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
			if (response.getStatusCode().is5xxServerError()) {
				Tracer.traceEntry(
						new RuntimeException("Server error: " + response.getStatusCode().value()),
						hostEntry);
			}

			return response;
		}
		catch (BlockException ex) {
			log.warn("RestClient request blocked by Sentinel: resource={}, rule={}",
					entryWithPath ? hostWithPathResource : hostResource,
					ex.getClass().getSimpleName());
			return handleBlockException(request, ex);
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

	private ClientHttpResponse handleBlockException(HttpRequest request, BlockException ex) {
		// Degrade (circuit breaking) → use fallback if configured
		if (ex instanceof DegradeException) {
			if (fallback != null) {
				return fallback.apply(request, ex);
			}
		}
		else {
			// Flow control → use blockHandler if configured
			if (blockHandler != null) {
				return blockHandler.apply(request, ex);
			}
		}
		// Default response
		return new SentinelRestClientHttpResponse(
				"Blocked by Sentinel: " + ex.getClass().getSimpleName());
	}

}
