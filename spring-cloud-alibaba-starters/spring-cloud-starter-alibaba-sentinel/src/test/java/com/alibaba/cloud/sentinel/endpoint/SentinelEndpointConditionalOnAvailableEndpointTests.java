/*
 * Copyright 2013-2020 the original author or authors.
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

package com.alibaba.cloud.sentinel.endpoint;

import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test cases for {@link SentinelEndpointAutoConfiguration}.
 *
 * @author lengleng
 */
public class SentinelEndpointConditionalOnAvailableEndpointTests {

	private ApplicationContextRunner contextRunner;

	@Before
	public void setUp() {
		contextRunner = new ApplicationContextRunner()
				.withUserConfiguration(SentinelEndpointAutoConfiguration.class);
	}

	@Test
	public void testSentinelEndpointEnabled() {
		this.contextRunner
				.withPropertyValues("management.endpoints.web.exposure.include=*",
						"management.endpoint.sentinel.enabled=true")
				.run((context) -> assertThat(context).hasBean("sentinelEndPoint"));
	}

	@Test
	public void testSentinelEndpointNotEnabled() {
		this.contextRunner
				.withPropertyValues("management.endpoints.web.exposure.include=*",
						"management.endpoint.sentinel.enabled=false")
				.run((context) -> assertThat(context)
						.doesNotHaveBean("sentinelEndPoint"));
	}

}
