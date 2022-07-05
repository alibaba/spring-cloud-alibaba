package com.alibaba.cloud.nacos.intetuntil;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.commons.util.InetUtilsProperties;
import org.springframework.context.annotation.Bean;

@EnableAutoConfiguration
public class UtilIPv6AutoConfiguration {
    public UtilIPv6AutoConfiguration() {
    }

    @Bean
    @ConditionalOnMissingBean
    public InetIPv6Utils inetUtils(InetUtilsProperties properties) {
        return new InetIPv6Utils(properties);
    }
}

