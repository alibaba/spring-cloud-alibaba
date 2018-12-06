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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.alibaba.sentinel.annotation.SentinelRestTemplate;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.ClassUtils;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.SphU;
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
		String hostResource = uri.getScheme() + "://" + uri.getHost() + ":"
				+ (uri.getPort() == -1 ? 80 : uri.getPort());
		String hostWithPathResource = hostResource + uri.getPath();
		Entry hostEntry = null, hostWithPathEntry = null;
		ClientHttpResponse response = null;
		try {
			ContextUtil.enter(hostWithPathResource);
			hostWithPathEntry = SphU.entry(hostWithPathResource);
			hostEntry = SphU.entry(hostResource);
			response = execution.execute(request, body);
		}
		catch (BlockException e) {
			logger.error("RestTemplate block", e);
			try {
				handleBlockException(e);
			}
			catch (Exception ex) {
				logger.error("sentinel handle BlockException error.", e);
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

	private void handleBlockException(BlockException ex) throws Exception {
		Object[] args = new Object[] { ex };
		// handle degrade
		if (isDegradeFailure(ex)) {
			Method method = extractFallbackMethod(sentinelRestTemplate.fallback(),
					sentinelRestTemplate.fallbackClass());
			if (method != null) {
				method.invoke(null, args);
			}
		}
		// handle block
		Method blockHandler = extractBlockHandlerMethod(
				sentinelRestTemplate.blockHandler(),
				sentinelRestTemplate.blockHandlerClass());
		if (blockHandler != null) {
			blockHandler.invoke(null, args);
		}
	}

	private Method extractFallbackMethod(String fallback, Class<?> fallbackClass) {
		if (StringUtil.isBlank(fallback) || fallbackClass == void.class) {
			return null;
		}
		Method cachedMethod = BlockClassRegistry.lookupFallback(fallbackClass, fallback);
		if (cachedMethod == null) {
			cachedMethod = ClassUtils.getStaticMethod(fallbackClass, fallback,
					BlockException.class);
			BlockClassRegistry.updateFallbackFor(fallbackClass, fallback, cachedMethod);
		}
		return cachedMethod;
	}

	private Method extractBlockHandlerMethod(String block, Class<?> blockClass) {
		if (StringUtil.isBlank(block) || blockClass == void.class) {
			return null;
		}
		Method cachedMethod = BlockClassRegistry.lookupBlockHandler(blockClass, block);
		if (cachedMethod == null) {
			cachedMethod = ClassUtils.getStaticMethod(blockClass, block,
					BlockException.class);
			BlockClassRegistry.updateBlockHandlerFor(blockClass, block, cachedMethod);
		}
		return cachedMethod;
	}

	private boolean isDegradeFailure(BlockException ex) {
		return ex instanceof DegradeException;
	}

}
