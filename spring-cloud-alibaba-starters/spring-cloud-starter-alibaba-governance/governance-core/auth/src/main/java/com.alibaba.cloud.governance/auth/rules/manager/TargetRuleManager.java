package com.alibaba.cloud.governance.auth.rules.manager;

import com.alibaba.cloud.governance.auth.rules.auth.TargetRule;
import com.alibaba.cloud.governance.auth.rules.util.StringMatchUtil;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TargetRuleManager {

	private static Map<String, TargetRule> allowTargetRules = new ConcurrentHashMap<>();

	private static Map<String, TargetRule> denyTargetRules = new ConcurrentHashMap<>();

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
					boolean flag = orRules.getRules().stream().anyMatch(
							httpHost -> StringMatchUtil.matchStr(host, httpHost));
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
					boolean flag = orRules.getRules().stream().anyMatch(
							httpMethod -> StringMatchUtil.matchStr(method, httpMethod));
					return orRules.isNot() != flag;
				});
	}

	private static boolean judgePath(String path, TargetRule andRules) {
		return andRules.getPaths() == null || andRules.getPaths().isEmpty()
				|| andRules.getPaths().getRules().stream().allMatch(orRules -> {
					boolean flag = orRules.getRules().stream().anyMatch(
							httpPath -> StringMatchUtil.matchStr(path, httpPath));
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
