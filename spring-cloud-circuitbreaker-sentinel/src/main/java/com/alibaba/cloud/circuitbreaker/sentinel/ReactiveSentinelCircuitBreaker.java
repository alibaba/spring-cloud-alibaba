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

import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.adapter.reactor.EntryConfig;
import com.alibaba.csp.sentinel.adapter.reactor.SentinelReactorTransformer;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreaker;
import org.springframework.util.Assert;

/**
 * Sentinel implementation of {@link ReactiveCircuitBreaker}.
 *
 * @author Eric Zhao
 */
public class ReactiveSentinelCircuitBreaker implements ReactiveCircuitBreaker {

	private final String resourceName;

	private final EntryType entryType;

	private final List<DegradeRule> rules;

	public ReactiveSentinelCircuitBreaker(String resourceName, EntryType entryType,
			List<DegradeRule> rules) {
		Assert.hasText(resourceName, "resourceName cannot be blank");
		Assert.notNull(rules, "rules should not be null");
		this.resourceName = resourceName;
		this.entryType = entryType;
		this.rules = Collections.unmodifiableList(rules);

		applyToSentinelRuleManager();
	}

	public ReactiveSentinelCircuitBreaker(String resourceName, List<DegradeRule> rules) {
		this(resourceName, EntryType.OUT, rules);
	}

	public ReactiveSentinelCircuitBreaker(String resourceName) {
		this(resourceName, EntryType.OUT, Collections.emptyList());
	}

	private void applyToSentinelRuleManager() {
		if (this.rules == null || this.rules.isEmpty()) {
			return;
		}
		Set<DegradeRule> ruleSet = new HashSet<>(DegradeRuleManager.getRules());
		for (DegradeRule rule : this.rules) {
			if (rule == null) {
				continue;
			}
			rule.setResource(resourceName);
			ruleSet.add(rule);
		}
		DegradeRuleManager.loadRules(new ArrayList<>(ruleSet));
	}

	@Override
	public <T> Mono<T> run(Mono<T> toRun, Function<Throwable, Mono<T>> fallback) {
		Mono<T> toReturn = toRun.transform(new SentinelReactorTransformer<>(
				new EntryConfig(resourceName, entryType)));
		if (fallback != null) {
			toReturn = toReturn.onErrorResume(fallback);
		}
		return toReturn;
	}

	@Override
	public <T> Flux<T> run(Flux<T> toRun, Function<Throwable, Flux<T>> fallback) {
		Flux<T> toReturn = toRun.transform(new SentinelReactorTransformer<>(
				new EntryConfig(resourceName, entryType)));
		if (fallback != null) {
			toReturn = toReturn.onErrorResume(fallback);
		}
		return toReturn;
	}

}