package com.alibaba.cloud.nacos.intetutil;

import com.alibaba.cloud.nacos.ConditionalOnNacosDiscoveryEnabled;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.client.ConditionalOnDiscoveryEnabled;
import org.springframework.cloud.commons.util.InetUtilsProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author HH
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnDiscoveryEnabled
@ConditionalOnNacosDiscoveryEnabled
public class UtilIPv6AutoConfiguration {

	public UtilIPv6AutoConfiguration() {
	}

	@Bean
	@ConditionalOnMissingBean
	public InetIPv6Util inetUtils(InetUtilsProperties properties) {
		return new InetIPv6Util(properties);
	}

}
