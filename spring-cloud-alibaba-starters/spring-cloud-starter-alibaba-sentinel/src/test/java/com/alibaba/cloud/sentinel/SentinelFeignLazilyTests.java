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

import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author <a href="mailto:fangjian0423@gmail.com">Jim</a>
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {SentinelFeignTests.TestConfig.class},
		properties = {"feign.sentinel.enabled=true", "spring.cloud.openfeign.lazy-attributes-resolution=true"})
public class SentinelFeignLazilyTests {

	@Autowired
	private SentinelFeignTests.EchoService echoService;

	@Autowired
	private SentinelFeignTests.FooService fooService;

	@Autowired
	private SentinelFeignTests.BarService barService;

	@Autowired
	private SentinelFeignTests.BazService bazService;

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

}
