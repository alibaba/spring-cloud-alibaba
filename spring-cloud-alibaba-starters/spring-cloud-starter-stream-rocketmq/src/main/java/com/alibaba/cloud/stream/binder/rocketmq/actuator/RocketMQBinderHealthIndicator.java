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

package com.alibaba.cloud.stream.binder.rocketmq.actuator;

import com.alibaba.cloud.stream.binder.rocketmq.metrics.Instrumentation;
import com.alibaba.cloud.stream.binder.rocketmq.metrics.InstrumentationManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;

/**
 * @author Timur Valiev
 * @author <a href="mailto:fangjian0423@gmail.com">Jim</a>
 */
public class RocketMQBinderHealthIndicator extends AbstractHealthIndicator {

	@Autowired
	private InstrumentationManager instrumentationManager;

	@Override
	protected void doHealthCheck(Health.Builder builder) throws Exception {
		if (instrumentationManager.getHealthInstrumentations().stream()
				.allMatch(Instrumentation::isUp)) {
			builder.up();
			return;
		}
		if (instrumentationManager.getHealthInstrumentations().stream()
				.allMatch(Instrumentation::isOutOfService)) {
			builder.outOfService();
			return;
		}
		builder.down();
		instrumentationManager.getHealthInstrumentations().stream()
				.filter(instrumentation -> !instrumentation.isStarted())
				.forEach(instrumentation1 -> builder
						.withException(instrumentation1.getStartException()));
	}

}
