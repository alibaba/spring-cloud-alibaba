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

package com.alibaba.cloud.sentinel.gateway;

import junit.framework.Assert;
import org.junit.Test;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

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

		Assert.assertEquals("response", properties.getMode());
		Assert.assertEquals("http://example.com", properties.getRedirect());
		Assert.assertEquals("{'message': 'Fallback response'}", properties.getResponseBody());
		Assert.assertEquals(HttpStatus.TOO_EARLY.value(), properties.getResponseStatus().intValue());
		Assert.assertEquals("application/json", properties.getContentType());
	}

	/**
	 * This test method checks the default values of a FallbackProperties object.
	 * It verifies that certain properties are not set (null) and others have default values.
	 */
	@Test
	public void testDefaultValues() {
		FallbackProperties properties = new FallbackProperties();
		Assert.assertNull(properties.getMode());
		Assert.assertNull(properties.getRedirect());
		Assert.assertNull(properties.getResponseBody());
		Assert.assertEquals(HttpStatus.TOO_MANY_REQUESTS.value(), properties.getResponseStatus().intValue());
		Assert.assertEquals(MediaType.APPLICATION_JSON.toString(), properties.getContentType());
	}
}
