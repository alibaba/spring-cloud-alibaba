package com.alibaba.cloud.istio.rules.manager;

import com.alibaba.cloud.istio.rules.auth.JwtAuthRule;
import com.alibaba.cloud.istio.util.StringMatchUtil;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.MalformedClaimException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class JwtAuthRuleManager {
    private static final Logger log = LoggerFactory.getLogger(JwtAuthRuleManager.class);
    private static Map<String, JwtAuthRule> allowJwtAuthRules = new ConcurrentHashMap<>();
    private static Map<String, JwtAuthRule> denyJwtAuthRules = new ConcurrentHashMap<>();

    public static void addJwtAuthRule(JwtAuthRule jwtAuthRule, boolean isAllow) {
        if (isAllow) {
            allowJwtAuthRules.put(jwtAuthRule.getName(), jwtAuthRule);
        } else {
            denyJwtAuthRules.put(jwtAuthRule.getName(), jwtAuthRule);
        }
    }

    public static void clear() {
        allowJwtAuthRules.clear();
        denyJwtAuthRules.clear();
    }

    public static boolean isValid(JwtClaims jwtClaims) {
        try {
            if (!denyJwtAuthRules.isEmpty() && judgeJwt(denyJwtAuthRules, jwtClaims)) {
                return false;
            }
            if (allowJwtAuthRules.isEmpty()) {
                return true;
            }
            return judgeJwt(allowJwtAuthRules, jwtClaims);
        } catch (MalformedClaimException e) {
            log.error("error on judge jwt", e);
            return false;
        }
    }

    private static boolean judgeJwt(Map<String, JwtAuthRule> rules, JwtClaims jwtClaims) throws MalformedClaimException {
        List<String> audiences = jwtClaims.getAudience();
        String issuer = jwtClaims.getIssuer();
        String subject = jwtClaims.getSubject();
        String presenter = jwtClaims.getClaimValueAsString("azp");
        return rules.values().stream().allMatch(andRules -> andRules.getAuthPresenters().stream().allMatch(orRules -> {
            boolean flag = orRules.getLeft().stream().anyMatch(jwtPresenter -> StringMatchUtil.matchStr(presenter, jwtPresenter));
            if (orRules.getRight()) {
                flag = !flag;
            }
            return flag;
        }) && andRules.getRequestPrincipals().stream().allMatch(orRules -> {
            boolean flag = orRules.getLeft().stream().anyMatch(jwtPrincipal -> StringMatchUtil.matchStr(issuer + "/" + subject, jwtPrincipal));
            if (orRules.getRight()) {
                flag = !flag;
            }
            return flag;
        }) && andRules.getAuthAudiences().stream().allMatch(orRules -> {
            boolean flag = audiences.stream().anyMatch(audStr -> orRules.getLeft().stream().anyMatch(jwtAudience -> StringMatchUtil.matchStr(audStr, jwtAudience)));
            if (orRules.getRight()) {
                flag = !flag;
            }
            return flag;
        }) && andRules.getAuthClaims().entrySet().stream().allMatch(allHeader -> {
            String key = allHeader.getKey();
            String claimValue;
            try {
                claimValue = jwtClaims.getStringClaimValue(key);
            } catch (MalformedClaimException e) {
                return false;
            }
            return allHeader.getValue().stream().allMatch(andRule -> {
                // only support string header
                boolean flag = andRule.getLeft().stream().anyMatch(orRule -> orRule.hasOneOf() && orRule.getOneOf().hasStringMatch() && StringMatchUtil.matchStr(claimValue, orRule.getOneOf().getStringMatch()));
                if (andRule.getRight()) {
                    flag = !flag;
                }
                return flag;
            });
        }));
    }
}
