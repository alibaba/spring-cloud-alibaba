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

package com.alibaba.cloud.examples;

import java.util.Collections;

import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.SentinelRpcException;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

/**
 * @author fangjian
 */
@SpringBootApplication(scanBasePackages = "com.alibaba.cloud.examples")
public class SentinelDubboConsumerApp {

	@Bean
	public FooServiceConsumer annotationDemoServiceConsumer() {
		return new FooServiceConsumer();
	}

	public static void main(String[] args) {

		FlowRule flowRule = new FlowRule();
		flowRule.setResource(
				"com.alibaba.cloud.examples.FooService:hello(java.lang.String)");
		flowRule.setCount(10);
		flowRule.setGrade(RuleConstant.FLOW_GRADE_QPS);
		flowRule.setLimitApp("default");
		FlowRuleManager.loadRules(Collections.singletonList(flowRule));

		SpringApplicationBuilder consumerBuilder = new SpringApplicationBuilder();
		ApplicationContext applicationContext = consumerBuilder
				.web(WebApplicationType.NONE).sources(SentinelDubboConsumerApp.class)
				.run(args);

		FooServiceConsumer service = applicationContext.getBean(FooServiceConsumer.class);

		for (int i = 0; i < 15; i++) {
			try {
				String message = service.hello("Jim");
				System.out.println((i + 1) + " -> Success: " + message);
			}
			catch (SentinelRpcException ex) {
				System.out.println("Blocked");
			}
			catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

}
