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

package com.alibaba.cloud.stream.binder.rocketmq.metrics;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author Timur Valiev
 * @author <a href="mailto:fangjian0423@gmail.com">Jim</a>
 */
public class InstrumentationManager {

	private final Map<String, Object> runtime = new ConcurrentHashMap<>();

	private final Map<String, Instrumentation> healthInstrumentations = new HashMap<>();

	public Set<Instrumentation> getHealthInstrumentations() {
		return healthInstrumentations.entrySet().stream().map(Map.Entry::getValue)
				.collect(Collectors.toSet());
	}

	public void addHealthInstrumentation(Instrumentation instrumentation) {
		healthInstrumentations.put(instrumentation.getName(), instrumentation);
	}

	public Instrumentation getHealthInstrumentation(String key) {
		return healthInstrumentations.get(key);
	}

	public Map<String, Object> getRuntime() {
		return runtime;
	}

}
