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

package com.alibaba.alicloud.acm.bootstrap;

import com.alibaba.alicloud.context.acm.AcmIntegrationProperties;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.bootstrap.config.PropertySourceLocator;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertySource;

/**
 * @author juven.xuxb
 * @author xiaolongzuo
 */
@ConditionalOnProperty(name = "spring.cloud.alicloud.acm.enabled", matchIfMissing = true)
public class AcmPropertySourceLocator implements PropertySourceLocator {

	private static final String DIAMOND_PROPERTY_SOURCE_NAME = "diamond";

	private AcmPropertySourceBuilder acmPropertySourceBuilder = new AcmPropertySourceBuilder();

	@Autowired
	private AcmIntegrationProperties acmIntegrationProperties;

	@Override
	public PropertySource<?> locate(Environment environment) {

		CompositePropertySource compositePropertySource = new CompositePropertySource(
				DIAMOND_PROPERTY_SOURCE_NAME);

		acmIntegrationProperties.setActiveProfiles(environment.getActiveProfiles());

		for (String dataId : acmIntegrationProperties.getGroupConfigurationDataIds()) {
			loadDiamondDataIfPresent(compositePropertySource, dataId,
					acmIntegrationProperties.getAcmProperties().getGroup(), true);
		}

		for (String dataId : acmIntegrationProperties
				.getApplicationConfigurationDataIds()) {
			loadDiamondDataIfPresent(compositePropertySource, dataId,
					acmIntegrationProperties.getAcmProperties().getGroup(), false);
		}

		return compositePropertySource;
	}

	private void loadDiamondDataIfPresent(final CompositePropertySource composite,
			final String dataId, final String diamondGroup, final boolean groupLevel) {
		AcmPropertySource ps = acmPropertySourceBuilder.build(dataId, diamondGroup,
				groupLevel);
		if (ps != null) {
			composite.addFirstPropertySource(ps);
		}
	}

}
