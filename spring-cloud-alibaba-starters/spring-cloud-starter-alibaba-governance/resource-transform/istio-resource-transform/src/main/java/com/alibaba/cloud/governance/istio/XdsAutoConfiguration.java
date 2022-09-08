package com.alibaba.cloud.governance.istio;

import com.alibaba.cloud.governance.istio.protocol.impl.CdsProtocol;
import com.alibaba.cloud.governance.istio.protocol.impl.EdsProtocol;
import com.alibaba.cloud.governance.istio.protocol.impl.LdsProtocol;
import com.alibaba.cloud.governance.istio.protocol.impl.RdsProtocol;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
	public XdsScheduledThreadPool xdsScheduledThreadPool() {
		return new XdsScheduledThreadPool(xdsConfigProperties);
	}

	@Bean
	public PilotExchanger pilotExchanger(LdsProtocol ldsProtocol, CdsProtocol cdsProtocol,
			EdsProtocol edsProtocol, RdsProtocol rdsProtocol) {
		return new PilotExchanger(ldsProtocol, cdsProtocol, edsProtocol, rdsProtocol);
	}

	@Bean
	public LdsProtocol ldsProtocol(XdsChannel xdsChannel,
			XdsScheduledThreadPool xdsScheduledThreadPool) {
		return new LdsProtocol(xdsChannel, xdsScheduledThreadPool,
				xdsConfigProperties.getPollingTime());
	}

	@Bean
	public CdsProtocol cdsProtocol(XdsChannel xdsChannel,
			XdsScheduledThreadPool xdsScheduledThreadPool) {
		return new CdsProtocol(xdsChannel, xdsScheduledThreadPool,
				xdsConfigProperties.getPollingTime());
	}

	@Bean
	EdsProtocol edsProtocol(XdsChannel xdsChannel,
			XdsScheduledThreadPool xdsScheduledThreadPool) {
		return new EdsProtocol(xdsChannel, xdsScheduledThreadPool,
				xdsConfigProperties.getPollingTime());
	}

	@Bean
	RdsProtocol rdsProtocol(XdsChannel xdsChannel,
			XdsScheduledThreadPool xdsScheduledThreadPool) {
		return new RdsProtocol(xdsChannel, xdsScheduledThreadPool,
				xdsConfigProperties.getPollingTime());
	}

}
