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

package com.alibaba.cloud.sentinel.gateway;

import org.junit.jupiter.api.Test;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class FallbackPropertiesTest {

	/**
	 * Tests the correct setting and retrieval of fallback properties.
	 * This test case verifies that the FallbackProperties class correctly sets and retrieves
	 * various properties for fallback responses, including the response mode, redirect URL,
	 * response body content, HTTP status code, and content type.
	 */
	@Test
	public void testFallbackProperties() {
		FallbackProperties properties = new FallbackProperties()
				.setMode("response")
				.setRedirect("http://example.com")
				.setResponseBody("{'message': 'Fallback response'}")
				.setResponseStatus(HttpStatus.TOO_EARLY.value())
				.setContentType("application/json");

		assertEquals("response", properties.getMode());
		assertEquals("http://example.com", properties.getRedirect());
		assertEquals("{'message': 'Fallback response'}", properties.getResponseBody());
		assertEquals(HttpStatus.TOO_EARLY.value(), properties.getResponseStatus().intValue());
		assertEquals("application/json", properties.getContentType());
	}

	/**
	 * This test method checks the default values of a FallbackProperties object.
	 * It verifies that certain properties are not set (null) and others have default values.
	 */
	@Test
	public void testDefaultValues() {
		FallbackProperties properties = new FallbackProperties();
		assertNull(properties.getMode());
		assertNull(properties.getRedirect());
		assertNull(properties.getResponseBody());
		assertEquals(HttpStatus.TOO_MANY_REQUESTS.value(), properties.getResponseStatus().intValue());
		assertEquals(MediaType.APPLICATION_JSON.toString(), properties.getContentType());
	}
}
