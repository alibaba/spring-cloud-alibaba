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

package com.alibaba.cloud.circuitbreaker.sentinel.feign;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import com.alibaba.cloud.circuitbreaker.sentinel.SentinelCircuitBreakerFactory;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import org.springframework.cloud.context.scope.refresh.RefreshScopeRefreshedEvent;
import org.springframework.context.support.GenericApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test for {@link CircuitBreakerRuleChangeListener}.
 */
public class CircuitBreakerRuleChangeListenerTest {

	@AfterEach
	public void tearDown() {
		DegradeRuleManager.loadRules(new ArrayList<>());
	}

	@Test
	public void testRemovedCustomRuleMustDisappearAfterRefresh() {
		SentinelFeignClientProperties properties = new SentinelFeignClientProperties();
		DegradeRule rule = new DegradeRule().setGrade(2).setCount(2)
				.setTimeWindow(10).setMinRequestAmount(1).setStatIntervalMs(30000);
		properties.getRules().put("custom-resource",
				new ArrayList<>(Collections.singletonList(rule)));

		SentinelCircuitBreakerFactory factory = new SentinelCircuitBreakerFactory();
		CircuitBreakerRuleChangeListener.configureCustom(properties, factory);
		factory.create("custom-resource");
		assertThat(DegradeRuleManager.hasConfig("custom-resource")).isTrue();

		try (GenericApplicationContext context = new GenericApplicationContext()) {
			context.getBeanFactory().registerSingleton("properties", properties);
			context.getBeanFactory().registerSingleton("factory", factory);
			context.refresh();

			CircuitBreakerRuleChangeListener listener = new CircuitBreakerRuleChangeListener();
			listener.setApplicationContext(context);
			listener.afterSingletonsInstantiated();

			// Remove the custom rule from properties
			properties.setRules(new HashMap<>());
			listener.onApplicationEvent(new RefreshScopeRefreshedEvent());
			factory.create("custom-resource");

			// Rule should be cleared from DegradeRuleManager
			assertThat(DegradeRuleManager.hasConfig("custom-resource")).isFalse();
		}
	}

}
