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

import com.alibaba.cloud.governance.auth.rules.auth.IpBlockRule;
import com.alibaba.cloud.governance.common.matcher.IpMatcher;
import com.alibaba.cloud.governance.common.rule.AndRule;

public final class IpBlockRuleManager {

	private static Map<String, IpBlockRule> allowIpBlockRules = new ConcurrentHashMap<>();

	private static Map<String, IpBlockRule> denyIpBlockRules = new ConcurrentHashMap<>();

	private IpBlockRuleManager() {

	}

	public static boolean isValid(String sourceIp, String destIp, String remoteIp) {
		if (!denyIpBlockRules.isEmpty()
				&& judgeIpBlockRule(denyIpBlockRules, sourceIp, destIp, remoteIp)) {
			return false;
		}
		if (allowIpBlockRules.isEmpty()) {
			return true;
		}
		return judgeIpBlockRule(allowIpBlockRules, sourceIp, destIp, remoteIp);
	}

	private static boolean judgeIpBlockRule(Map<String, IpBlockRule> rule,
			String sourceIp, String destIp, String remoteIp) {
		return rule.values().stream()
				.allMatch(andRules -> judgeSourceIp(sourceIp, andRules)
						&& judgeDestIp(destIp, andRules)
						&& judgeRemoteIp(remoteIp, andRules));
	}

	private static boolean judgeSourceIp(String ip, IpBlockRule andRules) {
		return andRules.getSourceIps() == null || andRules.getSourceIps().isEmpty()
				|| andRules.getSourceIps().getRules().stream().allMatch(orRules -> {
					boolean flag = orRules.getRules().stream()
							.anyMatch(httpSourceIp -> httpSourceIp.match(ip));
					return orRules.isNot() != flag;
				});
	}

	private static boolean judgeDestIp(String destIp, IpBlockRule andRules) {
		return andRules.getDestIps() == null || andRules.getDestIps().isEmpty()
				|| andRules.getDestIps().getRules().stream().allMatch(orRules -> {
					boolean flag = orRules.getRules().stream()
							.anyMatch(httpDestIp -> httpDestIp.match(destIp));
					return orRules.isNot() != flag;
				});
	}

	private static boolean judgeRemoteIp(String remoteIp, IpBlockRule andRules) {
		return andRules.getRemoteIps() == null || andRules.getRemoteIps().isEmpty()
				|| andRules.getRemoteIps().getRules().stream().allMatch(orRules -> {
					boolean flag = orRules.getRules().stream()
							.anyMatch(httpRemoteIp -> httpRemoteIp.match(remoteIp));
					return orRules.isNot() != flag;
				});
	}

	public static void addIpBlockRules(IpBlockRule rule, boolean isAllow) {
		if (isAllow) {
			allowIpBlockRules.put(rule.getName(), rule);
		}
		else {
			denyIpBlockRules.put(rule.getName(), rule);
		}
	}

	public static void updateDestIpRules(String name, AndRule<IpMatcher> destIpRules,
			boolean isAllow) {
		if (isAllow && allowIpBlockRules.containsKey(name)) {
			allowIpBlockRules.get(name).setDestIps(destIpRules);
		}
		if (!isAllow && denyIpBlockRules.containsKey(name)) {
			denyIpBlockRules.get(name).setDestIps(destIpRules);
		}
	}

	public static void clear() {
		allowIpBlockRules.clear();
		denyIpBlockRules.clear();
	}

}
