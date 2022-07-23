package com.alibaba.cloud.istio;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(name = "spring.cloud.istio.config.enabled", matchIfMissing = true)
public class XdsAutoConfiguration {
    @Autowired
    private XdsConfigProperties xdsConfigProperties;
    @Bean
    public XdsChannel xdsChannel() {
        return new XdsChannel(xdsConfigProperties);
    }
}
