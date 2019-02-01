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

package org.springframework.cloud.alibaba.sentinel;

import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.alibaba.sentinel.feign.SentinelFeignAutoConfiguration;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author <a href="mailto:fangjian0423@gmail.com">Jim</a>
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = { SentinelFeignTests.TestConfig.class }, properties = {
		"feign.sentinel.enabled=true" })
public class SentinelFeignTests {

	@Autowired
	private EchoService echoService;

	@Autowired
	private FooService fooService;

	@Autowired
	private BarService barService;

	@Before
	public void setUp() {
		FlowRule rule1 = new FlowRule();
		rule1.setGrade(RuleConstant.FLOW_GRADE_QPS);
		rule1.setCount(0);
		rule1.setResource("GET:http://test-service/echo/{str}");
		rule1.setLimitApp("default");
		rule1.setControlBehavior(RuleConstant.CONTROL_BEHAVIOR_DEFAULT);
		rule1.setStrategy(RuleConstant.STRATEGY_DIRECT);
		FlowRule rule2 = new FlowRule();
		rule2.setGrade(RuleConstant.FLOW_GRADE_QPS);
		rule2.setCount(0);
		rule2.setResource("GET:http://foo-service/echo/{str}");
		rule2.setLimitApp("default");
		rule2.setControlBehavior(RuleConstant.CONTROL_BEHAVIOR_DEFAULT);
		rule2.setStrategy(RuleConstant.STRATEGY_DIRECT);
		FlowRule rule3 = new FlowRule();
		rule3.setGrade(RuleConstant.FLOW_GRADE_QPS);
		rule3.setCount(0);
		rule3.setResource("GET:http://bar-service/bar");
		rule3.setLimitApp("default");
		rule3.setControlBehavior(RuleConstant.CONTROL_BEHAVIOR_DEFAULT);
		rule3.setStrategy(RuleConstant.STRATEGY_DIRECT);
		FlowRuleManager.loadRules(Arrays.asList(rule1, rule2, rule3));
	}

	@Test
	public void contextLoads() throws Exception {
		assertNotNull("EchoService was not created", echoService);
		assertNotNull("FooService was not created", fooService);
	}

	@Test
	public void testFeignClient() {
		assertEquals("Sentinel Feign Client fallback success", "echo fallback",
				echoService.echo("test"));
		assertEquals("Sentinel Feign Client fallbackFactory success", "foo fallback",
				fooService.echo("test"));
		assertThatExceptionOfType(Exception.class).isThrownBy(() -> {
			barService.bar();
		});

		assertNotEquals("ToString method invoke was not in SentinelInvocationHandler",
				echoService.toString(), fooService.toString());
		assertNotEquals("HashCode method invoke was not in SentinelInvocationHandler",
				echoService.hashCode(), fooService.hashCode());
		assertFalse("Equals method invoke was not in SentinelInvocationHandler",
				echoService.equals(fooService));
	}

	@Configuration
	@EnableAutoConfiguration
	@ImportAutoConfiguration({ SentinelFeignAutoConfiguration.class })
	@EnableFeignClients
	public static class TestConfig {

		@Bean
		public EchoServiceFallback echoServiceFallback() {
			return new EchoServiceFallback();
		}

		@Bean
		public CustomFallbackFactory customFallbackFactory() {
			return new CustomFallbackFactory();
		}

	}

	@FeignClient(value = "test-service", fallback = EchoServiceFallback.class)
	public interface EchoService {
		@RequestMapping(path = "echo/{str}")
		String echo(@RequestParam("str") String param);
	}

	@FeignClient(value = "foo-service", fallbackFactory = CustomFallbackFactory.class)
	public interface FooService {
		@RequestMapping(path = "echo/{str}")
		String echo(@RequestParam("str") String param);
	}

	@FeignClient(value = "bar-service")
	public interface BarService {
		@RequestMapping(path = "bar")
		String bar();
	}

	public static class EchoServiceFallback implements EchoService {

		@Override
		public String echo(@RequestParam("str") String param) {
			return "echo fallback";
		}

	}

	public static class FooServiceFallback implements FooService {

		@Override
		public String echo(@RequestParam("str") String param) {
			return "foo fallback";
		}
	}

	public static class CustomFallbackFactory
			implements feign.hystrix.FallbackFactory<FooService> {

		private FooService fooService = new FooServiceFallback();

		@Override
		public FooService create(Throwable throwable) {
			return fooService;
		}
	}

}
