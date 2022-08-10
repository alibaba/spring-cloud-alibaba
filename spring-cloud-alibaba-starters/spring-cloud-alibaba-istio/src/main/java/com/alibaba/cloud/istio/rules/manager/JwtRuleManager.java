package com.alibaba.cloud.istio.rules.manager;

import com.alibaba.cloud.istio.rules.auth.JwtRule;
import com.alibaba.cloud.istio.util.JwtUtil;
import org.apache.commons.lang3.StringUtils;
import org.jose4j.jwt.JwtClaims;
import org.springframework.http.HttpHeaders;
import org.springframework.util.MultiValueMap;

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

    public static JwtClaims isValid(MultiValueMap<String, String> params, HttpHeaders headers) {
        for (JwtRule rule : jwtRules.values()) {
            JwtClaims jwtClaims = JwtUtil.matchJwt(params, headers, rule);
            if (jwtClaims != null) {
                if (rule.isForwardOriginalToken()) {
                    // original token will be kept for upstream request
                }
                if (!StringUtils.isEmpty(rule.getOutputPayloadToHeader())) {
                    // output
                }
                return jwtClaims;
            }
        }
        return null;
    }
}
