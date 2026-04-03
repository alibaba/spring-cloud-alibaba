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

import static org.assertj.core.api.Assertions.assertThat;

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

		assertThat(properties.getMode()).isEqualTo("response");
		assertThat(properties.getRedirect()).isEqualTo("http://example.com");
		assertThat(properties.getResponseBody())
				.isEqualTo("{'message': 'Fallback response'}");
		assertThat(properties.getResponseStatus().intValue())
				.isEqualTo(HttpStatus.TOO_EARLY.value());
		assertThat(properties.getContentType()).isEqualTo("application/json");
	}

	/**
	 * This test method checks the default values of a FallbackProperties object.
	 * It verifies that certain properties are not set (null) and others have default values.
	 */
	@Test
	public void testDefaultValues() {
		FallbackProperties properties = new FallbackProperties();
		assertThat(properties.getMode()).isNull();
		assertThat(properties.getRedirect()).isNull();
		assertThat(properties.getResponseBody()).isNull();
		assertThat(properties.getResponseStatus().intValue())
				.isEqualTo(HttpStatus.TOO_MANY_REQUESTS.value());
		assertThat(properties.getContentType())
				.isEqualTo(MediaType.APPLICATION_JSON.toString());
	}
}
