package com.alibaba.cloud.governance.common.rules.auth;

import com.alibaba.cloud.governance.common.rules.AndRule;
import io.envoyproxy.envoy.type.matcher.v3.ListMatcher;
import io.envoyproxy.envoy.type.matcher.v3.StringMatcher;

import java.util.Map;

public class JwtAuthRule {
    private String name;
    private AndRule<StringMatcher> requestPrincipals;
    private AndRule<StringMatcher> authAudiences;
    private Map<String, AndRule<ListMatcher>> authClaims;
    private AndRule<StringMatcher> authPresenters;

    public JwtAuthRule(String name, AndRule<StringMatcher> requestPrincipals, AndRule<StringMatcher> authAudiences, Map<String, AndRule<ListMatcher>> authClaims, AndRule<StringMatcher> authPresenters) {
        this.name = name;
        this.requestPrincipals = requestPrincipals;
        this.authAudiences = authAudiences;
        this.authClaims = authClaims;
        this.authPresenters = authPresenters;
    }

    public String getName() {
        return name;
    }

    public AndRule<StringMatcher> getRequestPrincipals() {
        return requestPrincipals;
    }

    public AndRule<StringMatcher> getAuthAudiences() {
        return authAudiences;
    }

    public Map<String, AndRule<ListMatcher>> getAuthClaims() {
        return authClaims;
    }

    public AndRule<StringMatcher> getAuthPresenters() {
        return authPresenters;
    }
}
