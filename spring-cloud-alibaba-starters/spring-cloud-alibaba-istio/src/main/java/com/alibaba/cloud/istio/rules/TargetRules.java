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
    private final List<List<StringMatcher>> METHODS = new ArrayList<>();
    private final List<List<StringMatcher>> PATHS = new ArrayList<>();

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
            Permission.Set orRules = andRule.getOrRules();
            List<CidrRange> hosts = new ArrayList<>();
            List<Integer> ports = new ArrayList<>();
            List<StringMatcher> methods = new ArrayList<>();
            List<StringMatcher> paths = new ArrayList<>();
            List<CidrRange> ips = new ArrayList<>();
            for (Permission orRule : orRules.getRulesList()) {
                if (orRule.hasDestinationIp()) {
                    hosts.add(orRule.getDestinationIp());
                }
                int port = orRule.getDestinationPort();
                if (port >= 0 && port <= 65535) {
                    ports.add(port);
                }
                if (orRule.hasHeader() && orRule.getHeader().hasStringMatch() && HEADER_NAME_METHOD.equals(orRule.getHeader().getName())) {
                    methods.add(orRule.getHeader().getStringMatch());
                }
                if (orRule.hasUrlPath() && orRule.getUrlPath().hasPath()) {
                    paths.add(orRule.getUrlPath().getPath());
                }
                if (orRule.hasDestinationIp()) {
                    ips.add(orRule.getDestinationIp());
                }

            }
            HOSTS.add(hosts);
            PORTS.add(ports);
            METHODS.add(methods);
            PATHS.add(paths);
        }


    }
}
