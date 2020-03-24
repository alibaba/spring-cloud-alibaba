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

package com.alibaba.cloud.sentinel.gateway;

import com.alibaba.cloud.sentinel.gateway.scg.SentinelGatewayProperties;
import com.alibaba.cloud.sentinel.gateway.zuul.SentinelZuulProperties;

/**
 * @author <a href="mailto:fangjian0423@gmail.com">Jim</a>
 */
public final class ConfigConstants {

	/**
	 * Netflix Zuul type.
	 */
	public static final String APP_TYPE_ZUUL_GATEWAY = "12";

	/**
	 * Spring Cloud Gateway type.
	 */
	public static final String APP_TYPE_SCG_GATEWAY = "11";

	/**
	 * ConfigurationProperties for {@link SentinelZuulProperties}.
	 */
	public static final String ZUUl_PREFIX = "spring.cloud.sentinel.zuul";

	/**
	 * ConfigurationProperties for {@link SentinelGatewayProperties}.
	 */
	public static final String GATEWAY_PREFIX = "spring.cloud.sentinel.scg";

	/**
	 * Response type for Spring Cloud Gateway fallback.
	 */
	public static final String FALLBACK_MSG_RESPONSE = "response";

	/**
	 * Redirect type for Spring Cloud Gateway fallback.
	 */
	public static final String FALLBACK_REDIRECT = "redirect";

	private ConfigConstants() {
		throw new AssertionError("Must not instantiate constant utility class");
	}

}
