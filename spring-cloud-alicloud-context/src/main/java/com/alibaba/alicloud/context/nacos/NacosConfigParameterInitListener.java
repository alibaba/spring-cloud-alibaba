package com.alibaba.alicloud.context.nacos;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;

import com.alibaba.alicloud.context.listener.AbstractOnceApplicationListener;
import com.alibaba.cloud.context.edas.EdasChangeOrderConfiguration;
import com.alibaba.cloud.context.edas.EdasChangeOrderConfigurationFactory;

/**
 * @author pbting
 */
public class NacosConfigParameterInitListener
		extends AbstractOnceApplicationListener<ApplicationEnvironmentPreparedEvent> {
	private static final Logger log = LoggerFactory
			.getLogger(NacosConfigParameterInitListener.class);

	@Override
	protected String conditionalOnClass() {
		return "com.alibaba.cloud.nacos.NacosConfigAutoConfiguration";
	}

	@Override
	protected void handleEvent(ApplicationEnvironmentPreparedEvent event) {
		preparedNacosConfiguration();
	}

	private void preparedNacosConfiguration() {
		EdasChangeOrderConfiguration edasChangeOrderConfiguration = EdasChangeOrderConfigurationFactory
				.getEdasChangeOrderConfiguration();

		if (log.isDebugEnabled()) {
			log.debug("Initialize Nacos Config Parameter ,is managed {}.",
					edasChangeOrderConfiguration.isEdasManaged());
		}

		if (!edasChangeOrderConfiguration.isEdasManaged()) {
			return;
		}

		System.getProperties().setProperty("spring.cloud.nacos.config.server-mode",
				"EDAS");
		// initialize nacos configuration
		System.getProperties().setProperty("spring.cloud.nacos.config.server-addr", "");
		System.getProperties().setProperty("spring.cloud.nacos.config.endpoint",
				edasChangeOrderConfiguration.getAddressServerDomain());
		System.getProperties().setProperty("spring.cloud.nacos.config.namespace",
				edasChangeOrderConfiguration.getTenantId());
		System.getProperties().setProperty("spring.cloud.nacos.config.access-key",
				edasChangeOrderConfiguration.getDauthAccessKey());
		System.getProperties().setProperty("spring.cloud.nacos.config.secret-key",
				edasChangeOrderConfiguration.getDauthSecretKey());
	}
}