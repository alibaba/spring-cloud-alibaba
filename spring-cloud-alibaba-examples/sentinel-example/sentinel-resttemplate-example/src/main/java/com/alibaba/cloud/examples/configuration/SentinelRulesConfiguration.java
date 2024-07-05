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

package com.alibaba.cloud.examples.configuration;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import jakarta.annotation.PostConstruct;

import org.springframework.stereotype.Component;

/**
 * @description
 * @author ChengPu raozihao
 * @date 2023/2/11
 */
@Component
public class SentinelRulesConfiguration {
	/**
	 * You can configure sentinel rules by referring.
	 * https://sca.aliyun.com/docs/2023/user-guide/sentinel/advanced-guide/#%E6%9B%B4%E5%A4%9A%E9%85%8D%E7%BD%AE%E9%A1%B9
	 */
	@PostConstruct
	public void init() {
		System.out.println("Load Sentinel Rules start！");
		List<FlowRule> flowRules = new ArrayList<FlowRule>();
		FlowRule flowRule = new FlowRule();
		flowRule.setResource("GET:https://httpbin.org/get");
		flowRule.setCount(1);
		flowRule.setControlBehavior(RuleConstant.CONTROL_BEHAVIOR_DEFAULT);
		flowRule.setStrategy(RuleConstant.STRATEGY_DIRECT);
		flowRule.setGrade(RuleConstant.FLOW_GRADE_QPS);
		flowRule.setLimitApp("default");
		flowRules.add(flowRule);
		FlowRuleManager.loadRules(flowRules);

		List<DegradeRule> degradeRules = new ArrayList<DegradeRule>();
		DegradeRule degradeRule1 = new DegradeRule();
		degradeRule1.setResource("GET:https://httpbin.org/status/500");
		degradeRule1.setCount(1);
		degradeRule1.setMinRequestAmount(1);
		degradeRule1.setTimeWindow(30);
		degradeRule1.setGrade(RuleConstant.DEGRADE_GRADE_EXCEPTION_COUNT);
		degradeRule1.setLimitApp("default");
		degradeRules.add(degradeRule1);

		DegradeRule degradeRule2 = new DegradeRule();
		degradeRule2.setResource("GET:https://httpbin.org/delay/3");
		degradeRule2.setCount(1);
		degradeRule2.setGrade(RuleConstant.DEGRADE_GRADE_RT);
		degradeRule2.setSlowRatioThreshold(0.1);
		degradeRule2.setMinRequestAmount(1);
		degradeRule2.setTimeWindow(30);
		degradeRule2.setLimitApp("default");
		degradeRules.add(degradeRule2);
		DegradeRuleManager.loadRules(degradeRules);
		System.out.println("Load Sentinel Rules end！");
	}
}
