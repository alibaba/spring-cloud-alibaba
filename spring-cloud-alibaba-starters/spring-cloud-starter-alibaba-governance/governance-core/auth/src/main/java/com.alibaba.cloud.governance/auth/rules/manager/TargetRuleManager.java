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

import com.alibaba.cloud.governance.auth.rules.auth.TargetRule;

public final class TargetRuleManager {

	private static Map<String, TargetRule> allowTargetRules = new ConcurrentHashMap<>();

	private static Map<String, TargetRule> denyTargetRules = new ConcurrentHashMap<>();

	private TargetRuleManager() {

	}

	public static boolean isValid(String host, int port, String method, String path) {
		if (!denyTargetRules.isEmpty()
				&& judgeTargetRule(denyTargetRules, host, port, method, path)) {
			return false;
		}
		if (allowTargetRules.isEmpty()) {
			return true;
		}
		return judgeTargetRule(allowTargetRules, host, port, method, path);
	}

	private static boolean judgeHost(String host, TargetRule andRules) {
		return andRules.getHosts() == null || andRules.getHosts().isEmpty()
				|| andRules.getHosts().getRules().stream().allMatch(orRules -> {
					boolean flag = orRules.getRules().stream()
							.anyMatch(httpHost -> httpHost.match(host));
					return orRules.isNot() != flag;
				});
	}

	private static boolean judgePort(int port, TargetRule andRules) {
		return andRules.getPorts() == null || andRules.getPorts().isEmpty()
				|| andRules.getPorts().getRules().stream().allMatch(orRules -> {
					boolean flag = orRules.getRules().stream()
							.anyMatch(httpPort -> port == httpPort);
					return orRules.isNot() != flag;
				});
	}

	private static boolean judgeMethod(String method, TargetRule andRules) {
		return andRules.getMethods() == null || andRules.getMethods().isEmpty()
				|| andRules.getMethods().getRules().stream().allMatch(orRules -> {
					boolean flag = orRules.getRules().stream()
							.anyMatch(httpMethod -> httpMethod.match(method));
					return orRules.isNot() != flag;
				});
	}

	private static boolean judgePath(String path, TargetRule andRules) {
		return andRules.getPaths() == null || andRules.getPaths().isEmpty()
				|| andRules.getPaths().getRules().stream().allMatch(orRules -> {
					boolean flag = orRules.getRules().stream()
							.anyMatch(httpPath -> httpPath.match(path));
					return orRules.isNot() != flag;
				});
	}

	private static boolean judgeTargetRule(Map<String, TargetRule> rules, String host,
			int port, String method, String path) {
		return rules.values().stream()
				.anyMatch(andRules -> judgeHost(host, andRules)
						&& judgePort(port, andRules) && judgeMethod(method, andRules)
						&& judgePath(path, andRules));
	}

	public static void addTargetRules(TargetRule targetRule, boolean isAllow) {
		if (isAllow) {
			allowTargetRules.put(targetRule.getName(), targetRule);
		}
		else {
			denyTargetRules.put(targetRule.getName(), targetRule);
		}
	}

	public static void clear() {
		allowTargetRules.clear();
		denyTargetRules.clear();
	}

}
