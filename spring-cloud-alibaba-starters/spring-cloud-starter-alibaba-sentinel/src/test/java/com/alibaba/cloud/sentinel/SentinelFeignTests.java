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

package com.alibaba.cloud.sentinel;

import java.util.Arrays;

import com.alibaba.cloud.sentinel.feign.SentinelFeignAutoConfiguration;
import com.alibaba.cloud.sentinel.feign.handler.ResourceHandler;
import com.alibaba.cloud.sentinel.feign.handler.ResourceHandlerHolder;
import com.alibaba.cloud.sentinel.feign.handler.SentinelResourceHandlerAutoConfiguration;
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
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author <a href="mailto:fangjian0423@gmail.com">Jim</a>
 */
@RunWith(SpringRunner.class)
@SpringBootTest(
		classes = { SentinelFeignTests.TestConfig.class,
				SentinelResourceHandlerAutoConfiguration.class },
		properties = { "feign.sentinel.enabled=true" })
public class SentinelFeignTests {

	@Autowired
	private EchoService echoService;

	@Autowired
	private FooService fooService;

	@Autowired
	private BarService barService;

	@Autowired
	private BazService bazService;

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
		FlowRule rule4 = new FlowRule();
		rule4.setGrade(RuleConstant.FLOW_GRADE_QPS);
		rule4.setCount(0);
		rule4.setResource("GET:http://baz-service/baz");
		rule4.setLimitApp("default");
		rule4.setControlBehavior(RuleConstant.CONTROL_BEHAVIOR_DEFAULT);
		rule4.setStrategy(RuleConstant.STRATEGY_DIRECT);
		FlowRuleManager.loadRules(Arrays.asList(rule1, rule2, rule3, rule4));
	}

	@Test
	public void contextLoads() throws Exception {
		assertThat(echoService).isNotNull();
		assertThat(fooService).isNotNull();
	}

	@Test
	public void testFeignClient() {
		assertThat(echoService.echo("test")).isEqualTo("echo fallback");
		assertThat(fooService.echo("test")).isEqualTo("foo fallback");

		assertThatThrownBy(() -> {
			barService.bar();
		}).isInstanceOf(Exception.class);

		assertThatThrownBy(() -> {
			bazService.baz();
		}).isInstanceOf(Exception.class);

		assertThat(fooService.toString()).isNotEqualTo(echoService.toString());
		assertThat(fooService.hashCode()).isNotEqualTo(echoService.hashCode());
		assertThat(echoService.equals(fooService)).isEqualTo(Boolean.FALSE);
	}

	@Test
	public void testFeignServiceInstanceResourceHandler() {
		assertThat(echoService.echo("test")).isEqualTo("echo fallback");
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

	@ResourceHandler(ResourceHandlerHolder.SERVICE_INSTANCE)
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

	@FeignClient("bar-service")
	public interface BarService {

		@RequestMapping(path = "bar")
		String bar();

	}

	public interface BazService {

		@RequestMapping(path = "baz")
		String baz();

	}

	@FeignClient("baz-service")
	public interface BazClient extends BazService {

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
