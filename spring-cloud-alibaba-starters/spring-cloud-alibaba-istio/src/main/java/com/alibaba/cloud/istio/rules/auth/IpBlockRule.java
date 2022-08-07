package com.alibaba.cloud.istio.rules.auth;

import io.envoyproxy.envoy.config.core.v3.CidrRange;

import java.util.List;

public class IpBlockRule {
    private String name;
    private List<List<CidrRange>> sourceIps;
    private List<List<CidrRange>> remoteIps;
    private List<List<CidrRange>> destIps;

    public IpBlockRule(String name, List<List<CidrRange>> sourceIps, List<List<CidrRange>> remoteIps) {
        this.name = name;
        this.remoteIps = remoteIps;
        this.sourceIps = sourceIps;
    }

    public IpBlockRule(String name, List<List<CidrRange>> sourceIps, List<List<CidrRange>> remoteIps, List<List<CidrRange>> destIps) {
        this(name, sourceIps, remoteIps);
        this.destIps = destIps;
    }

    public List<List<CidrRange>> getRemoteIps() {
        return remoteIps;
    }

    public List<List<CidrRange>> getDestIps() {
        return destIps;
    }

    public List<List<CidrRange>> getSourceIps() {
        return sourceIps;
    }

    public void setDestIps(List<List<CidrRange>> destIps) {
        this.destIps = destIps;
    }

    public String getName() {
        return name;
    }
}
