/*
 * Copyright 2013-2019 the original author or authors.
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

package com.alibaba.cloud.circuitbreaker.sentinel;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;

import org.springframework.cloud.client.circuitbreaker.ConfigBuilder;
import org.springframework.util.Assert;

/**
 * @author Eric Zhao
 */
public class SentinelConfigBuilder implements
		ConfigBuilder<SentinelConfigBuilder.SentinelCircuitBreakerConfiguration> {

	private String resourceName;

	private EntryType entryType;

	private List<DegradeRule> rules;

	public SentinelConfigBuilder() {
	}

	public SentinelConfigBuilder(String resourceName) {
		this.resourceName = resourceName;
	}

	public SentinelConfigBuilder resourceName(String resourceName) {
		this.resourceName = resourceName;
		return this;
	}

	public SentinelConfigBuilder entryType(EntryType entryType) {
		this.entryType = entryType;
		return this;
	}

	public SentinelConfigBuilder rules(List<DegradeRule> rules) {
		this.rules = rules;
		return this;
	}

	@Override
	public SentinelCircuitBreakerConfiguration build() {
		Assert.hasText(resourceName, "resourceName cannot be empty");
		List<DegradeRule> rules = Optional.ofNullable(this.rules)
				.orElse(new ArrayList<>());

		EntryType entryType = Optional.ofNullable(this.entryType).orElse(EntryType.OUT);
		return new SentinelCircuitBreakerConfiguration()
				.setResourceName(this.resourceName).setEntryType(entryType)
				.setRules(rules);
	}

	public static class SentinelCircuitBreakerConfiguration {

		private String resourceName;

		private EntryType entryType;

		private List<DegradeRule> rules;

		public String getResourceName() {
			return resourceName;
		}

		public SentinelCircuitBreakerConfiguration setResourceName(String resourceName) {
			this.resourceName = resourceName;
			return this;
		}

		public EntryType getEntryType() {
			return entryType;
		}

		public SentinelCircuitBreakerConfiguration setEntryType(EntryType entryType) {
			this.entryType = entryType;
			return this;
		}

		public List<DegradeRule> getRules() {
			return rules;
		}

		public SentinelCircuitBreakerConfiguration setRules(List<DegradeRule> rules) {
			this.rules = rules;
			return this;
		}

	}

}