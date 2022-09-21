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

import com.alibaba.cloud.governance.auth.rules.auth.HttpHeaderRule;

import org.springframework.http.HttpHeaders;

public final class HeaderRuleManager {

	private static Map<String, HttpHeaderRule> allowHeaderRules = new ConcurrentHashMap<>();

	private static Map<String, HttpHeaderRule> denyHeaderRules = new ConcurrentHashMap<>();

	private HeaderRuleManager() {

	}

	public static void addHttpHeaderRule(HttpHeaderRule rule, boolean isAllow) {
		if (isAllow) {
			allowHeaderRules.put(rule.getName(), rule);
		}
		else {
			denyHeaderRules.put(rule.getName(), rule);
		}
	}

	public static void clear() {
		allowHeaderRules.clear();
		denyHeaderRules.clear();
	}

	public static boolean isValid(HttpHeaders headers) {
		if (!denyHeaderRules.isEmpty() && judgeHttpHeaderRule(denyHeaderRules, headers)) {
			return false;
		}
		if (allowHeaderRules.isEmpty()) {
			return true;
		}
		return judgeHttpHeaderRule(allowHeaderRules, headers);
	}

	private static boolean judgeHttpHeaderRule(Map<String, HttpHeaderRule> rules,
			HttpHeaders headers) {
		return rules.values().stream()
				.anyMatch(andRules -> judgeHttpHeaderRule(andRules, headers));
	}

	private static boolean judgeHttpHeaderRule(HttpHeaderRule andRules,
			HttpHeaders headers) {
		return andRules == null || andRules.getHeaders().isEmpty()
				|| andRules.getHeaders().entrySet().stream().allMatch(allHeader -> {
					String key = allHeader.getKey();
					return allHeader.getValue().getRules().stream().allMatch(andRule -> {
						boolean flag = andRule.getRules().stream()
								.anyMatch(orRule -> orRule.match(headers, key));
						return andRule.isNot() != flag;
					});
				});
	}

}
