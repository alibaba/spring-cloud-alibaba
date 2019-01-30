package org.springframework.cloud.alicloud.ans.migrate;

import com.netflix.loadbalancer.Server;

import java.util.concurrent.atomic.AtomicLong;

public class ServerWrapper {

	private Server server;
	private AtomicLong callCount;

	public ServerWrapper() {
	}

	public ServerWrapper(Server server, AtomicLong callCount) {
		this.server = server;
		this.callCount = callCount;
	}

	public Server getServer() {
		return server;
	}

	public void setServer(Server server) {
		this.server = server;
	}

	public AtomicLong getCallCount() {
		return callCount;
	}

	public void setCallCount(AtomicLong callCount) {
		this.callCount = callCount;
	}
}