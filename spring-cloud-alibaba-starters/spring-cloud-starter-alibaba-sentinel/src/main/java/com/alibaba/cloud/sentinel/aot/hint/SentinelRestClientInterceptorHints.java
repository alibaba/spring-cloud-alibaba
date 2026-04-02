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

package com.alibaba.cloud.sentinel.aot.hint;

import java.lang.reflect.Constructor;

import com.alibaba.cloud.sentinel.annotation.SentinelRestClient;
import com.alibaba.cloud.sentinel.restclient.SentinelRestClientInterceptor;

import org.springframework.aot.hint.ExecutableMode;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;

/**
 * AOT hints for {@link SentinelRestClientInterceptor}.
 *
 * @author uuuyuqi
 */
public class SentinelRestClientInterceptorHints implements RuntimeHintsRegistrar {

	@Override
	public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
		Constructor<SentinelRestClientInterceptor> constructor;
		try {
			constructor = SentinelRestClientInterceptor.class.getConstructor(
					SentinelRestClient.class);
		}
		catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		}
		hints.reflection().registerConstructor(constructor, ExecutableMode.INVOKE);
	}

}
