package com.alibaba.cloud.istio.rules.manager;

import com.alibaba.cloud.istio.rules.auth.TargetRule;
import com.alibaba.cloud.istio.util.StringMatchUtil;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

public class TargetRuleManager {
    private static Map<String, TargetRule> targetRules = new ConcurrentHashMap<>();
    private static Map<String, TargetRule> notTargetRules = new ConcurrentHashMap<>();
    public static boolean isValid(String host, int port, String method, String path) {
        if (judgeTargetRule(notTargetRules, host, port, method, path)) {
            return false;
        }
        if (targetRules.isEmpty()) {
            return true;
        }
        return judgeTargetRule(targetRules, host, port, method, path);
    }
    private static boolean judgeTargetRule(Map<String, TargetRule> rules, String host, int port, String method, String path) {
        return rules.values().stream().allMatch(andRules -> andRules.getHosts().stream().allMatch(orRules -> orRules.stream().anyMatch(httpHost -> StringMatchUtil.matchStr(host, httpHost))) ||
                andRules.getPorts().stream().allMatch(orRules -> orRules.stream().anyMatch(httpPort -> httpPort == port)) ||
                andRules.getMethods().stream().allMatch(orRules -> orRules.stream().anyMatch(httpMethod -> StringMatchUtil.matchStr(method, httpMethod))) ||
                andRules.getPaths().stream().allMatch(orRules -> orRules.stream().anyMatch(httpPath -> StringMatchUtil.matchStr(path, httpPath))));
    }

    public static void addTargetRules(TargetRule targetRule, boolean isAllow) {
        if (isAllow) {
            targetRules.put(targetRule.getName(), targetRule);
        } else {
            notTargetRules.put(targetRule.getName(), targetRule);
        }
    }
    public static void clear() {
        targetRules.clear();
        notTargetRules.clear();
    }
}
