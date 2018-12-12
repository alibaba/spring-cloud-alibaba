package org.springframework.cloud.alicloud.context.nacos;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;

import com.alibaba.cloud.context.edas.EdasChangeOrderConfiguration;
import com.alibaba.cloud.context.edas.EdasChangeOrderConfigurationFactory;

public class NacosParameterInitListener
		implements ApplicationListener<ApplicationEnvironmentPreparedEvent> {
	private static final Logger log = LoggerFactory
			.getLogger(NacosParameterInitListener.class);

	@Override
	public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {

		preparedNacosConfiguration();
	}

	private void preparedNacosConfiguration() {
		EdasChangeOrderConfiguration edasChangeOrderConfiguration = EdasChangeOrderConfigurationFactory
				.getEdasChangeOrderConfiguration();
		log.info("Initialize Nacos Parameter from edas change order,is edas managed {}.",
				edasChangeOrderConfiguration.isEdasManaged());
		if (!edasChangeOrderConfiguration.isEdasManaged()) {
			return;
		}
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