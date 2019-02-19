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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.cloud.stream.binder.rocketmq.metrics.Instrumentation;
import org.springframework.cloud.stream.binder.rocketmq.metrics.InstrumentationManager;

/**
 * @author Timur Valiev
 * @author <a href="mailto:fangjian0423@gmail.com">Jim</a>
 */
public class RocketMQBinderHealthIndicator extends AbstractHealthIndicator {

	@Autowired
	private InstrumentationManager instrumentationManager;

	@Override
	protected void doHealthCheck(Health.Builder builder) throws Exception {
		int upCount = 0, outOfServiceCount = 0;
		for (Instrumentation instrumentation : instrumentationManager
				.getHealthInstrumentations()) {
			if (instrumentation.isUp()) {
				upCount++;
			}
			else if (instrumentation.isOutOfService()) {
				upCount++;
			}
		}
		if (upCount == instrumentationManager.getHealthInstrumentations().size()) {
			builder.up();
			return;
		}
		else if (outOfServiceCount == instrumentationManager.getHealthInstrumentations()
				.size()) {
			builder.outOfService();
			return;
		}
		builder.down();

		for (Instrumentation instrumentation : instrumentationManager
				.getHealthInstrumentations()) {
			if (!instrumentation.isStarted()) {
				builder.withException(instrumentation.getStartException());
			}
		}
	}
}
