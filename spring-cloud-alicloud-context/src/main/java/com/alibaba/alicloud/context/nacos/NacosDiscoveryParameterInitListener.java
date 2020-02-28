/*
 * Copyright 2013-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.alicloud.context.nacos;

import java.util.Properties;

import com.alibaba.alicloud.context.listener.AbstractOnceApplicationListener;
import com.alibaba.cloud.context.edas.EdasChangeOrderConfiguration;
import com.alibaba.cloud.context.edas.EdasChangeOrderConfigurationFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;

/**
 * @author pbting
 * @date 2019-02-14 11:12 AM
 */
public class NacosDiscoveryParameterInitListener
		extends AbstractOnceApplicationListener<ApplicationEnvironmentPreparedEvent> {

	private static final Logger log = LoggerFactory
			.getLogger(NacosDiscoveryParameterInitListener.class);

	@Override
	protected String conditionalOnClass() {
		return "com.alibaba.cloud.nacos.NacosDiscoveryAutoConfiguration";
	}

	@Override
	protected void handleEvent(ApplicationEnvironmentPreparedEvent event) {
		EdasChangeOrderConfiguration edasChangeOrderConfiguration = EdasChangeOrderConfigurationFactory
				.getEdasChangeOrderConfiguration();

		if (log.isDebugEnabled()) {
			log.debug("Initialize Nacos Discovery Parameter ,is managed {}.",
					edasChangeOrderConfiguration.isEdasManaged());
		}

		if (!edasChangeOrderConfiguration.isEdasManaged()) {
			return;
		}
		// initialize nacos configuration
		Properties properties = System.getProperties();
		properties.setProperty("spring.cloud.nacos.discovery.server-mode", "EDAS");
		// step 1: set some properties for spring cloud alibaba nacos discovery
		properties.setProperty("spring.cloud.nacos.discovery.server-addr", "");
		properties.setProperty("spring.cloud.nacos.discovery.endpoint",
				edasChangeOrderConfiguration.getAddressServerDomain());
		properties.setProperty("spring.cloud.nacos.discovery.namespace",
				edasChangeOrderConfiguration.getTenantId());
		properties.setProperty("spring.cloud.nacos.discovery.access-key",
				edasChangeOrderConfiguration.getDauthAccessKey());
		properties.setProperty("spring.cloud.nacos.discovery.secret-key",
				edasChangeOrderConfiguration.getDauthSecretKey());

		// step 2: set these properties for nacos client
		properties.setProperty("nacos.naming.web.context", "/vipserver");
		properties.setProperty("nacos.naming.exposed.port", "80");
	}

}
