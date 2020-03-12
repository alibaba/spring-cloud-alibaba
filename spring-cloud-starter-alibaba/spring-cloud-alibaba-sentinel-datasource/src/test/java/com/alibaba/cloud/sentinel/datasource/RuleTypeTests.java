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

package com.alibaba.cloud.sentinel.datasource;

import com.alibaba.csp.sentinel.slots.block.AbstractRule;
import com.alibaba.csp.sentinel.slots.block.authority.AuthorityRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowRule;
import com.alibaba.csp.sentinel.slots.system.SystemRule;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author <a href="mailto:fangjian0423@gmail.com">Jim</a>
 */
public class RuleTypeTests {

	@Test
	public void testGetByName() {
		assertThat(RuleType.getByName("").isPresent()).isEqualTo(Boolean.FALSE);
		assertThat(RuleType.getByName("test").isPresent()).isEqualTo(Boolean.FALSE);
		assertThat(RuleType.getByName("param_flow").isPresent()).isEqualTo(Boolean.FALSE);
		assertThat(RuleType.getByName("param").isPresent()).isEqualTo(Boolean.FALSE);
		assertThat(RuleType.getByName("FLOW").isPresent()).isEqualTo(Boolean.FALSE);
		assertThat(RuleType.getByName("flow").isPresent()).isEqualTo(Boolean.TRUE);
		assertThat(RuleType.getByName("degrade").isPresent()).isEqualTo(Boolean.TRUE);
		assertThat(RuleType.getByName("param-flow").isPresent()).isEqualTo(Boolean.TRUE);
		assertThat(RuleType.getByName("system").isPresent()).isEqualTo(Boolean.TRUE);
		assertThat(RuleType.getByName("authority").isPresent()).isEqualTo(Boolean.TRUE);
		assertThat(RuleType.getByName("flow").get()).isEqualTo(RuleType.FLOW);
		assertThat(RuleType.getByName("degrade").get()).isEqualTo(RuleType.DEGRADE);
		assertThat(RuleType.getByName("param-flow").get()).isEqualTo(RuleType.PARAM_FLOW);
		assertThat(RuleType.getByName("system").get()).isEqualTo(RuleType.SYSTEM);
		assertThat(RuleType.getByName("authority").get()).isEqualTo(RuleType.AUTHORITY);
	}

	@Test
	public void testGetByClass() {
		assertThat(RuleType.getByClass(Object.class).isPresent())
				.isEqualTo(Boolean.FALSE);
		assertThat(RuleType.getByClass(AbstractRule.class).isPresent())
				.isEqualTo(Boolean.FALSE);
		assertThat(RuleType.getByClass(FlowRule.class).isPresent())
				.isEqualTo(Boolean.TRUE);
		assertThat(RuleType.getByClass(DegradeRule.class).isPresent())
				.isEqualTo(Boolean.TRUE);
		assertThat(RuleType.getByClass(ParamFlowRule.class).isPresent())
				.isEqualTo(Boolean.TRUE);
		assertThat(RuleType.getByClass(SystemRule.class).isPresent())
				.isEqualTo(Boolean.TRUE);
		assertThat(RuleType.getByClass(AuthorityRule.class).isPresent())
				.isEqualTo(Boolean.TRUE);
	}

}
