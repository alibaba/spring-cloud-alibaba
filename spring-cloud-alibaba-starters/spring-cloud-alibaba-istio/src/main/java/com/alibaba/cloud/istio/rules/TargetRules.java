package com.alibaba.cloud.istio.rules;

import io.envoyproxy.envoy.config.core.v3.CidrRange;
import io.envoyproxy.envoy.config.rbac.v3.Permission;
import io.envoyproxy.envoy.type.matcher.v3.StringMatcher;

import java.util.ArrayList;
import java.util.List;

/**
 * operation for istio
 */
public class TargetRules {
    private final List<List<CidrRange>> HOSTS = new ArrayList<>();
    private final List<List<CidrRange>> IPS = new ArrayList<>();
    private final List<List<Integer>> PORTS = new ArrayList<>();
    private final List<List<String>> METHODS = new ArrayList<>();
    private final List<List<StringMatcher>> PATHS = new ArrayList<>();
    private boolean isAny;
    public boolean isAny() {
        return isAny;
    }

    private static final String HEADER_NAME_METHOD = ":method";

    private final String NAME;

    public String getNAME() {
        return NAME;
    }


    @Override
    public String toString() {
        return "TargetRules{" +
                "HOSTS=" + HOSTS +
                ", IPS=" + IPS +
                ", PORTS=" + PORTS +
                ", METHODS=" + METHODS +
                ", PATHS=" + PATHS +
                ", NAME='" + NAME + '\'' +
                '}';
    }

    public TargetRules(String name, Permission permission, boolean isAllowed) {
        this.NAME = name;
        if (!isAllowed) {
            permission = permission.getNotRule();
        }
        Permission.Set andRules = permission.getAndRules();
        for (Permission andRule : andRules.getRulesList()) {
            if (andRule.getAny()) {
                isAny = true;
                return;
            }
            Permission.Set orRules = andRule.getOrRules();
            List<CidrRange> hosts = new ArrayList<>();
            List<Integer> ports = new ArrayList<>();
            List<String> methods = new ArrayList<>();
            List<StringMatcher> paths = new ArrayList<>();
            List<CidrRange> ips = new ArrayList<>();
            for (Permission orRule : orRules.getRulesList()) {
                if (orRule.hasDestinationIp()) {
                    hosts.add(orRule.getDestinationIp());
                }
                int port = orRule.getDestinationPort();
                if (port > 0 && port <= 65535) {
                    ports.add(port);
                }
                if (orRule.hasHeader() && orRule.getHeader().getExactMatch() != null && HEADER_NAME_METHOD.equals(orRule.getHeader().getName())) {
                    methods.add(orRule.getHeader().getExactMatch());
                }
                if (orRule.hasUrlPath() && orRule.getUrlPath().hasPath()) {
                    paths.add(orRule.getUrlPath().getPath());
                }
                if (orRule.hasDestinationIp()) {
                    ips.add(orRule.getDestinationIp());
                }

            }
            if (!hosts.isEmpty()) {
                HOSTS.add(hosts);
            }
            if (!ports.isEmpty()) {
                PORTS.add(ports);
            }
            if (!methods.isEmpty()) {
                METHODS.add(methods);
            }
            if (!paths.isEmpty()) {
                PATHS.add(paths);
            }
        }
    }
}
