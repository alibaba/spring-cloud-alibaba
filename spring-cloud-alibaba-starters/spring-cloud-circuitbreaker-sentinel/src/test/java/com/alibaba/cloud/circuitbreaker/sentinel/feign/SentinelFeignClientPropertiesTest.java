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

package com.alibaba.cloud.circuitbreaker.sentinel.feign;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for {@link SentinelFeignClientProperties#copy()}.
 */
public class SentinelFeignClientPropertiesTest {

	@Test
	public void copyReturnsEqualButDistinctInstance() {
		SentinelFeignClientProperties original = new SentinelFeignClientProperties();
		original.setDefaultRule("custom-default");
		original.setEnableRefreshRules(false);
		Map<String, List<DegradeRule>> rules = new HashMap<>();
		List<DegradeRule> ruleList = new ArrayList<>();
		DegradeRule rule = new DegradeRule("resource-a");
		rule.setCount(5.0);
		ruleList.add(rule);
		rules.put("resource-a", ruleList);
		original.setRules(rules);

		SentinelFeignClientProperties copied = original.copy();

		assertThat(copied).isNotSameAs(original);
		assertThat(copied).isEqualTo(original);
	}

	@Test
	public void modifyingCopyDoesNotAffectOriginal() {
		SentinelFeignClientProperties original = new SentinelFeignClientProperties();
		Map<String, List<DegradeRule>> rules = new HashMap<>();
		List<DegradeRule> ruleList = new ArrayList<>();
		ruleList.add(new DegradeRule("resource-a"));
		rules.put("resource-a", ruleList);
		original.setRules(rules);

		SentinelFeignClientProperties copied = original.copy();
		copied.getRules().put("resource-b",
				new ArrayList<>(List.of(new DegradeRule("resource-b"))));
		copied.setDefaultRule("changed");

		assertThat(original.getRules()).containsOnlyKeys("resource-a");
		assertThat(original.getDefaultRule()).isEqualTo("default");
	}

	@Test
	public void copyFailsFastWhenJacksonCannotSerialize() {
		RuleAccessFailureProperties brokenProps = new RuleAccessFailureProperties();
		brokenProps.failOnRuleAccess = true;

		assertThatThrownBy(brokenProps::copy)
				.isInstanceOf(IllegalStateException.class)
				.hasMessageContaining("SentinelFeignClientProperties")
				.hasRootCauseInstanceOf(IllegalArgumentException.class)
				.rootCause()
				.hasMessage("simulated serialization failure");
	}

	// Control for copyFailsFastWhenJacksonCannotSerialize: the same carrier
	// round-trips cleanly once the getter stops throwing, so the failure asserted
	// there can only come from the getter and not from the carrier type itself.
	@Test
	public void copySucceedsForTheSameSubclassWhenTheGetterDoesNotThrow() {
		RuleAccessFailureProperties props = new RuleAccessFailureProperties();
		props.setDefaultRule("custom-default");
		Map<String, List<DegradeRule>> rules = new HashMap<>();
		rules.put("resource-a",
				new ArrayList<>(List.of(new DegradeRule("resource-a"))));
		props.setRules(rules);

		SentinelFeignClientProperties copied = props.copy();

		assertThat(copied).isNotSameAs(props);
		assertThat(copied).isEqualTo(props);
	}

	/**
	 * Named subclass used as the failure carrier. {@code failOnRuleAccess} has no
	 * accessors on purpose so that Jackson neither writes nor reads it, keeping the
	 * serialized form identical to the base class.
	 */
	public static class RuleAccessFailureProperties
			extends SentinelFeignClientProperties {

		boolean failOnRuleAccess;

		@Override
		public Map<String, List<DegradeRule>> getRules() {
			if (failOnRuleAccess) {
				throw new IllegalArgumentException("simulated serialization failure");
			}
			return super.getRules();
		}

	}

}
