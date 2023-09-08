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

package com.alibaba.cloud.routing.zuul.constants;

/**
 * @author yuluo
 * @author 1481556636@qq.com
 */

public final class RoutingZuulConstants {

	private RoutingZuulConstants() {
	}

	/**
	 * Service governance traffic stains the public prefix.
	 */
	public static final String ZUUL_PROPERTY_PREFIX = "spring.cloud.governance.routing.zuul";

	/**
	 * zuul filter order.
	 */
	public static final String ZUUL_ROUTE_FILTER_ORDER = ZUUL_PROPERTY_PREFIX
			+ ".filter.order";

	/**
	 * Zuul header priority.
	 */
	public static final String ZUUL_HEADER_PRIORITY = ZUUL_PROPERTY_PREFIX
			+ ".header.priority";

	/**
	 * Zuul filter order value.
	 */
	public static final int ZUUL_ROUTE_FILTER_ORDER_VALUE = 0;

}
