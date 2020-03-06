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

import com.alibaba.alicloud.context.listener.AbstractOnceApplicationListener;
import com.alibaba.cloud.context.edas.EdasChangeOrderConfiguration;
import com.alibaba.cloud.context.edas.EdasChangeOrderConfigurationFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;

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
