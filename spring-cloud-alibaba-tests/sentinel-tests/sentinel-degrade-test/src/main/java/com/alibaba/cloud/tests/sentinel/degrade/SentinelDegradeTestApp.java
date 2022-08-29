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
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.Collections;

/**
 * @author Freeman
 * @date 2022/8/27
 */
@SpringBootApplication
@RestController
public class SentinelDegradeTestApp {

    public static void main(String[] args) {
        SpringApplication.run(SentinelDegradeTestApp.class, args);
    }

    @RequestMapping("/degrade")
    @SentinelResource(value = "/degrade", fallback = "fallback")
    public String degrade() {
        throw new RuntimeException("Ops, something wrong!");
    }

    public static String fallback() {
        return "fallback";
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        DegradeRule degradeRule = new DegradeRule();
        degradeRule.setResource("/degrade");
        degradeRule.setGrade(RuleConstant.DEGRADE_GRADE_EXCEPTION_COUNT);
        degradeRule.setMinRequestAmount(1);
        degradeRule.setStatIntervalMs(2);
        degradeRule.setTimeWindow(1);
        degradeRule.setCount(1);
        DegradeRuleManager.loadRules(Arrays.asList(degradeRule));
    }

}
