/*
 * Copyright 2024-present the original author or authors.
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

package com.alibaba.cloud.scheduling;

/**
 * @author yaohui
 **/
public final class SchedulingConstants {

	/**
	 * Scheduling config prefix.
	 */
	public static final String SCHEDULING_CONFIG_PREFIX = "spring.cloud.scheduling";

	/**
	 * Scheduling distributed mode.
	 */
	public static final String SCHEDULING_CONFIG_DISTRIBUTED_MODE_KEY = SCHEDULING_CONFIG_PREFIX + ".distributed-mode";

	private SchedulingConstants() {
		throw new AssertionError("Must not instantiate constant utility class");
	}

}
