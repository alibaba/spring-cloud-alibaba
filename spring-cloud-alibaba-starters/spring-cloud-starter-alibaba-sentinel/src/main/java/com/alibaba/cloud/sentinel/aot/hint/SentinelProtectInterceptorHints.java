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

import org.springframework.aot.hint.ExecutableMode;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.web.client.RestTemplate;

/**
 * @author ruansheneg
 */
public class SentinelProtectInterceptorHints implements RuntimeHintsRegistrar {
	@Override
	public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
		Constructor<SentinelProtectInterceptor> constructor;
		try {
			constructor = SentinelProtectInterceptor.class.getConstructor(SentinelRestTemplate.class, RestTemplate.class);
		}
		catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		}
		hints.reflection().registerConstructor(constructor, ExecutableMode.INVOKE);
	}
}
