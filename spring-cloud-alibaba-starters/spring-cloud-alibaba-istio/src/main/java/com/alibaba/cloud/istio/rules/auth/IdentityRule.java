package com.alibaba.cloud.istio.rules.auth;

import io.envoyproxy.envoy.type.matcher.v3.StringMatcher;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;

public class IdentityRule {
    private String name;
    // in k8s, it is service account
    private List<Pair<List<StringMatcher>, Boolean>> identities = new ArrayList<>();

    public IdentityRule(String name, List<Pair<List<StringMatcher>, Boolean>> identities) {
        this.name = name;
        this.identities = identities;
    }

    public List<Pair<List<StringMatcher>, Boolean>> getIdentities() {
        return identities;
    }

    public String getName() {
        return name;
    }
}
