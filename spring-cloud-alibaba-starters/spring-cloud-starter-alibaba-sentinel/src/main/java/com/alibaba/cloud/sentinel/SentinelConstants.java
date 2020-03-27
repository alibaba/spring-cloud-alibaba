/*
 * Copyright 2013-2018 the original author or authors.
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

package com.alibaba.cloud.sentinel;

/**
 * @author fangjian
 */
public final class SentinelConstants {

	/**
	 * Prefix of {@link SentinelProperties}.
	 */
	public static final String PROPERTY_PREFIX = "spring.cloud.sentinel";

	/**
	 * Block page key.
	 */
	public static final String BLOCK_PAGE_URL_CONF_KEY = "csp.sentinel.web.servlet.block.page";

	/**
	 * Block type.
	 */
	public static final String BLOCK_TYPE = "block";

	/**
	 * Fallback type.
	 */
	public static final String FALLBACK_TYPE = "fallback";

	/**
	 * UrlCleaner type.
	 */
	public static final String URLCLEANER_TYPE = "urlCleaner";

	/**
	 * The cold factor.
	 */
	public static final String COLD_FACTOR = "3";

	/**
	 * The charset.
	 */
	public static final String CHARSET = "UTF-8";

	/**
	 * The Sentinel api port.
	 */
	public static final String API_PORT = "8719";

	private SentinelConstants() {
		throw new AssertionError("Must not instantiate constant utility class");
	}

}
