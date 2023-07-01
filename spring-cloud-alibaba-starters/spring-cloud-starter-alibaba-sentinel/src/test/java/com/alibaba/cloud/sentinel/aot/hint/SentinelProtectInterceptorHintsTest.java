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

package com.alibaba.cloud.sentinel.aot.hint;

import java.lang.reflect.Constructor;

import com.alibaba.cloud.sentinel.annotation.SentinelRestTemplate;
import com.alibaba.cloud.sentinel.custom.SentinelProtectInterceptor;
import org.junit.jupiter.api.Test;

import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.predicate.RuntimeHintsPredicates;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author ruansheneg
 */
public class SentinelProtectInterceptorHintsTest {

	@Test
	public void shouldRegisterHints() {
		Constructor<SentinelProtectInterceptor> constructor;
		try {
			constructor = SentinelProtectInterceptor.class.getConstructor(SentinelRestTemplate.class, RestTemplate.class);
		}
		catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		}
		RuntimeHints hints = new RuntimeHints();
		new SentinelProtectInterceptorHints().registerHints(hints, getClass().getClassLoader());
		assertThat(RuntimeHintsPredicates.reflection().onConstructor(constructor)).accepts(hints);
	}

}

