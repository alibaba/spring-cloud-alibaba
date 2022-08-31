package com.alibaba.cloud.governance.auth.rules.auth;

import com.alibaba.cloud.governance.auth.rules.AndRule;
import io.envoyproxy.envoy.config.core.v3.CidrRange;

public class IpBlockRule {

	private String name;

	private AndRule<CidrRange> sourceIps;

	private AndRule<CidrRange> remoteIps;

	private AndRule<CidrRange> destIps;

	public IpBlockRule(String name, AndRule<CidrRange> sourceIps,
			AndRule<CidrRange> remoteIps, AndRule<CidrRange> destIps) {
		this(name, sourceIps, remoteIps);
		this.destIps = destIps;
	}

	public IpBlockRule(String name, AndRule<CidrRange> sourceIps,
			AndRule<CidrRange> remoteIps) {
		this.name = name;
		this.sourceIps = sourceIps;
		this.remoteIps = remoteIps;
	}

	public AndRule<CidrRange> getSourceIps() {
		return sourceIps;
	}

	public AndRule<CidrRange> getRemoteIps() {
		return remoteIps;
	}

	public AndRule<CidrRange> getDestIps() {
		return destIps;
	}

	public void setDestIps(AndRule<CidrRange> destIps) {
		this.destIps = destIps;
	}

	public String getName() {
		return name;
	}

}
