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

package com.alibaba.cloud.sentinel;

import com.alibaba.cloud.sentinel.feign.SentinelFeignAutoConfiguration;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.web.bind.annotation.RequestMapping;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringJUnitConfig
@SpringBootTest(classes = SentinelFeignFallbackFactoryTests.TestConfig.class,
		properties = "feign.sentinel.enabled=true")
class SentinelFeignFallbackFactoryTests {

	@Autowired
	private InvalidFallbackService invalidFallbackService;

	@BeforeEach
	void setUp() {
		FlowRule rule = new FlowRule();
		rule.setGrade(RuleConstant.FLOW_GRADE_QPS);
		rule.setCount(0);
		rule.setResource("GET:http://invalid-fallback-service/invalid");
		rule.setLimitApp("default");
		rule.setControlBehavior(RuleConstant.CONTROL_BEHAVIOR_DEFAULT);
		rule.setStrategy(RuleConstant.STRATEGY_DIRECT);
		FlowRuleManager.loadRules(singletonList(rule));
	}

	@Test
	void invalidFallbackFactoryReturnTypeShouldFailFast() {
		assertThatThrownBy(() -> invalidFallbackService.invalid())
			.isInstanceOf(IllegalStateException.class)
			.hasMessageContaining("Incompatible fallback instance")
			.hasMessageContaining(InvalidFallbackService.class.getName());
	}

	@Configuration
	@EnableAutoConfiguration
	@EnableFeignClients
	@Import(SentinelFeignAutoConfiguration.class)
	static class TestConfig {

		@Bean
		InvalidFallbackFactory invalidFallbackFactory() {
			return new InvalidFallbackFactory();
		}

	}

	@FeignClient(value = "invalid-fallback-service",
			fallbackFactory = InvalidFallbackFactory.class)
	interface InvalidFallbackService {

		@RequestMapping(path = "invalid")
		String invalid();

	}

	@SuppressWarnings("rawtypes")
	static class InvalidFallbackFactory implements FallbackFactory {

		@Override
		public Object create(Throwable cause) {
			return new Object();
		}

	}

}
