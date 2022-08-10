package com.alibaba.cloud.istio.rules.auth;

import io.envoyproxy.envoy.config.core.v3.CidrRange;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

public class IpBlockRule {
    private String name;
    private List<Pair<List<CidrRange>, Boolean>> sourceIps;
    private List<Pair<List<CidrRange>, Boolean>> remoteIps;
    private List<Pair<List<CidrRange>, Boolean>> destIps;

    public IpBlockRule(String name, List<Pair<List<CidrRange>, Boolean>> sourceIps, List<Pair<List<CidrRange>, Boolean>> remoteIps, List<Pair<List<CidrRange>, Boolean>> destIps) {
        this(name, sourceIps, remoteIps);
        this.destIps = destIps;
    }

    public IpBlockRule(String name, List<Pair<List<CidrRange>, Boolean>> sourceIps, List<Pair<List<CidrRange>, Boolean>> remoteIps) {
        this.name = name;
        this.sourceIps = sourceIps;
        this.remoteIps = remoteIps;
    }

    public List<Pair<List<CidrRange>, Boolean>> getSourceIps() {
        return sourceIps;
    }

    public List<Pair<List<CidrRange>, Boolean>> getRemoteIps() {
        return remoteIps;
    }

    public List<Pair<List<CidrRange>, Boolean>> getDestIps() {
        return destIps;
    }

    public void setDestIps(List<Pair<List<CidrRange>, Boolean>> destIps) {
        this.destIps = destIps;
    }

    public String getName() {
        return name;
    }
}
