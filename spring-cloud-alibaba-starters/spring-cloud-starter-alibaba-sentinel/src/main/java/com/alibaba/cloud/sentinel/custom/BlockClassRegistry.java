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

package com.alibaba.cloud.sentinel.custom;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.alibaba.csp.sentinel.util.StringUtil;

/**
 * Registry for block handler, fallback and url cleaner methods.
 *
 * @author fangjian
 */
public final class BlockClassRegistry {

	private BlockClassRegistry() {

	}

	private static final Map<String, Method> FALLBACK_MAP = new ConcurrentHashMap<>();

	private static final Map<String, Method> BLOCK_HANDLER_MAP = new ConcurrentHashMap<>();

	private static final Map<String, Method> URL_CLEANER_MAP = new ConcurrentHashMap<>();

	/**
	 * Lookup fallback method.
	 * @param clazz the class containing the fallback method
	 * @param name the method name
	 * @return the fallback method, or null if not found
	 */
	public static Method lookupFallback(Class<?> clazz, String name) {
		return FALLBACK_MAP.get(getKey(clazz, name));
	}

	/**
	 * Lookup block handler method.
	 * @param clazz the class containing the block handler method
	 * @param name the method name
	 * @return the block handler method, or null if not found
	 */
	public static Method lookupBlockHandler(Class<?> clazz, String name) {
		return BLOCK_HANDLER_MAP.get(getKey(clazz, name));
	}

	/**
	 * Lookup url cleaner method.
	 * @param clazz the class containing the url cleaner method
	 * @param name the method name
	 * @return the url cleaner method, or null if not found
	 */
	public static Method lookupUrlCleaner(Class<?> clazz, String name) {
		return URL_CLEANER_MAP.get(getKey(clazz, name));
	}

	/**
	 * Register fallback method.
	 * @param clazz the class containing the fallback method
	 * @param name the method name
	 * @param method the fallback method
	 */
	public static void updateFallbackFor(Class<?> clazz, String name, Method method) {
		if (clazz == null || StringUtil.isBlank(name)) {
			throw new IllegalArgumentException("Bad argument");
		}
		FALLBACK_MAP.put(getKey(clazz, name), method);
	}

	/**
	 * Register block handler method.
	 * @param clazz the class containing the block handler method
	 * @param name the method name
	 * @param method the block handler method
	 */
	public static void updateBlockHandlerFor(Class<?> clazz, String name, Method method) {
		if (clazz == null || StringUtil.isBlank(name)) {
			throw new IllegalArgumentException("Bad argument");
		}
		BLOCK_HANDLER_MAP.put(getKey(clazz, name), method);
	}

	/**
	 * Register url cleaner method.
	 * @param clazz the class containing the url cleaner method
	 * @param name the method name
	 * @param method the url cleaner method
	 */
	public static void updateUrlCleanerFor(Class<?> clazz, String name, Method method) {
		if (clazz == null || StringUtil.isBlank(name)) {
			throw new IllegalArgumentException("Bad argument");
		}
		URL_CLEANER_MAP.put(getKey(clazz, name), method);
	}

	private static String getKey(Class<?> clazz, String name) {
		return String.format("%s:%s", clazz.getCanonicalName(), name);
	}

}
