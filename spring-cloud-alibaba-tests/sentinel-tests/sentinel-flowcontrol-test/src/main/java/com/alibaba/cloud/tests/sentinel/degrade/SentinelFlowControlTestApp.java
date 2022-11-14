/*
 * Copyright 2013-2022 the original author or authors.
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

package com.alibaba.cloud.tests.sentinel.degrade;

import java.util.Arrays;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.alibaba.cloud.tests.sentinel.degrade.Util.FLOW_CONTROL_NOT_TRIGGERED;
import static com.alibaba.cloud.tests.sentinel.degrade.Util.FLOW_CONTROL_TRIGGERED;

/**
 * @author Freeman
 */
@SpringBootApplication
@RestController
public class SentinelFlowControlTestApp {

	public static void main(String[] args) {
		SpringApplication.run(SentinelFlowControlTestApp.class, args);
	}

	@RequestMapping(FLOW_CONTROL_NOT_TRIGGERED)
	@SentinelResource(value = FLOW_CONTROL_NOT_TRIGGERED, defaultFallback = "fallback")
	public String flowControlNotTriggered() {
		return "OK";
	}

	@RequestMapping(FLOW_CONTROL_TRIGGERED)
	@SentinelResource(value = FLOW_CONTROL_TRIGGERED, defaultFallback = "fallback")
	public String flowControlTriggered() {
		return "OK";
	}

	private static String fallback() {
		return "fallback";
	}

	@EventListener(ApplicationReadyEvent.class)
	public void onApplicationReady() {
		FlowRule notTriggeredRule = new FlowRule();
		notTriggeredRule.setResource(FLOW_CONTROL_NOT_TRIGGERED);
		notTriggeredRule.setGrade(RuleConstant.FLOW_GRADE_QPS);
		notTriggeredRule.setCount(4);
		FlowRule triggeredRule = new FlowRule();
		triggeredRule.setResource(FLOW_CONTROL_TRIGGERED);
		triggeredRule.setGrade(RuleConstant.FLOW_GRADE_QPS);
		triggeredRule.setCount(3);
		FlowRuleManager.loadRules(Arrays.asList(notTriggeredRule, triggeredRule));
	}

}
