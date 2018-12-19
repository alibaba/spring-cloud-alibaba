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
import java.lang.reflect.Method;
import java.net.URI;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.alibaba.sentinel.annotation.SentinelRestTemplate;
import org.springframework.cloud.alibaba.sentinel.rest.SentinelClientHttpResponse;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.ClassUtils;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.Tracer;
import com.alibaba.csp.sentinel.context.ContextUtil;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeException;
import com.alibaba.csp.sentinel.util.StringUtil;

/**
 * Interceptor using by SentinelRestTemplate
 *
 * @author fangjian
 */
public class SentinelProtectInterceptor implements ClientHttpRequestInterceptor {

	private static final Logger logger = LoggerFactory
			.getLogger(SentinelProtectInterceptor.class);

	private SentinelRestTemplate sentinelRestTemplate;

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
				try {
					return handleBlockException(request, body, execution,
							(BlockException) e);
				}
				catch (Exception ex) {
					if (ex instanceof IllegalStateException) {
						throw (IllegalStateException) ex;
					}
					throw new IllegalStateException(
							"sentinel handle BlockException error: " + ex.getMessage(),
							ex);
				}
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
			ClientHttpRequestExecution execution, BlockException ex) throws Exception {
		Object[] args = new Object[] { request, body, execution, ex };
		// handle degrade
		if (isDegradeFailure(ex)) {
			Method method = extractFallbackMethod(sentinelRestTemplate.fallback(),
					sentinelRestTemplate.fallbackClass());
			if (method != null) {
				return (ClientHttpResponse) method.invoke(null, args);
			}
			else {
				return new SentinelClientHttpResponse();
			}
		}
		// handle block
		Method blockHandler = extractBlockHandlerMethod(
				sentinelRestTemplate.blockHandler(),
				sentinelRestTemplate.blockHandlerClass());
		if (blockHandler != null) {
			return (ClientHttpResponse) blockHandler.invoke(null, args);
		}
		else {
			return new SentinelClientHttpResponse();
		}
	}

	private Method extractFallbackMethod(String fallback, Class<?> fallbackClass) {
		if (StringUtil.isBlank(fallback) || fallbackClass == void.class) {
			return null;
		}
		Method cachedMethod = BlockClassRegistry.lookupFallback(fallbackClass, fallback);
		Class[] args = new Class[] { HttpRequest.class, byte[].class,
				ClientHttpRequestExecution.class, BlockException.class };
		if (cachedMethod == null) {
			cachedMethod = ClassUtils.getStaticMethod(fallbackClass, fallback, args);
			if (cachedMethod != null) {
				if (!ClientHttpResponse.class
						.isAssignableFrom(cachedMethod.getReturnType())) {
					throw new IllegalStateException(String.format(
							"the return type of method [%s] in class [%s] is not ClientHttpResponse in degrade",
							cachedMethod.getName(), fallbackClass.getCanonicalName()));
				}
				BlockClassRegistry.updateFallbackFor(fallbackClass, fallback,
						cachedMethod);
			}
			else {
				throw new IllegalStateException(String.format(
						"Cannot find method [%s] in class [%s] with parameters %s in degrade",
						fallback, fallbackClass.getCanonicalName(), Arrays.asList(args)));
			}
		}
		return cachedMethod;
	}

	private Method extractBlockHandlerMethod(String block, Class<?> blockClass) {
		if (StringUtil.isBlank(block) || blockClass == void.class) {
			return null;
		}
		Method cachedMethod = BlockClassRegistry.lookupBlockHandler(blockClass, block);
		Class[] args = new Class[] { HttpRequest.class, byte[].class,
				ClientHttpRequestExecution.class, BlockException.class };
		if (cachedMethod == null) {
			cachedMethod = ClassUtils.getStaticMethod(blockClass, block, args);
			if (cachedMethod != null) {
				if (!ClientHttpResponse.class
						.isAssignableFrom(cachedMethod.getReturnType())) {
					throw new IllegalStateException(String.format(
							"the return type of method [%s] in class [%s] is not ClientHttpResponse in flow control",
							cachedMethod.getName(), blockClass.getCanonicalName()));
				}
				BlockClassRegistry.updateBlockHandlerFor(blockClass, block, cachedMethod);
			}
			else {
				throw new IllegalStateException(String.format(
						"Cannot find method [%s] in class [%s] with parameters %s in flow control",
						block, blockClass.getCanonicalName(), Arrays.asList(args)));
			}
		}
		return cachedMethod;
	}

	private boolean isDegradeFailure(BlockException ex) {
		return ex instanceof DegradeException;
	}

}
