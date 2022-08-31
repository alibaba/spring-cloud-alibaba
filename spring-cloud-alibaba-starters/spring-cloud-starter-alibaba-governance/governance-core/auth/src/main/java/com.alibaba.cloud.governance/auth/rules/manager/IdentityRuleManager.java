package com.alibaba.cloud.governance.auth.rules.manager;

import com.alibaba.cloud.governance.auth.rules.auth.IdentityRule;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// TODO: To use this feature requires implementing mTLS first
public class IdentityRuleManager {

	private static Map<String, IdentityRule> allowIdentityRules = new ConcurrentHashMap<>();

	private static Map<String, IdentityRule> denyIdentityRules = new ConcurrentHashMap<>();

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
