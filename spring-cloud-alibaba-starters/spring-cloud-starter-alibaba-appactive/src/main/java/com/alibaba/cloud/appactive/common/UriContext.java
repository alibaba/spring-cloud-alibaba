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

package com.alibaba.cloud.appactive.common;

/**
 * @author raozihao, mageekchiu
 * @author <a href="mailto:zihaorao@gmail.com">Steve</a>
 */
public final class UriContext {

	private UriContext() {
	}

	private static final ThreadLocal<String> URI_INFO = new ThreadLocal<String>();

	public static void clearContext() {
		URI_INFO.remove();
	}

	public static String getUriPath() {
		return URI_INFO.get();
	}

	public static void setUriPath(String targetUnit) {
		URI_INFO.set(targetUnit);
	}

}
