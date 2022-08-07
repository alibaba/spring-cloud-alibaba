package com.alibaba.cloud.istio.rules.auth;

import io.envoyproxy.envoy.type.matcher.v3.StringMatcher;

import java.util.List;

public class TargetRule {
    private final String name;
    private final List<List<StringMatcher>> hosts;
    private final List<List<Integer>> ports;
    private final List<List<StringMatcher>> methods;
    private final List<List<StringMatcher>> paths;


    public TargetRule(String name, List<List<StringMatcher>> hosts, List<List<Integer>> ports, List<List<StringMatcher>> methods, List<List<StringMatcher>> paths) {
        this.name = name;
        this.hosts = hosts;
        this.ports = ports;
        this.methods = methods;
        this.paths = paths;
    }

    public String getName() {
        return name;
    }

    public List<List<StringMatcher>> getHosts() {
        return hosts;
    }

    public List<List<Integer>> getPorts() {
        return ports;
    }

    public List<List<StringMatcher>> getMethods() {
        return methods;
    }

    public List<List<StringMatcher>> getPaths() {
        return paths;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TargetRule)) {
            return false;
        }

        TargetRule that = (TargetRule) o;

        return name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
