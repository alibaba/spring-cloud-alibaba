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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.Tracer;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;

import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;
import org.springframework.util.Assert;

/**
 * Sentinel implementation of {@link CircuitBreaker}.
 *
 * @author Eric Zhao
 */
public class SentinelCircuitBreaker implements CircuitBreaker {

	private final String RESOURCE_NAME;

	private final EntryType ENTRY_TYPE;

	private final List<DegradeRule> RULES;

	public SentinelCircuitBreaker(String resourceName, EntryType entryType,
			List<DegradeRule> rules) {
		Assert.hasText(resourceName, "resourceName cannot be blank");
		Assert.notNull(rules, "rules should not be null");
		this.RESOURCE_NAME = resourceName;
		this.ENTRY_TYPE = entryType;
		this.RULES = Collections.unmodifiableList(rules);

		applyToSentinelRuleManager();
	}

	public SentinelCircuitBreaker(String resourceName, List<DegradeRule> rules) {
		this(resourceName, EntryType.OUT, rules);
	}

	public SentinelCircuitBreaker(String resourceName) {
		this(resourceName, EntryType.OUT, Collections.emptyList());
	}

	private void applyToSentinelRuleManager() {
		if (this.RULES == null || this.RULES.isEmpty()) {
			return;
		}
		Set<DegradeRule> ruleSet = new HashSet<>(DegradeRuleManager.getRules());
		for (DegradeRule rule : this.RULES) {
			if (rule == null) {
				continue;
			}
			rule.setResource(RESOURCE_NAME);
			ruleSet.add(rule);
		}
		DegradeRuleManager.loadRules(new ArrayList<>(ruleSet));
	}

	@Override
	public <T> T run(Supplier<T> toRun, Function<Throwable, T> fallback) {
		Entry entry = null;
		try {
			entry = SphU.entry(RESOURCE_NAME, entryType);
			// If the SphU.entry() does not throw `BlockException`, it means that the
			// request can pass.
			return toRun.get();
		}
		catch (BlockException ex) {
			// SphU.entry() may throw BlockException which indicates that
			// the request was rejected (flow control or circuit breaking triggered).
			// So it should not be counted as the business exception.
			return fallback.apply(ex);
		}
		catch (Exception ex) {
			// For other kinds of exceptions, we'll trace the exception count via
			// Tracer.trace(ex).
			Tracer.trace(ex);
			return fallback.apply(ex);
		}
		finally {
			// Guarantee the invocation has been completed.
			if (entry != null) {
				entry.exit();
			}
		}
	}

}
