package com.alibaba.cloud.istio.rules.manager;

import com.alibaba.cloud.istio.rules.auth.JwtRule;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class JwtRuleManager {
    private static Map<String, JwtRule> jwtRules = new ConcurrentHashMap<>();
    public static void addJwtRule(JwtRule jwtRule) {
        jwtRules.put(jwtRule.getName(), jwtRule);
    }
    public static void clear() {
        jwtRules.clear();
    }
}
