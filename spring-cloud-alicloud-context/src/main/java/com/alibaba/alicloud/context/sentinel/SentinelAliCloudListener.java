/*
 * Copyright (C) 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.alicloud.context.sentinel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;

import com.alibaba.alicloud.context.Constants;
import com.alibaba.alicloud.context.listener.AbstractOnceApplicationListener;
import com.alibaba.cloud.context.edas.EdasChangeOrderConfiguration;
import com.alibaba.cloud.context.edas.EdasChangeOrderConfigurationFactory;

/**
 * @author <a href="mailto:fangjian0423@gmail.com">Jim</a>
 */
public class SentinelAliCloudListener
		extends AbstractOnceApplicationListener<ApplicationEnvironmentPreparedEvent> {

	private static final Logger logger = LoggerFactory
			.getLogger(SentinelAliCloudListener.class);

	@Override
	protected void handleEvent(ApplicationEnvironmentPreparedEvent event) {
		EdasChangeOrderConfiguration edasChangeOrderConfiguration = EdasChangeOrderConfigurationFactory
				.getEdasChangeOrderConfiguration();
		logger.info("Sentinel Nacos datasource will"
				+ (edasChangeOrderConfiguration.isEdasManaged() ? " be " : " not be ")
				+ "changed by edas change order.");
		if (!edasChangeOrderConfiguration.isEdasManaged()) {
			return;
		}
		System.getProperties().setProperty(Constants.Sentinel.NACOS_DATASOURCE_ENDPOINT,
				edasChangeOrderConfiguration.getAddressServerDomain());
		System.getProperties().setProperty(Constants.Sentinel.NACOS_DATASOURCE_NAMESPACE,
				edasChangeOrderConfiguration.getTenantId());
		System.getProperties().setProperty(Constants.Sentinel.NACOS_DATASOURCE_AK,
				edasChangeOrderConfiguration.getDauthAccessKey());
		System.getProperties().setProperty(Constants.Sentinel.NACOS_DATASOURCE_SK,
				edasChangeOrderConfiguration.getDauthSecretKey());
		System.getProperties().setProperty(Constants.Sentinel.PROJECT_NAME,
				edasChangeOrderConfiguration.getProjectName());
	}

	@Override
	protected String conditionalOnClass() {
		return "com.alibaba.csp.sentinel.datasource.nacos.NacosDataSource";
	}

}
