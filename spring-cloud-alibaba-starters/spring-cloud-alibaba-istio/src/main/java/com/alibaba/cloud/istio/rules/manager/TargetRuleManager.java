package com.alibaba.cloud.istio.rules.manager;

import com.alibaba.cloud.istio.rules.auth.TargetRule;
import com.alibaba.cloud.istio.util.StringMatchUtil;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TargetRuleManager {
    private static Map<String, TargetRule> allowTargetRules = new ConcurrentHashMap<>();
    private static Map<String, TargetRule> denyTargetRules = new ConcurrentHashMap<>();

    public static boolean isValid(String host, int port, String method, String path) {
        if (!denyTargetRules.isEmpty() && judgeTargetRule(denyTargetRules, host, port, method, path)) {
            return false;
        }
        if (allowTargetRules.isEmpty()) {
            return true;
        }
        return judgeTargetRule(allowTargetRules, host, port, method, path);
    }

    private static boolean judgeTargetRule(Map<String, TargetRule> rules, String host, int port, String method, String path) {
        return rules.values().stream().allMatch(andRules -> andRules.getHosts().stream().allMatch(orRules -> {
            boolean flag = orRules.getLeft().stream().anyMatch(httpHost -> StringMatchUtil.matchStr(host, httpHost));
            if (orRules.getRight()) {
                flag = !flag;
            }
            return flag;
        }) && andRules.getPorts().stream().allMatch(orRules -> {
            boolean flag = orRules.getLeft().stream().anyMatch(httpPort -> port == httpPort);
            if (orRules.getRight()) {
                flag = !flag;
            }
            return flag;
        }) && andRules.getMethods().stream().allMatch(orRules -> {
            boolean flag = orRules.getLeft().stream().anyMatch(httpMethod -> StringMatchUtil.matchStr(method, httpMethod));
            if (orRules.getRight()) {
                flag = !flag;
            }
            return flag;
        }) && andRules.getPaths().stream().allMatch(orRules -> {
            boolean flag = orRules.getLeft().stream().anyMatch(httpPath -> StringMatchUtil.matchStr(path, httpPath));
            if (orRules.getRight()) {
                flag = !flag;
            }
            return flag;
        }));
    }

    public static void addTargetRules(TargetRule targetRule, boolean isAllow) {
        if (isAllow) {
            allowTargetRules.put(targetRule.getName(), targetRule);
        } else {
            denyTargetRules.put(targetRule.getName(), targetRule);
        }
    }

    public static void clear() {
        allowTargetRules.clear();
        denyTargetRules.clear();
    }
}
