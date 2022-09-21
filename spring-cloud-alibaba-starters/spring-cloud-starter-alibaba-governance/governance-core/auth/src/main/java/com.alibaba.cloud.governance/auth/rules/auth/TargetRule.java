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

package com.alibaba.cloud.governance.auth.rules.auth;

import com.alibaba.cloud.governance.common.matcher.StringMatcher;
import com.alibaba.cloud.governance.common.rule.AndRule;

public class TargetRule {

	private final String name;

	private final AndRule<StringMatcher> hosts;

	private final AndRule<Integer> ports;

	private final AndRule<StringMatcher> methods;

	private final AndRule<StringMatcher> paths;

	public TargetRule(String name, AndRule<StringMatcher> hosts, AndRule<Integer> ports,
			AndRule<StringMatcher> methods, AndRule<StringMatcher> paths) {
		this.name = name;
		this.hosts = hosts;
		this.ports = ports;
		this.methods = methods;
		this.paths = paths;
	}

	public String getName() {
		return name;
	}

	public AndRule<StringMatcher> getHosts() {
		return hosts;
	}

	public AndRule<Integer> getPorts() {
		return ports;
	}

	public AndRule<StringMatcher> getMethods() {
		return methods;
	}

	public AndRule<StringMatcher> getPaths() {
		return paths;
	}

}
