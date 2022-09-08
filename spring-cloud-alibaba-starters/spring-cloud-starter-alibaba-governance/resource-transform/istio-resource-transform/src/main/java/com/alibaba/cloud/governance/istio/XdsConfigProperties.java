package com.alibaba.cloud.governance.istio;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(XdsConfigProperties.PREFIX)
public class XdsConfigProperties {

	public static final String PREFIX = "spring.cloud.istio.config";

	private String host;

	private int port;

	private int pollingPoolSize;

	private int pollingTime;

	private boolean secure;

	private String caCert;

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public int getPollingPoolSize() {
		return pollingPoolSize;
	}

	public void setPollingPoolSize(int pollingPoolSize) {
		this.pollingPoolSize = pollingPoolSize;
	}

	public int getPollingTime() {
		return pollingTime;
	}

	public void setPollingTime(int pollingTime) {
		this.pollingTime = pollingTime;
	}

	public boolean isSecure() {
		return secure;
	}

	public void setSecure(boolean secure) {
		this.secure = secure;
	}

	public String getCaCert() {
		return caCert;
	}

	public void setCaCert(String caCert) {
		this.caCert = caCert;
	}

}
