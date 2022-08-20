package com.alibaba.cloud.governance.common.rules.auth;

import com.alibaba.cloud.governance.common.rules.AndRule;
import io.envoyproxy.envoy.type.matcher.v3.StringMatcher;

public class TargetRule {
    private final String name;
    private final AndRule<StringMatcher> hosts;
    private final AndRule<Integer> ports;
    private final AndRule<StringMatcher> methods;
    private final AndRule<StringMatcher> paths;


    public TargetRule(String name, AndRule<StringMatcher> hosts, AndRule<Integer> ports, AndRule<StringMatcher> methods, AndRule<StringMatcher> paths) {
        this.name = name;
        this.hosts = hosts;
        this.ports = ports;
        this.methods = methods;
        this.paths = paths;
    }

    public String getName() {
        return name;
    }

    public AndRule<StringMatcher> getHosts() {
        return hosts;
    }

    public AndRule<Integer> getPorts() {
        return ports;
    }

    public AndRule<StringMatcher> getMethods() {
        return methods;
    }

    public AndRule<StringMatcher> getPaths() {
        return paths;
    }
}
