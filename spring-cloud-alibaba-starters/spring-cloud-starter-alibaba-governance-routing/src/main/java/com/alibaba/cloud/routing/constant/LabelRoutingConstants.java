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

package com.alibaba.cloud.routing.constant;

/**
 * @author yuluo
 * @author <a href="1481556636@qq.com"></a>
 */

public final class LabelRoutingConstants {

	private LabelRoutingConstants() {
	}

	/**
	 * Support Parsing Rules from path,only URI at present.
	 */
	public static final String PATH = "path";

	/**
	 * Support Parsing Rules from header.
	 */
	public static final String HEADER = "header";

	/**
	 * Support Parsing Rules from parameter.
	 */
	public static final String PARAMETER = "parameter";

	/**
	 * Filter base on version metadata.
	 */
	public static final String VERSION = "version";

	/**
	 * Default.
	 */
	public static final String DEFAULT = "default";

	/**
	 * Sign of no match any rule.
	 */
	public static final int NO_MATCH = -1;

	/**
	 * Avoid loss of accuracy.
	 */
	public static final double KEEP_ACCURACY = 1.0;

	/**
	 * Listener execution order.
	 */
	public static final int LISTENER_ORDER = 200;

	/**
	 * Zone.
	 */
	public static final String ZONE = "zone";

	/**
	 * Region.
	 */
	public static final String REGION = "region";

	/**
	 * Service governance traffic stains the public prefix.
	 */
	public static final String PROPERTY_PREFIX = "spring.cloud.governance.routing";

	/**
	 * Turn on the zone avoidance rule.
	 */
	public static final String ZONE_AVOIDANCE_RULE_ENABLED = PROPERTY_PREFIX
			+ ".zone.affinity.enabled";

	/**
	 * Local Service Availability Zone staining constants.
	 */
	public static final String SCA_ROUTING_SERVICE_ZONE = "s-l-r-service-zone";

	/**
	 * Local Service region staining constants.
	 */
	public static final String SCA_ROUTING_SERVICE_REGION = "s-l-r-service-region";

	/**
	 * Empty String constants.
	 */
	public static final String EMPTY_STRING = "";

	/**
	 * Web request client constants class.
	 */
	public static class WebClient {

		/**
		 * Turn on the RestTemplate interceptor.
		 */
		public static final String REST_INTERCEPT_ENABLED = PROPERTY_PREFIX
				+ ".rest.intercept.enabled";

		/**
		 * Turn on the Feign interceptor.
		 */
		public static final String FEIGN_INTERCEPT_ENABLED = PROPERTY_PREFIX
				+ ".feign.intercept.enabled";

		/**
		 * Turn on the WebClient interceptor.
		 */
		public static final String REACTIVE_INTERCEPT_ENABLED = PROPERTY_PREFIX
				+ ".reactive.intercept.enabled";

		/**
		 * The feign request header is passed at the beginning.
		 */
		public static final String FEIGN_HEADER_TRANSMISSION_ENABLED = PROPERTY_PREFIX
				+ ".feign.core.header.transmission.enabled";

		/**
		 * The rest request header is passed at the beginning.
		 */
		public static final String REST_HEADER_TRANSMISSION_ENABLED = PROPERTY_PREFIX
				+ ".rest.template.core.header.transmission.enabled";

		/**
		 * The reactive request header is passed at the beginning.
		 */
		public static final String WEB_CLIENT_HEADER_TRANSMISSION_ENABLED = PROPERTY_PREFIX
				+ ".web.client.core.header.transmission.enabled";

	}

	/**
	 * Spring Cloud Gateway constants class.
	 */
	public static class Gateway {

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

	/**
	 * Zuul gateway constants class.
	 */
	public static class Zuul {

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

}
