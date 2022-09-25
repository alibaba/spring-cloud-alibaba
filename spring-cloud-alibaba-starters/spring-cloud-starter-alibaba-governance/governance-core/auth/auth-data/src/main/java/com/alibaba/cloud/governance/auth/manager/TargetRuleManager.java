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

import com.alibaba.cloud.governance.auth.rule.TargetRule;

public class TargetRuleManager {

	private Map<String, TargetRule> allowTargetRules = new ConcurrentHashMap<>();

	private Map<String, TargetRule> denyTargetRules = new ConcurrentHashMap<>();

	public TargetRuleManager() {

	}

	public TargetRuleManager(Map<String, TargetRule> allowTargetRules,
			Map<String, TargetRule> denyTargetRules) {
		this.allowTargetRules = allowTargetRules;
		this.denyTargetRules = denyTargetRules;
	}

	public Map<String, TargetRule> getAllowTargetRules() {
		return allowTargetRules;
	}

	public Map<String, TargetRule> getDenyTargetRules() {
		return denyTargetRules;
	}

	public void setAllowTargetRules(Map<String, TargetRule> allowTargetRules) {
		this.allowTargetRules = allowTargetRules;
	}

	public void setDenyTargetRules(Map<String, TargetRule> denyTargetRules) {
		this.denyTargetRules = denyTargetRules;
	}

}
