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

package com.alibaba.cloud.governance.auth.rules.manager;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.alibaba.cloud.governance.auth.rules.auth.IdentityRule;

// TODO: To use this feature requires implementing mTLS first
public final class IdentityRuleManager {

	private static Map<String, IdentityRule> allowIdentityRules = new ConcurrentHashMap<>();

	private static Map<String, IdentityRule> denyIdentityRules = new ConcurrentHashMap<>();

	private IdentityRuleManager() {

	}

	public static void addIdentityRule(IdentityRule rule, boolean isAllow) {
		if (isAllow) {
			allowIdentityRules.put(rule.getName(), rule);
		}
		else {
			denyIdentityRules.put(rule.getName(), rule);
		}
	}

	public static void clear() {
		allowIdentityRules.clear();
		denyIdentityRules.clear();
	}

}
