package com.alibaba.cloud.istio.rules.auth;

import io.envoyproxy.envoy.type.matcher.v3.StringMatcher;

import java.util.ArrayList;
import java.util.List;

public class IdentityRule {
    private String name;
    // in k8s, it is service account
    private List<List<StringMatcher>> identities = new ArrayList<>();

    public List<List<StringMatcher>> getIdentities() {
        return identities;
    }

    public IdentityRule(String name, List<List<StringMatcher>> identities) {
        this.name = name;
        this.identities = identities;
    }

    public String getName() {
        return name;
    }
}
