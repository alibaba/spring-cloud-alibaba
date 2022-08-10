package com.alibaba.cloud.istio.rules.auth;

import io.envoyproxy.envoy.type.matcher.v3.ListMatcher;
import io.envoyproxy.envoy.type.matcher.v3.StringMatcher;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JwtAuthRule {
    private String name;
    private List<Pair<List<StringMatcher>, Boolean>> requestPrincipals;
    private List<Pair<List<StringMatcher>, Boolean>> authAudiences;
    private Map<String, List<Pair<List<ListMatcher>, Boolean>>> authClaims;
    private List<Pair<List<StringMatcher>, Boolean>> authPresenters;

    public JwtAuthRule(String name, List<Pair<List<StringMatcher>, Boolean>> requestPrincipals, List<Pair<List<StringMatcher>, Boolean>> authAudiences, Map<String, List<Pair<List<ListMatcher>, Boolean>>> authClaims, List<Pair<List<StringMatcher>, Boolean>> authPresenters) {
        this.name = name;
        this.requestPrincipals = requestPrincipals;
        this.authAudiences = authAudiences;
        this.authClaims = authClaims;
        this.authPresenters = authPresenters;
    }

    public String getName() {
        return name;
    }

    public List<Pair<List<StringMatcher>, Boolean>> getRequestPrincipals() {
        return requestPrincipals;
    }

    public List<Pair<List<StringMatcher>, Boolean>> getAuthAudiences() {
        return authAudiences;
    }

    public Map<String, List<Pair<List<ListMatcher>, Boolean>>> getAuthClaims() {
        return authClaims;
    }

    public List<Pair<List<StringMatcher>, Boolean>> getAuthPresenters() {
        return authPresenters;
    }
}
