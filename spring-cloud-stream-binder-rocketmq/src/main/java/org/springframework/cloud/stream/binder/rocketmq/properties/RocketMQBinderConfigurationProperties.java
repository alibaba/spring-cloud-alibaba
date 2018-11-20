package org.springframework.cloud.stream.binder.rocketmq.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Timur Valiev
 * @author <a href="mailto:fangjian0423@gmail.com">Jim</a>
 */
@ConfigurationProperties(prefix = "spring.cloud.stream.rocketmq.binder")
public class RocketMQBinderConfigurationProperties {

	private String namesrvAddr;

	private String logLevel = "ERROR";

	public String getNamesrvAddr() {
		return namesrvAddr;
	}

	public void setNamesrvAddr(String namesrvAddr) {
		this.namesrvAddr = namesrvAddr;
	}

	public String getLogLevel() {
		return logLevel;
	}

	public void setLogLevel(String logLevel) {
		this.logLevel = logLevel;
	}

}
