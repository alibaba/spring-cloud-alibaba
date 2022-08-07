package com.alibaba.cloud.istio.rules.manager;

import com.alibaba.cloud.istio.rules.auth.IdentityRule;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class IdentityRuleManager {
    private static Map<String, IdentityRule> identityRules = new ConcurrentHashMap<>();
    private static Map<String, IdentityRule> notIdentityRules = new ConcurrentHashMap<>();
    public static void addIdentityRule(IdentityRule rule, boolean isAllow) {
        if (isAllow) {
            identityRules.put(rule.getName(), rule);
        } else {
            notIdentityRules.put(rule.getName(), rule);
        }
    }
    public static void clear() {
        identityRules.clear();
        notIdentityRules.clear();
    }
}
