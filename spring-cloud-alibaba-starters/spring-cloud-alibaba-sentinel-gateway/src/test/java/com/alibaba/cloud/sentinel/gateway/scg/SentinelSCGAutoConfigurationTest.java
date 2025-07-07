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

package com.alibaba.cloud.sentinel.gateway.scg;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.alibaba.cloud.sentinel.gateway.ConfigConstants;
import com.alibaba.cloud.sentinel.gateway.FallbackProperties;
import com.alibaba.csp.sentinel.adapter.gateway.sc.callback.GatewayCallbackManager;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Mono;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.reactive.result.view.ViewResolver;
import org.springframework.web.server.ServerWebExchange;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SentinelSCGAutoConfigurationTest {

	@Mock
	private ObjectProvider<List<ViewResolver>> viewResolversProvider;

	@Mock
	private ServerCodecConfigurer serverCodecConfigurer;

	@Mock
	private SentinelGatewayProperties gatewayProperties;

	private SentinelSCGAutoConfiguration config;

	@Before
	public void setup() throws Exception {
		MockitoAnnotations.openMocks(this).close();
		config = new SentinelSCGAutoConfiguration(viewResolversProvider, serverCodecConfigurer);
		Field optional = SentinelSCGAutoConfiguration.class.getDeclaredField("blockRequestHandlerOptional");
		optional.setAccessible(true);
		optional.set(config, Optional.empty());
		Field properties = SentinelSCGAutoConfiguration.class.getDeclaredField("gatewayProperties");
		properties.setAccessible(true);
		properties.set(config, this.gatewayProperties);
	}

	/**
	 * Tests the initialization method to ensure that fallback properties are fetched
	 * and the block exception handler and gateway filter are properly configured.
	 */
	@Test
	public void testInit() {
		config.init();
		verify(gatewayProperties).getFallback(); // Check if fallback properties are fetched
		Assert.assertNotNull(config.sentinelGatewayBlockExceptionHandler());
		Assert.assertNotNull(config.sentinelGatewayFilter());
	}

	/**
	 * Tests the initialization method when the fallback mode is set to return a custom response message.
	 * Verifies that the response status, content type, and body match the expected values.
	 */
	@Test
	public void testInitWithFallbackMsgResponse() {
		FallbackProperties fallbackProperties = mock(FallbackProperties.class);
		when(gatewayProperties.getFallback()).thenReturn(fallbackProperties);
		when(fallbackProperties.getMode()).thenReturn(ConfigConstants.FALLBACK_MSG_RESPONSE);
		when(fallbackProperties.getResponseStatus()).thenReturn(200);
		when(fallbackProperties.getContentType()).thenReturn(MediaType.APPLICATION_JSON.toString());
		when(fallbackProperties.getResponseBody()).thenReturn("test");
		config.init();
		Mono<ServerResponse> responseMono = GatewayCallbackManager.getBlockHandler()
				.handleRequest(mock(ServerWebExchange.class), null);
		Assert.assertEquals(200, Objects.requireNonNull(responseMono.block()).statusCode().value());
	}

	/**
	 * Tests the initialization method when the fallback mode is set to redirect to another URL.
	 * Verifies that the response contains the correct redirect location header.
	 */
	@Test
	public void testInitWithFallbackRedirect() {
		FallbackProperties fallbackProperties = mock(FallbackProperties.class);
		when(gatewayProperties.getFallback()).thenReturn(fallbackProperties);
		when(fallbackProperties.getMode()).thenReturn(ConfigConstants.FALLBACK_REDIRECT);
		when(fallbackProperties.getRedirect()).thenReturn("/test");
		config.init();
		Mono<ServerResponse> responseMono = GatewayCallbackManager.getBlockHandler()
				.handleRequest(mock(ServerWebExchange.class), null);
		HttpHeaders headers = Objects.requireNonNull(responseMono.block()).headers();
		List<String> location = headers.get("Location");
		Assert.assertNotNull(location);
		Assert.assertEquals("/test", location.get(0));
	}
}
