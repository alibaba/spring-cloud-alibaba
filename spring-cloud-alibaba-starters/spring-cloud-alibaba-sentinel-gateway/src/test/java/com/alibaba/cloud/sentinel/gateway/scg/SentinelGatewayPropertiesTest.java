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

package com.alibaba.cloud.sentinel.gateway.scg;

import com.alibaba.cloud.sentinel.gateway.FallbackProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.core.Ordered;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

public class SentinelGatewayPropertiesTest {

	private SentinelGatewayProperties properties;

	@BeforeEach
	public void setUp() {
		properties = new SentinelGatewayProperties();
	}

	@Test
	public void testDefaultOrder() {
		assertEquals(Ordered.HIGHEST_PRECEDENCE, properties.getOrder().intValue());
	}

	@Test
	public void testSetOrder() {
		int newOrder = 100;
		properties.setOrder(newOrder);
		assertEquals(newOrder, properties.getOrder().intValue());
	}

	@Test
	public void testFallbackPropertiesInitialization() {
		assertNull(properties.getFallback());
	}

	@Test
	public void testSetFallbackProperties() {
		FallbackProperties newFallback = new FallbackProperties();
		properties.setFallback(newFallback);
		assertSame(newFallback, properties.getFallback());
	}
}
