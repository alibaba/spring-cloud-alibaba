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

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author xiaojing
 */
public class HandlerUtil {

	private static final ConcurrentHashMap<String, SentinelBlockHandler> map = new ConcurrentHashMap<>(
			16);

	/**
	 * you should add your custom handler before use it
	 * @param name see {@link EnableSentinel#handler()}
	 * @param handler you custom handler
	 */
	public static void addHandler(String name, SentinelBlockHandler handler) {
		map.put(name, handler);
	}

	public static SentinelBlockHandler getHandler(String name) {
		SentinelBlockHandler handler = map.get(name);
		if (null == handler) {
			throw new RuntimeException("cannot find handler name=<" + name
					+ ",> did you forgot to invoke HandlerUtil.addHandler(name, handler) ?");
		}
		return handler;
	}
}
