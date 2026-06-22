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

package com.alibaba.cloud.sentinel.feign;

import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;

import feign.InvocationHandlerFactory.MethodHandler;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link SentinelInvocationHandler}.
 */
class SentinelInvocationHandlerTests {

	@Test
	void shouldMapInterfaceMethodToFallbackImplementationMethod() throws Exception {
		Method interfaceMethod = DemoClient.class.getMethod("echo", String.class);
		Method fallbackImplementationMethod = DemoFallback.class.getMethod("echo",
				String.class);
		Map<Method, MethodHandler> dispatch = new LinkedHashMap<>();
		dispatch.put(interfaceMethod, argv -> null);

		Map<Method, Method> fallbackMethodMap = SentinelInvocationHandler
				.toFallbackMethod(dispatch, DemoFallback.class);

		assertThat(fallbackMethodMap).containsEntry(interfaceMethod,
				fallbackImplementationMethod);
		assertThat(fallbackMethodMap.get(interfaceMethod).getDeclaringClass())
				.isEqualTo(DemoFallback.class);
	}

	interface DemoClient {

		String echo(String value);

	}

	static class DemoFallback implements DemoClient {

		@Override
		public String echo(String value) {
			return value;
		}

	}

}
