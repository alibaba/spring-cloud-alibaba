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

package com.alibaba.cloud.examples.configuration;

import java.util.Arrays;

import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.context.annotation.Configuration;

/**
 * Programmatic Sentinel rule configuration for the RestClient demo.
 *
 * @author QHT
 */
@Configuration
public class SentinelRulesConfiguration {

	private static final Logger log = LoggerFactory.getLogger(SentinelRulesConfiguration.class);

	@PostConstruct
	public void init() {
		log.info("Loading Sentinel rules...");

		// Flow rules
		FlowRule getRule = new FlowRule("GET:https://httpbin.org/get");
		getRule.setGrade(RuleConstant.FLOW_GRADE_QPS);
		getRule.setCount(1);
		getRule.setLimitApp("default");

		FlowRule expRule = new FlowRule("GET:https://httpbin.org/status/500");
		expRule.setGrade(RuleConstant.FLOW_GRADE_QPS);
		expRule.setCount(1000);
		expRule.setLimitApp("default");

		FlowRule rtRule = new FlowRule("GET:https://httpbin.org/delay/3");
		rtRule.setGrade(RuleConstant.FLOW_GRADE_QPS);
		rtRule.setCount(1000);
		rtRule.setLimitApp("default");

		FlowRuleManager.loadRules(Arrays.asList(getRule, expRule, rtRule));

		// Degrade rules
		DegradeRule degradeRule1 = new DegradeRule("GET:https://httpbin.org/status/500");
		degradeRule1.setGrade(RuleConstant.DEGRADE_GRADE_EXCEPTION_RATIO);
		degradeRule1.setCount(0.99); // value "1" is not supported now, see: https://github.com/alibaba/Sentinel/pull/1857
		degradeRule1.setMinRequestAmount(1);
		degradeRule1.setStatIntervalMs(10 * 1000);
		degradeRule1.setTimeWindow(30);
		degradeRule1.setLimitApp("default");

		DegradeRule degradeRule2 = new DegradeRule("GET:https://httpbin.org/delay/3");
		degradeRule2.setGrade(RuleConstant.DEGRADE_GRADE_RT);
		degradeRule2.setCount(2000);
		degradeRule1.setStatIntervalMs(10 * 1000);
		degradeRule2.setSlowRatioThreshold(0.1);
		degradeRule2.setMinRequestAmount(1);
		degradeRule2.setTimeWindow(30);
		degradeRule2.setLimitApp("default");

		DegradeRuleManager.loadRules(Arrays.asList(degradeRule1, degradeRule2));

		log.info("Sentinel rules loaded successfully.");
	}

}
