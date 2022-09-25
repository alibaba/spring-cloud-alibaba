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

package com.alibaba.cloud.governance.auth.rule;

import com.alibaba.cloud.governance.common.matcher.IpMatcher;
import com.alibaba.cloud.governance.common.rule.AndRule;

public class IpBlockRule {

	private String name;

	private AndRule<IpMatcher> sourceIps;

	private AndRule<IpMatcher> remoteIps;

	private AndRule<IpMatcher> destIps;

	public IpBlockRule(String name, AndRule<IpMatcher> sourceIps,
			AndRule<IpMatcher> remoteIps, AndRule<IpMatcher> destIps) {
		this(name, sourceIps, remoteIps);
		this.destIps = destIps;
	}

	public IpBlockRule(String name, AndRule<IpMatcher> sourceIps,
			AndRule<IpMatcher> remoteIps) {
		this.name = name;
		this.sourceIps = sourceIps;
		this.remoteIps = remoteIps;
	}

	public AndRule<IpMatcher> getSourceIps() {
		return sourceIps;
	}

	public AndRule<IpMatcher> getRemoteIps() {
		return remoteIps;
	}

	public AndRule<IpMatcher> getDestIps() {
		return destIps;
	}

	public void setDestIps(AndRule<IpMatcher> destIps) {
		this.destIps = destIps;
	}

	public String getName() {
		return name;
	}

}
