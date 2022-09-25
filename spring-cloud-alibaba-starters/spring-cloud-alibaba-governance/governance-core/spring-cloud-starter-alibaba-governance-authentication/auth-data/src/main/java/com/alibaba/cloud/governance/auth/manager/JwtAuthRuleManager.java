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

import com.alibaba.cloud.governance.auth.rule.JwtAuthRule;

public class JwtAuthRuleManager {

	private Map<String, JwtAuthRule> allowJwtAuthRules = new ConcurrentHashMap<>();

	private Map<String, JwtAuthRule> denyJwtAuthRules = new ConcurrentHashMap<>();

	public JwtAuthRuleManager() {

	}

	public JwtAuthRuleManager(Map<String, JwtAuthRule> allowJwtAuthRules,
			Map<String, JwtAuthRule> denyJwtAuthRules) {
		this.allowJwtAuthRules = allowJwtAuthRules;
		this.denyJwtAuthRules = denyJwtAuthRules;
	}

	public Map<String, JwtAuthRule> getAllowJwtAuthRules() {
		return allowJwtAuthRules;
	}

	public Map<String, JwtAuthRule> getDenyJwtAuthRules() {
		return denyJwtAuthRules;
	}

	public void setAllowJwtAuthRules(Map<String, JwtAuthRule> allowJwtAuthRules) {
		this.allowJwtAuthRules = allowJwtAuthRules;
	}

	public void setDenyJwtAuthRules(Map<String, JwtAuthRule> denyJwtAuthRules) {
		this.denyJwtAuthRules = denyJwtAuthRules;
	}

}
