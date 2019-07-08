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

package com.alibaba.cloud.sentinel.datasource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.alibaba.csp.sentinel.slots.block.AbstractRule;
import com.alibaba.csp.sentinel.slots.block.authority.AuthorityRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowRule;
import com.alibaba.csp.sentinel.slots.system.SystemRule;

/**
 * @author <a href="mailto:fangjian0423@gmail.com">Jim</a>
 */
public class RuleTypeTests {

	@Test
	public void testGetByName() {
		assertFalse("empty str rule name was not null",
				RuleType.getByName("").isPresent());
		assertFalse("test rule name was not null",
				RuleType.getByName("test").isPresent());
		assertFalse("param_flow rule name was not null",
				RuleType.getByName("param_flow").isPresent());
		assertFalse("param rule name was not null",
				RuleType.getByName("param").isPresent());
		assertFalse("FLOW rule name was not null",
				RuleType.getByName("FLOW").isPresent());
		assertTrue("flow rule name was null", RuleType.getByName("flow").isPresent());
		assertTrue("degrade rule name was null",
				RuleType.getByName("degrade").isPresent());
		assertTrue("param-flow rule name was null",
				RuleType.getByName("param-flow").isPresent());
		assertTrue("system rule name was null", RuleType.getByName("system").isPresent());
		assertTrue("authority rule name was null",
				RuleType.getByName("authority").isPresent());
		assertEquals("flow rule name was not equals RuleType.FLOW", RuleType.FLOW,
				RuleType.getByName("flow").get());
		assertEquals("flow rule name was not equals RuleType.DEGRADE", RuleType.DEGRADE,
				RuleType.getByName("degrade").get());
		assertEquals("flow rule name was not equals RuleType.PARAM_FLOW",
				RuleType.PARAM_FLOW, RuleType.getByName("param-flow").get());
		assertEquals("flow rule name was not equals RuleType.SYSTEM", RuleType.SYSTEM,
				RuleType.getByName("system").get());
		assertEquals("flow rule name was not equals RuleType.AUTHORITY",
				RuleType.AUTHORITY, RuleType.getByName("authority").get());
	}

	@Test
	public void testGetByClass() {
		assertFalse("Object.class type type was not null",
				RuleType.getByClass(Object.class).isPresent());
		assertFalse("AbstractRule.class rule type was not null",
				RuleType.getByClass(AbstractRule.class).isPresent());
		assertTrue("FlowRule.class rule type was null",
				RuleType.getByClass(FlowRule.class).isPresent());
		assertTrue("DegradeRule.class rule type was null",
				RuleType.getByClass(DegradeRule.class).isPresent());
		assertTrue("ParamFlowRule.class rule type was null",
				RuleType.getByClass(ParamFlowRule.class).isPresent());
		assertTrue("SystemRule.class rule type was null",
				RuleType.getByClass(SystemRule.class).isPresent());
		assertTrue("AuthorityRule.class rule type was null",
				RuleType.getByClass(AuthorityRule.class).isPresent());
	}

}
