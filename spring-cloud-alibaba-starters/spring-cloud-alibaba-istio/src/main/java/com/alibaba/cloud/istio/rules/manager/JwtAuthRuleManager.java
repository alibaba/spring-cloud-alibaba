package com.alibaba.cloud.istio.rules.manager;

import com.alibaba.cloud.istio.rules.auth.JwtAuthRule;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class JwtAuthRuleManager {
    private static Map<String, JwtAuthRule> jwtAuthRules = new ConcurrentHashMap<>();
    private static Map<String, JwtAuthRule> notJwtAuthRules = new ConcurrentHashMap<>();
    public static void addJwtAuthRule(JwtAuthRule jwtAuthRule, boolean isAllow) {
        if (isAllow) {
            jwtAuthRules.put(jwtAuthRule.getName(), jwtAuthRule);
        } else {
            notJwtAuthRules.put(jwtAuthRule.getName(), jwtAuthRule);
        }
    }
    public static void clear() {
        jwtAuthRules.clear();
        notJwtAuthRules.clear();
    }
}
