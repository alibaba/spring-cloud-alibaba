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

package com.alibaba.cloud.governance.auth.manager;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.alibaba.cloud.governance.auth.rule.IpBlockRule;

public class IpBlockRuleManager {

	private Map<String, IpBlockRule> allowIpBlockRules = new ConcurrentHashMap<>();

	private Map<String, IpBlockRule> denyIpBlockRules = new ConcurrentHashMap<>();

	public IpBlockRuleManager() {

	}

	public IpBlockRuleManager(Map<String, IpBlockRule> allowIpBlockRules,
			Map<String, IpBlockRule> denyIpBlockRules) {
		this.allowIpBlockRules = allowIpBlockRules;
		this.denyIpBlockRules = denyIpBlockRules;
	}

	public Map<String, IpBlockRule> getAllowIpBlockRules() {
		return allowIpBlockRules;
	}

	public Map<String, IpBlockRule> getDenyIpBlockRules() {
		return denyIpBlockRules;
	}

	public void setAllowIpBlockRules(Map<String, IpBlockRule> allowIpBlockRules) {
		this.allowIpBlockRules = allowIpBlockRules;
	}

	public void setDenyIpBlockRules(Map<String, IpBlockRule> denyIpBlockRules) {
		this.denyIpBlockRules = denyIpBlockRules;
	}

}
