package com.alibaba.cloud.governance.istio;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.alibaba.cloud.governance.istio.protocol.impl.LdsProtocol;

@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(name = "spring.cloud.istio.config.enabled", matchIfMissing = true)
@EnableConfigurationProperties(XdsConfigProperties.class)
public class XdsAutoConfiguration {
    @Autowired
    private XdsConfigProperties xdsConfigProperties;
    @Bean
    public XdsChannel xdsChannel() {
        return new XdsChannel(xdsConfigProperties);
    }
    @Bean
    public PilotExchanger pilotExchanger(LdsProtocol ldsProtocol) {
        return new PilotExchanger(ldsProtocol);
    }
    @Bean
    public LdsProtocol ldsProtocol(XdsChannel xdsChannel) {
        return new LdsProtocol(xdsChannel, xdsConfigProperties);
    }
}
