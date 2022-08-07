package com.alibaba.cloud.istio.rules.auth;

import io.envoyproxy.envoy.type.matcher.v3.ListMatcher;
import io.envoyproxy.envoy.type.matcher.v3.StringMatcher;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JwtAuthRule {
    private String name;
    private List<List<StringMatcher>> requestPrincipals = new ArrayList<>();
    private List<List<StringMatcher>> authAudiences = new ArrayList<>();
    private Map<String, List<List<ListMatcher>>> authClaims = new HashMap<>();
    private List<List<StringMatcher>> authPresenters = new ArrayList<>();

    public JwtAuthRule(String name, List<List<StringMatcher>> requestPrincipals, List<List<StringMatcher>> authAudiences, Map<String, List<List<ListMatcher>>> authClaims, List<List<StringMatcher>> authPresenters) {
        this.name = name;
        this.requestPrincipals = requestPrincipals;
        this.authAudiences = authAudiences;
        this.authClaims = authClaims;
        this.authPresenters = authPresenters;
    }

    public String getName() {
        return name;
    }

    public List<List<StringMatcher>> getRequestPrincipals() {
        return requestPrincipals;
    }

    public List<List<StringMatcher>> getAuthAudiences() {
        return authAudiences;
    }

    public Map<String, List<List<ListMatcher>>> getAuthClaims() {
        return authClaims;
    }

    public List<List<StringMatcher>> getAuthPresenters() {
        return authPresenters;
    }
}
