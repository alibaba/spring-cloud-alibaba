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

import com.alibaba.cloud.governance.auth.rule.HttpHeaderRule;

public class HeaderRuleManager {

	private Map<String, HttpHeaderRule> allowHeaderRules = new ConcurrentHashMap<>();

	private Map<String, HttpHeaderRule> denyHeaderRules = new ConcurrentHashMap<>();

	public HeaderRuleManager() {

	}

	public HeaderRuleManager(Map<String, HttpHeaderRule> allowHeaderRules,
			Map<String, HttpHeaderRule> denyHeaderRules) {
		this.allowHeaderRules = allowHeaderRules;
		this.denyHeaderRules = denyHeaderRules;
	}

	public Map<String, HttpHeaderRule> getAllowHeaderRules() {
		return allowHeaderRules;
	}

	public Map<String, HttpHeaderRule> getDenyHeaderRules() {
		return denyHeaderRules;
	}

	public void setAllowHeaderRules(Map<String, HttpHeaderRule> allowHeaderRules) {
		this.allowHeaderRules = allowHeaderRules;
	}

	public void setDenyHeaderRules(Map<String, HttpHeaderRule> denyHeaderRules) {
		this.denyHeaderRules = denyHeaderRules;
	}

}
