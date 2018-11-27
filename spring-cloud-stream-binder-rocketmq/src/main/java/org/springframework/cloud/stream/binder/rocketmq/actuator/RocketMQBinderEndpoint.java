/*
 * Copyright (C) 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.stream.binder.rocketmq.actuator;

import static org.springframework.cloud.stream.binder.rocketmq.RocketMQBinderConstants.ENDPOINT_ID;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.boot.actuate.endpoint.AbstractEndpoint;

import com.codahale.metrics.MetricRegistry;

/**
 * @author Timur Valiev
 * @author <a href="mailto:fangjian0423@gmail.com">Jim</a>
 */
public class RocketMQBinderEndpoint extends AbstractEndpoint<Map<String, Object>> {

	private MetricRegistry metricRegistry = new MetricRegistry();
	private Map<String, Object> runtime = new ConcurrentHashMap<>();

	public RocketMQBinderEndpoint() {
		super(ENDPOINT_ID);
	}

	@Override
	public Map<String, Object> invoke() {
		Map<String, Object> result = new HashMap<>();
		result.put("metrics", metricRegistry().getMetrics());
		result.put("runtime", runtime());
		return result;
	}

	public MetricRegistry metricRegistry() {
		return metricRegistry;
	}

	public Map<String, Object> runtime() {
		return runtime;
	}

}
