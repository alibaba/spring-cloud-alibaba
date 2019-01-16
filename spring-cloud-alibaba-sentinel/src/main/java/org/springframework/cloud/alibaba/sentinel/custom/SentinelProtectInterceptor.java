/*
 * Copyright (C) 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.alibaba.sentinel.custom;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.alibaba.sentinel.annotation.SentinelRestTemplate;
import org.springframework.cloud.alibaba.sentinel.rest.SentinelClientHttpResponse;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.Tracer;
import com.alibaba.csp.sentinel.context.ContextUtil;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeException;

/**
 * Interceptor using by SentinelRestTemplate
 *
 * @author <a href="mailto:fangjian0423@gmail.com">Jim</a>
 */
public class SentinelProtectInterceptor implements ClientHttpRequestInterceptor {

	private static final Logger logger = LoggerFactory
			.getLogger(SentinelProtectInterceptor.class);

	private final SentinelRestTemplate sentinelRestTemplate;

	public SentinelProtectInterceptor(SentinelRestTemplate sentinelRestTemplate) {
		this.sentinelRestTemplate = sentinelRestTemplate;
	}

	@Override
	public ClientHttpResponse intercept(HttpRequest request, byte[] body,
			ClientHttpRequestExecution execution) throws IOException {
		URI uri = request.getURI();
		String hostResource = uri.getScheme() + "://" + uri.getHost()
				+ (uri.getPort() == -1 ? "" : ":" + uri.getPort());
		String hostWithPathResource = hostResource + uri.getPath();
		boolean entryWithPath = true;
		if (hostResource.equals(hostWithPathResource)) {
			entryWithPath = false;
		}
		Entry hostEntry = null, hostWithPathEntry = null;
		ClientHttpResponse response;
		try {
			ContextUtil.enter(hostWithPathResource);
			if (entryWithPath) {
				hostWithPathEntry = SphU.entry(hostWithPathResource);
			}
			hostEntry = SphU.entry(hostResource);
			response = execution.execute(request, body);
		}
		catch (Throwable e) {
			if (!BlockException.isBlockException(e)) {
				Tracer.trace(e);
				throw new IllegalStateException(e);
			}
			else {
				return handleBlockException(request, body, execution, (BlockException) e);
			}
		}
		finally {
			if (hostEntry != null) {
				hostEntry.exit();
			}
			if (hostWithPathEntry != null) {
				hostWithPathEntry.exit();
			}
			ContextUtil.exit();
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
				return methodInvoke(fallbackMethod, args);
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
			return methodInvoke(blockHandler, args);
		}
		else {
			return new SentinelClientHttpResponse();
		}
	}

	private ClientHttpResponse methodInvoke(Method method, Object... args) {
		try {
			return (ClientHttpResponse) method.invoke(null, args);
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
