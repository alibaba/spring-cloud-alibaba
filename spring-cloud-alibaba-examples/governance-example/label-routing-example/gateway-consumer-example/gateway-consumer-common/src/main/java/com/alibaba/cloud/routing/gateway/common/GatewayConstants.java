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

package com.alibaba.cloud.routing.gateway.common;

/**
 * @author yuluo
 * @author <a href="1481556636@qq.com"></a>
 */

public final class GatewayConstants {

	private GatewayConstants() {
	}

	/**
	 * Gateway consumer example name.
	 */
	public static final String GATEWAY_CONSUMER_EXAMPLE = "gateway-consumer-example";

	/**
	 * Zuul consumer example name.
	 */
	public static final String ZUUL_CONSUMER_EXAMPLE = "zuul-consumer-example";

	/**
	 * Service provider name.
	 */
	public static final String SERVICE_PROVIDER_NAME = "routing-service-provider";

	/**
	 * Access service provider url.
	 */
	public static final String SERVICE_PROVIDER_ADDRESS = "http://"
			+ SERVICE_PROVIDER_NAME;

	/**
	 * Routing rule description.
	 */
	public static final String ADD_RULE_DESCRIPTION = "If the request parameters contain `tag=v2`, the request header contains id and the value is less than 10, and the uri is `/test-a1`, then all traffic will be routed to the v2 version. If one of them is not satisfied, the traffic will be routed to In v1 version.";

	/**
	 * Routing rule description.
	 */
	public static final String UPDATE_RULE_DESCRIPTION = "If the request parameters contain `tag=v2`, the request header contains id and the value is less than 10, and the uri is `/test-a1`, then 50% of the traffic is routed to the v2 version, and the remaining traffic is routed to the v1 version. If any one of them is not satisfied, the traffic will be routed to version v1.";

}
