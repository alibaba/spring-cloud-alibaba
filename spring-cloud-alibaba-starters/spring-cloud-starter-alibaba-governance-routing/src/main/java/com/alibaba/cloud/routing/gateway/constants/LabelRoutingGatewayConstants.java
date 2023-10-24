/*
 * Copyright 2022-2023 the original author or authors.
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

package com.alibaba.cloud.routing.gateway.constants;

/**
 * @author yuluo
 * @author <a href="1481556636@qq.com"></a>
 */

public final class LabelRoutingGatewayConstants {

	private LabelRoutingGatewayConstants() {
	}

	/**
	 * Service governance traffic stains the public prefix.
	 */
	public static final String GATEWAY_PROPERTY_PREFIX = "spring.cloud.governance.routing.gateway";

	/**
	 * Strategy gateway route filter order.
	 */
	public static final String GATEWAY_ROUTE_FILTER_ORDER = GATEWAY_PROPERTY_PREFIX
			+ ".filter.order";

	/**
	 * Filter order number.
	 */
	public static final int GATEWAY_ROUTE_FILTER_ORDER_VALUE = 10000;

	/**
	 * Whether strategy gateway header priority is enabled.
	 */
	public static final String GATEWAY_HEADER_PRIORITY = GATEWAY_PROPERTY_PREFIX
			+ ".header.priority";

}
