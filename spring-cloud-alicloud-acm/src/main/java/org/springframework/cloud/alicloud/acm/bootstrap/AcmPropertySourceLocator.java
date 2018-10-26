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

package org.springframework.cloud.alicloud.acm.bootstrap;

import com.taobao.diamond.maintenance.DiamondHealth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.alicloud.acm.diagnostics.analyzer.DiamondConnectionFailureException;
import org.springframework.cloud.alicloud.context.acm.AcmProperties;
import org.springframework.cloud.bootstrap.config.PropertySourceLocator;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertySource;
import org.springframework.util.StringUtils;

import static com.taobao.diamond.client.impl.ServerHttpAgent.addressPort;
import static com.taobao.diamond.client.impl.ServerHttpAgent.domainName;

/**
 * @author juven.xuxb
 * @author xiaolongzuo
 */
public class AcmPropertySourceLocator implements PropertySourceLocator {

	private final Logger logger = LoggerFactory.getLogger(AcmPropertySourceLocator.class);

	private static final String DIAMOND_PROPERTY_SOURCE_NAME = "diamond";

	private static String defaultDiamondGroup = "DEFAULT_GROUP";

	private AcmPropertySourceBuilder acmPropertySourceBuilder = new AcmPropertySourceBuilder();

	@Autowired
	private AcmProperties acmProperties;

	@Override
	public PropertySource<?> locate(Environment environment) {
		checkDiamondHealth();

		String applicationName = environment.getProperty("spring.application.name");
		logger.info("Initialize spring.application.name '" + applicationName + "'.");
		String applicationGroup = environment.getProperty("spring.application.group");

		if (StringUtils.isEmpty(applicationName)) {
			throw new IllegalStateException(
					"'spring.application.name' must be configured.");
		}

		CompositePropertySource compositePropertySource = new CompositePropertySource(
				DIAMOND_PROPERTY_SOURCE_NAME);

		loadGroupConfigurationRecursively(compositePropertySource, applicationGroup);

		loadApplicationConfiguration(compositePropertySource, environment,
				applicationGroup, applicationName);

		return compositePropertySource;
	}

	private void checkDiamondHealth() {
		logger.info("Checking ACM health");
		try {
			if (!"UP".equals(DiamondHealth.getHealth())) {
				throw new DiamondConnectionFailureException(domainName, addressPort,
						DiamondHealth.getHealth());
			}
		}
		catch (Throwable t) {
			throw new DiamondConnectionFailureException(domainName, addressPort,
					"ACM Health error", t);
		}
	}

	private void loadGroupConfigurationRecursively(
			CompositePropertySource compositePropertySource, String applicationGroup) {
		if (StringUtils.isEmpty(applicationGroup)) {
			return;
		}
		String[] parts = applicationGroup.split("\\.");
		for (int i = 1; i < parts.length; i++) {
			String subGroup = parts[0];
			for (int j = 1; j <= i; j++) {
				subGroup = subGroup + "." + parts[j];
			}
			String dataId = subGroup + ":application." + acmProperties.getFileExtension();
			loadDiamondDataIfPresent(compositePropertySource, dataId, defaultDiamondGroup,
					true);
		}
	}

	private void loadApplicationConfiguration(
			CompositePropertySource compositePropertySource, Environment environment,
			String applicationGroup, String applicationName) {

		if (!StringUtils.isEmpty(applicationGroup)) {
			String dataId = applicationGroup + ":" + applicationName + "."
					+ acmProperties.getFileExtension();
			loadDiamondDataIfPresent(compositePropertySource, dataId, defaultDiamondGroup,
					false);
			for (String profile : environment.getActiveProfiles()) {
				dataId = applicationGroup + ":" + applicationName + "-" + profile + "."
						+ acmProperties.getFileExtension();
				loadDiamondDataIfPresent(compositePropertySource, dataId,
						defaultDiamondGroup, false);
			}

		}
		String dataId = applicationName + "." + acmProperties.getFileExtension();
		loadDiamondDataIfPresent(compositePropertySource, dataId, defaultDiamondGroup,
				false);
		for (String profile : environment.getActiveProfiles()) {
			dataId = applicationName + "-" + profile + "."
					+ acmProperties.getFileExtension();
			loadDiamondDataIfPresent(compositePropertySource, dataId, defaultDiamondGroup,
					false);
		}
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
