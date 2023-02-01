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
