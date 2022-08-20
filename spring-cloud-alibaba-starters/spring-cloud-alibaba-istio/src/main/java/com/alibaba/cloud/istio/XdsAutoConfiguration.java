package com.alibaba.cloud.istio;

import com.alibaba.cloud.istio.protocol.impl.LdsProtocol;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(name = "spring.cloud.istio.config.enabled")
public class XdsAutoConfiguration {
    @Bean
    public XdsConfigProperties xdsConfigProperties() {
        return new XdsConfigProperties();
    }
    @Bean
    public XdsChannel xdsChannel(XdsConfigProperties xdsConfigProperties) {
        return new XdsChannel(xdsConfigProperties);
    }
    @Bean
    public PilotExchanger pilotExchanger(LdsProtocol ldsProtocol) {
        return new PilotExchanger(ldsProtocol);
    }
    @Bean
    public LdsProtocol ldsProtocol(XdsChannel xdsChannel, XdsConfigProperties xdsConfigProperties) {
        return new LdsProtocol(xdsChannel, xdsConfigProperties);
    }
}
