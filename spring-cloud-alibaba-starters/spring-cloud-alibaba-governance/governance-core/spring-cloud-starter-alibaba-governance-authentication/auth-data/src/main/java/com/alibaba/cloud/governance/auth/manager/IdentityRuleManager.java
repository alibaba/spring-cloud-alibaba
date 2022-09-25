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

import com.alibaba.cloud.governance.auth.rule.IdentityRule;

// TODO: To use this feature requires implementing mTLS first
public class IdentityRuleManager {

	private Map<String, IdentityRule> allowIdentityRules = new ConcurrentHashMap<>();

	private Map<String, IdentityRule> denyIdentityRules = new ConcurrentHashMap<>();

	public IdentityRuleManager() {

	}

	public IdentityRuleManager(Map<String, IdentityRule> allowIdentityRules,
			Map<String, IdentityRule> denyIdentityRules) {
		this.allowIdentityRules = allowIdentityRules;
		this.denyIdentityRules = denyIdentityRules;
	}

	public Map<String, IdentityRule> getAllowIdentityRules() {
		return allowIdentityRules;
	}

	public Map<String, IdentityRule> getDenyIdentityRules() {
		return denyIdentityRules;
	}

	public void setAllowIdentityRules(Map<String, IdentityRule> allowIdentityRules) {
		this.allowIdentityRules = allowIdentityRules;
	}

	public void setDenyIdentityRules(Map<String, IdentityRule> denyIdentityRules) {
		this.denyIdentityRules = denyIdentityRules;
	}

}
