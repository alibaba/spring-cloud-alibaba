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
 * @author 1481556636@qq.com
 */

public final class RoutingConstants {

	private RoutingConstants() {
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
	 * Unknown.
	 */
	public static final String UNKNOWN = "unknown";

	/**
	 * Ignored.
	 */
	public static final String IGNORED = "ignored";

	/**
	 * Zone.
	 */
	public static final String ZONE = "zone";

	/**
	 * Region.
	 */
	public static final String REGION = "region";

	/**
	 * SEPARATE.
	 */
	public static final String SEPARATE = ";";

	/**
	 * Region staining labels.
	 */
	public static final String TRAFFIC_REGION = "X-traffic-" + REGION;

	/**
	 * Availability zone staining labels.
	 */
	public static final String TRAFFIC_ZONE = "X-traffic-" + ZONE;

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
	public static final String FEIGN_CORE_HEADER_TRANSMISSION_ENABLED = PROPERTY_PREFIX
			+ ".feign.core.header.transmission.enabled";

	/**
	 * The rest request header is passed at the beginning.
	 */
	public static final String REST_CORE_HEADER_TRANSMISSION_ENABLED = PROPERTY_PREFIX
			+ ".rest.template.core.header.transmission.enabled";

	/**
	 * The reactive request header is passed at the beginning.
	 */
	public static final String WEB_CLIENT_CORE_HEADER_TRANSMISSION_ENABLED = PROPERTY_PREFIX
			+ ".web.client.core.header.transmission.enabled";

	/**
	 * Local Service Availability Zone staining constants.
	 */
	public static final String SCA_ROUTING_SERVICE_ZONE = "sca-routing-service-zone";

	/**
	 * Local Service region staining constants.
	 */
	public static final String SCA_ROUTING_SERVICE_REGION = "sca-routing-service-region";

	/**
	 * Other Service Availability Zone staining constants.
	 */
	public static final String SCA_ROUTING_ZONE = "sca-routing-zone";

	/**
	 * Other Service region staining constants.
	 */
	public static final String SCA_ROUTING_REGION = "sca-routing-region";

	/**
	 * Pass the request header priority constant.
	 */
	public static final String SERVICE_HEADER_PRIORITY = PROPERTY_PREFIX
			+ ".service.header.priority";

}
