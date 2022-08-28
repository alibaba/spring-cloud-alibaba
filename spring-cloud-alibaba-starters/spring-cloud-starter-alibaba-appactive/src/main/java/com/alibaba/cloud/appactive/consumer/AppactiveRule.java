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

package com.alibaba.cloud.appactive.consumer;

import com.netflix.loadbalancer.AbstractServerPredicate;
import com.netflix.loadbalancer.CompositePredicate;
import com.netflix.loadbalancer.ILoadBalancer;
import com.netflix.loadbalancer.PredicateBasedRule;
import com.netflix.loadbalancer.PredicateKey;
import com.netflix.loadbalancer.RoundRobinRule;
import com.netflix.loadbalancer.Server;

/**
 * @description
 * @author ChengPu raozihao
 * @date 2022/8/21
 */
public class AppactiveRule extends PredicateBasedRule {

	RoundRobinRule roundRobinRule = new RoundRobinRule();

	private AbstractServerPredicate predicate;

	public AppactiveRule() {
		super();
		predicate = CompositePredicate.withPredicate(new AppactivePredicate(this, null))
				.addFallbackPredicate(AbstractServerPredicate.alwaysTrue()).build();
	}

	@Override
	public void setLoadBalancer(ILoadBalancer lb) {
		super.setLoadBalancer(lb);
		roundRobinRule.setLoadBalancer(lb);
	}

	@Override
	public AbstractServerPredicate getPredicate() {
		return predicate;
	}

	@Override
	public Server choose(Object key) {
		int count = 0;
		Server server = roundRobinRule.choose(key);
		while (count++ <= 10) {
			if (predicate.apply(new PredicateKey(server))) {
				return server;
			}
			server = roundRobinRule.choose(key);
		}
		return super.choose(key);
	}

}
