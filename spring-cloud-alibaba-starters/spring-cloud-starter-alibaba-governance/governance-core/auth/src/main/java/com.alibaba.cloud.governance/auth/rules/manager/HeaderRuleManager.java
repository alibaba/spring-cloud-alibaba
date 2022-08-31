package com.alibaba.cloud.governance.auth.rules.manager;

import com.alibaba.cloud.governance.auth.rules.util.HeaderMatchUtil;
import com.alibaba.cloud.governance.auth.rules.auth.HttpHeaderRule;
import org.springframework.http.HttpHeaders;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class HeaderRuleManager {

	private static Map<String, HttpHeaderRule> allowHeaderRules = new ConcurrentHashMap<>();

	private static Map<String, HttpHeaderRule> denyHeaderRules = new ConcurrentHashMap<>();

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
								.anyMatch(orRule -> HeaderMatchUtil.matchHeader(headers,
										key, orRule));
						return andRule.isNot() != flag;
					});
				});
	}

}
