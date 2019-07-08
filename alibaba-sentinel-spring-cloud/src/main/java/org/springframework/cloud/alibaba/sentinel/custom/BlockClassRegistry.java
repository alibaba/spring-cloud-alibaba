/*
 * Copyright (C) 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.alibaba.sentinel.custom;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.alibaba.csp.sentinel.util.StringUtil;

/**
 * @author fangjian
 */
final class BlockClassRegistry {

	private static final Map<String, Method> FALLBACK_MAP = new ConcurrentHashMap<>();
	private static final Map<String, Method> BLOCK_HANDLER_MAP = new ConcurrentHashMap<>();

	static Method lookupFallback(Class<?> clazz, String name) {
		return FALLBACK_MAP.get(getKey(clazz, name));
	}

	static Method lookupBlockHandler(Class<?> clazz, String name) {
		return BLOCK_HANDLER_MAP.get(getKey(clazz, name));
	}

	static void updateFallbackFor(Class<?> clazz, String name, Method method) {
		if (clazz == null || StringUtil.isBlank(name)) {
			throw new IllegalArgumentException("Bad argument");
		}
		FALLBACK_MAP.put(getKey(clazz, name), method);
	}

	static void updateBlockHandlerFor(Class<?> clazz, String name, Method method) {
		if (clazz == null || StringUtil.isBlank(name)) {
			throw new IllegalArgumentException("Bad argument");
		}
		BLOCK_HANDLER_MAP.put(getKey(clazz, name), method);
	}

	private static String getKey(Class<?> clazz, String name) {
		return String.format("%s:%s", clazz.getCanonicalName(), name);
	}

}
