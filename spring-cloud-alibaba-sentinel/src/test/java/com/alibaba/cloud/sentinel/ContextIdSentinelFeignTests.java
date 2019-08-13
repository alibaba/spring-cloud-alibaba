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

package com.alibaba.cloud.sentinel;

import static org.junit.Assert.*;

import java.util.Arrays;

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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.alibaba.cloud.sentinel.feign.SentinelFeignAutoConfiguration;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;

/**
 * Add this unit test to verify https://github.com/alibaba/spring-cloud-alibaba/pull/838
 * 
 * @author <a href="mailto:fangjian0423@gmail.com">Jim</a>
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = { ContextIdSentinelFeignTests.TestConfig.class }, properties = {
		"feign.sentinel.enabled=true" })
public class ContextIdSentinelFeignTests {

	@Autowired
	private EchoService echoService;

	@Autowired
	private FooService fooService;

	@Test
	public void testFeignClient() {
		assertEquals("Sentinel Feign Client fallback success", "echo fallback",
				echoService.echo("test"));
		assertEquals("Sentinel Feign Client fallbackFactory success", "foo fallback",
				fooService.echo("test"));
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

	}

	@FeignClient(contextId = "echoService", name = "service-provider", fallback = EchoServiceFallback.class, configuration = FeignConfiguration.class)
	public interface EchoService {
		@GetMapping(value = "/echo/{str}")
		String echo(@PathVariable("str") String str);
	}

	@FeignClient(contextId = "fooService", value = "foo-service", fallbackFactory = CustomFallbackFactory.class, configuration = FeignConfiguration.class)
	public interface FooService {
		@RequestMapping(path = "echo/{str}")
		String echo(@RequestParam("str") String param);
	}

	public static class FeignConfiguration {

		@Bean
		public EchoServiceFallback echoServiceFallback() {
			return new EchoServiceFallback();
		}

		@Bean
		public CustomFallbackFactory customFallbackFactory() {
			return new CustomFallbackFactory();
		}

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
