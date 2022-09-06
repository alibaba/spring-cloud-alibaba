package com.alibaba.cloud.governance.auth.rules.auth;

import com.alibaba.cloud.governance.common.matcher.IpMatcher;
import com.alibaba.cloud.governance.common.rule.AndRule;

public class IpBlockRule {

	private String name;

	private AndRule<IpMatcher> sourceIps;

	private AndRule<IpMatcher> remoteIps;

	private AndRule<IpMatcher> destIps;

	public IpBlockRule(String name, AndRule<IpMatcher> sourceIps,
			AndRule<IpMatcher> remoteIps, AndRule<IpMatcher> destIps) {
		this(name, sourceIps, remoteIps);
		this.destIps = destIps;
	}

	public IpBlockRule(String name, AndRule<IpMatcher> sourceIps,
			AndRule<IpMatcher> remoteIps) {
		this.name = name;
		this.sourceIps = sourceIps;
		this.remoteIps = remoteIps;
	}

	public AndRule<IpMatcher> getSourceIps() {
		return sourceIps;
	}

	public AndRule<IpMatcher> getRemoteIps() {
		return remoteIps;
	}

	public AndRule<IpMatcher> getDestIps() {
		return destIps;
	}

	public void setDestIps(AndRule<IpMatcher> destIps) {
		this.destIps = destIps;
	}

	public String getName() {
		return name;
	}

}
