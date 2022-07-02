package com.alibaba.cloud.nacos.intetuntil;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.commons.util.InetUtilsProperties;
import org.springframework.context.annotation.Bean;

@EnableAutoConfiguration
public class UtilIPAutoConfiguration{
    public UtilIPAutoConfiguration() {
    }

    @Bean
    @ConditionalOnMissingBean
    public InetIPUtils inetUtils(InetUtilsProperties properties) {
        return new InetIPUtils(properties);
    }
}

