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

import java.util.Collections;

/**
 * @author Freeman
 * @date 2022/8/27
 */
@SpringBootApplication
@RestController
public class SentinelFlowControlTestApp {

    public static void main(String[] args) {
        SpringApplication.run(SentinelFlowControlTestApp.class, args);
    }

    @RequestMapping("/notFlowControl")
    @SentinelResource("/notFlowControl")
    public String notFlowControl() {
        return "OK";
    }

    @RequestMapping("/flowControl")
    @SentinelResource(value = "/flowControl", fallback = "flowControlFallback")
    public String flowControl() {
        return "OK";
    }

    public static String flowControlFallback() {
        return "fallback";
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        FlowRule flowRule = new FlowRule();
        flowRule.setResource("/flowControl");
        flowRule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        flowRule.setCount(2);
        FlowRuleManager.loadRules(Collections.singletonList(flowRule));
    }

}
