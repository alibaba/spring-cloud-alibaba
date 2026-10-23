/*
 * Copyright 2013-present the original author or authors.
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

package com.alibaba.cloud.circuitbreaker.sentinel;

import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;

/**
 * Utilities for Sentinel rules.
 *
 * @author yushiwuzhengjianyuzi
 */
final class DegradeRuleUtils {

	private DegradeRuleUtils() {
	}

	static DegradeRule copyForResource(DegradeRule source, String resourceName) {
		DegradeRule copy = new DegradeRule();
		copy.setId(source.getId());
		copy.setResource(resourceName);
		copy.setLimitApp(source.getLimitApp());
		copy.setGrade(source.getGrade());
		copy.setCount(source.getCount());
		copy.setTimeWindow(source.getTimeWindow());
		copy.setMinRequestAmount(source.getMinRequestAmount());
		copy.setSlowRatioThreshold(source.getSlowRatioThreshold());
		copy.setStatIntervalMs(source.getStatIntervalMs());
		return copy;
	}

}
