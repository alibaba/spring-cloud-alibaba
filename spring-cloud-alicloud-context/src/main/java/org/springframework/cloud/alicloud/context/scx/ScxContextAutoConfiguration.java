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

package org.springframework.cloud.alicloud.context.scx;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.alicloud.context.AliCloudProperties;
import org.springframework.cloud.alicloud.context.edas.EdasContextAutoConfiguration;
import org.springframework.cloud.alicloud.context.edas.EdasProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import com.alibaba.cloud.context.edas.AliCloudEdasSdk;
import com.alibaba.cloud.context.edas.EdasChangeOrderConfiguration;
import com.alibaba.cloud.context.edas.EdasChangeOrderConfigurationFactory;
import com.alibaba.dts.common.exception.InitException;
import com.alibaba.edas.schedulerx.SchedulerXClient;
import com.aliyuncs.edas.model.v20170801.GetSecureTokenResponse;

/**
 * @author xiaolongzuo
 */
@Configuration
@ConditionalOnClass(name = "org.springframework.cloud.alicloud.scx.ScxAutoConfiguration")
@EnableConfigurationProperties(ScxProperties.class)
@ImportAutoConfiguration(EdasContextAutoConfiguration.class)
public class ScxContextAutoConfiguration implements InitializingBean {

	private static final Logger log = LoggerFactory
			.getLogger(ScxContextAutoConfiguration.class);

	private static final String TEST_REGION = "cn-test";

	private static final String DEFAULT_KEY = "123456";

	@Autowired
	private SchedulerXClient schedulerXClient;

	@Bean
	public SchedulerXClient schedulerXClient(AliCloudProperties aliCloudProperties,
			EdasProperties edasProperties, ScxProperties scxProperties,
			AliCloudEdasSdk aliCloudEdasSdk) {
		Assert.isTrue(!StringUtils.isEmpty(scxProperties.getGroupId()),
				"${spring.cloud.alicloud.scx.group-id} can't be null.");
		SchedulerXClient schedulerXClient = new SchedulerXClient();
		schedulerXClient.setGroupId(scxProperties.getGroupId());

		EdasChangeOrderConfiguration edasChangeOrderConfiguration = EdasChangeOrderConfigurationFactory
				.getEdasChangeOrderConfiguration();
		if (edasChangeOrderConfiguration.isEdasManaged()) {
			if (edasChangeOrderConfiguration.getRegionId() != null) {
				schedulerXClient
						.setRegionName(edasChangeOrderConfiguration.getRegionId());
			}
			else {
				Assert.isTrue(!StringUtils.isEmpty(edasProperties.getNamespace()),
						"${spring.cloud.alicloud.edas.namespace} can't be null.");
				schedulerXClient.setRegionName(edasProperties.getRegionId());
			}
			schedulerXClient.setDomainName(scxProperties.getDomainName());
			schedulerXClient
					.setAccessKey(edasChangeOrderConfiguration.getDauthAccessKey());
			schedulerXClient
					.setSecretKey(edasChangeOrderConfiguration.getDauthSecretKey());
		}
		else if (TEST_REGION.equals(edasProperties.getNamespace())) {
			Assert.isTrue(!StringUtils.isEmpty(edasProperties.getNamespace()),
					"${spring.cloud.alicloud.edas.namespace} can't be null.");
			schedulerXClient.setRegionName(edasProperties.getNamespace());
			schedulerXClient.setAccessKey(DEFAULT_KEY);
			schedulerXClient.setSecretKey(DEFAULT_KEY);
		}
		else {
			Assert.isTrue(!StringUtils.isEmpty(edasProperties.getNamespace()),
					"${spring.cloud.alicloud.edas.namespace} can't be null.");
			Assert.isTrue(!StringUtils.isEmpty(aliCloudProperties.getAccessKey()),
					"${spring.cloud.alicloud.access-key} can't be empty.");
			Assert.isTrue(!StringUtils.isEmpty(aliCloudProperties.getSecretKey()),
					"${spring.cloud.alicloud.secret-key} can't be empty.");
			GetSecureTokenResponse.SecureToken secureToken = aliCloudEdasSdk
					.getSecureToken(edasProperties.getNamespace());
			schedulerXClient.setRegionName(edasProperties.getRegionId());
			schedulerXClient.setDomainName(scxProperties.getDomainName());
			schedulerXClient.setAccessKey(secureToken.getAccessKey());
			schedulerXClient.setSecretKey(secureToken.getSecretKey());
		}
		return schedulerXClient;
	}

	@Override
	public void afterPropertiesSet() {
		try {
			schedulerXClient.init();
		}
		catch (InitException e) {
			log.error("Init SchedulerX failed.", e);
			throw new RuntimeException(e);
		}
	}
}
