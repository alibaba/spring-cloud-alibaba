package com.alibaba.cloud.istio.rules.auth;

import io.envoyproxy.envoy.type.matcher.v3.StringMatcher;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

public class TargetRule {
    private final String name;
    private final List<Pair<List<StringMatcher>, Boolean>> hosts;
    private final List<Pair<List<Integer>, Boolean>> ports;
    private final List<Pair<List<StringMatcher>, Boolean>> methods;
    private final List<Pair<List<StringMatcher>, Boolean>> paths;


    public TargetRule(String name, List<Pair<List<StringMatcher>, Boolean>> hosts, List<Pair<List<Integer>, Boolean>> ports, List<Pair<List<StringMatcher>, Boolean>> methods, List<Pair<List<StringMatcher>, Boolean>> paths) {
        this.name = name;
        this.hosts = hosts;
        this.ports = ports;
        this.methods = methods;
        this.paths = paths;
    }

    public String getName() {
        return name;
    }

    public List<Pair<List<StringMatcher>, Boolean>> getHosts() {
        return hosts;
    }

    public List<Pair<List<Integer>, Boolean>> getPorts() {
        return ports;
    }

    public List<Pair<List<StringMatcher>, Boolean>> getMethods() {
        return methods;
    }

    public List<Pair<List<StringMatcher>, Boolean>> getPaths() {
        return paths;
    }
}
