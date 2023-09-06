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

package com.alibaba.cloud.stream.binder.rocketmq.metrics;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Timur Valiev
 * @author <a href="mailto:fangjian0423@gmail.com">Jim</a>
 */
public final class InstrumentationManager {

	private InstrumentationManager() {
	}

	private static final Map<Integer, Instrumentation> HEALTH_INSTRUMENTATIONS = new HashMap<>();

	public static Collection<Instrumentation> getHealthInstrumentations() {
		return HEALTH_INSTRUMENTATIONS.values();
	}

	public static void addHealthInstrumentation(Instrumentation instrumentation) {
		if (null != instrumentation) {
			HEALTH_INSTRUMENTATIONS.computeIfPresent(instrumentation.hashCode(),
					(k, v) -> {
						if (instrumentation.getActuator() != null) {
							instrumentation.getActuator().stop();
						}
						throw new IllegalArgumentException(
								"The current actuator exists, please confirm if there is a repeat operation!!!");
					});
		}

	}

}
