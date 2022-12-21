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

package com.alibaba.cloud.router.util;

import com.netflix.loadbalancer.AvailabilityFilteringRule;
import com.netflix.loadbalancer.BestAvailableRule;
import com.netflix.loadbalancer.ILoadBalancer;
import com.netflix.loadbalancer.RandomRule;
import com.netflix.loadbalancer.RetryRule;
import com.netflix.loadbalancer.RoundRobinRule;
import com.netflix.loadbalancer.Server;
import com.netflix.loadbalancer.WeightedResponseTimeRule;
import com.netflix.loadbalancer.ZoneAvoidanceRule;

/**
 * @author HH
 */
public class LoadBalanceUtil {

	/**
	 * Default polling.
	 */
	public static final String ROUND_ROBIN_RULE = "RoundRobinRule";

	/**
	 * Random choose a instance.
	 */
	public static final String RANDOM_RULE = "RandomRule";

	/**
	 * The weight is allocated according to the response time.
	 */
	public static final String WEIGHTED_RESPONSE_TIME_RULE = "WeightedResponseTimeRule";

	/**
	 * Select the method with the minimum concurrency.
	 */
	public static final String BEST_AVAILABLE_RULE = "BestAvailableRule";

	/**
	 * Retry when fail.
	 */
	public static final String RETRY_RULE = "RetryRule";

	/**
	 * Choose based on performance and availability.
	 */
	public static final String ZONE_AVOIDANCE_RULE = "ZoneAvoidanceRule";

	/**
	 * Filter by Availability.
	 */
	public static final String AVAILABILITY_FILTERING_RULE = "AvailabilityFilteringRule";

	public Server loadBalanceByOrdinaryRule(ILoadBalancer iLoadBalancer, Object key,
			String rule) {
		if (rule == null) {
			rule = ZONE_AVOIDANCE_RULE;
		}
		switch (rule) {
		case ROUND_ROBIN_RULE:
			return new RoundRobinRule().choose(iLoadBalancer, key);
		case RANDOM_RULE:
			return new RandomRule().choose(iLoadBalancer, key);
		case WEIGHTED_RESPONSE_TIME_RULE:
			return new WeightedResponseTimeRule().choose(iLoadBalancer, key);
		case BEST_AVAILABLE_RULE:
			BestAvailableRule bestAvailableRule = new BestAvailableRule();
			bestAvailableRule.setLoadBalancer(iLoadBalancer);
			return bestAvailableRule.choose(key);
		case RETRY_RULE:
			return new RetryRule().choose(iLoadBalancer, key);
		case ZONE_AVOIDANCE_RULE:
			ZoneAvoidanceRule zoneAvoidanceRule = new ZoneAvoidanceRule();
			zoneAvoidanceRule.setLoadBalancer(iLoadBalancer);
			return zoneAvoidanceRule.choose(key);
		case AVAILABILITY_FILTERING_RULE:
			AvailabilityFilteringRule availabilityFilteringRule = new AvailabilityFilteringRule();
			availabilityFilteringRule.setLoadBalancer(iLoadBalancer);
			return availabilityFilteringRule.choose(key);
		default:
			throw new UnsupportedOperationException(
					"unsupported string compare operation");
		}
	}

}
