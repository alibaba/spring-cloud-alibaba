package com.alibaba.cloud.istio;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(XdsConfigProperties.PREFIX)
public class XdsConfigProperties {
    public static final String PREFIX = "spring.cloud.istio.config";
    private String host;
    private int port;
    private int pollingPoolSize;
    private int pollingTimeout;

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

    public int getPollingTimeout() {
        return pollingTimeout;
    }

    public void setPollingTimeout(int pollingTimeout) {
        this.pollingTimeout = pollingTimeout;
    }
}
