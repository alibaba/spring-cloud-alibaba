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

package com.alibaba.cloud.nacos.client;

import java.util.List;

import com.alibaba.cloud.nacos.NacosConfigManager;
import com.alibaba.cloud.nacos.NacosConfigProperties;
import com.alibaba.cloud.nacos.NacosPropertySourceRepository;
import com.alibaba.cloud.nacos.parser.NacosDataParserHandler;
import com.alibaba.cloud.nacos.refresh.NacosContextRefresher;
import com.alibaba.nacos.api.config.ConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.cloud.bootstrap.config.PropertySourceLocator;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertySource;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

/**
 * @author xiaojing
 * @author pbting
 */
@Order(0)
public class NacosPropertySourceLocator implements PropertySourceLocator {

	private static final Logger log = LoggerFactory
			.getLogger(NacosPropertySourceLocator.class);

	private static final String NACOS_PROPERTY_SOURCE_NAME = "NACOS";

	private static final String SEP1 = "-";

	private static final String DOT = ".";

	private NacosPropertySourceBuilder nacosPropertySourceBuilder;

	private NacosConfigProperties nacosConfigProperties;

	private NacosConfigManager nacosConfigManager;

	/**
	 * recommend to use
	 * {@link NacosPropertySourceLocator#NacosPropertySourceLocator(com.alibaba.cloud.nacos.NacosConfigManager)}.
	 * @param nacosConfigProperties nacosConfigProperties
	 */
	@Deprecated
	public NacosPropertySourceLocator(NacosConfigProperties nacosConfigProperties) {
		this.nacosConfigProperties = nacosConfigProperties;
	}

	public NacosPropertySourceLocator(NacosConfigManager nacosConfigManager) {
		this.nacosConfigManager = nacosConfigManager;
		this.nacosConfigProperties = nacosConfigManager.getNacosConfigProperties();
	}

	@Override
	public PropertySource<?> locate(Environment env) {
		nacosConfigProperties.setEnvironment(env);
		ConfigService configService = nacosConfigManager.getConfigService();

		if (null == configService) {
			log.warn("no instance of config service found, can't load config from nacos");
			return null;
		}
		long timeout = nacosConfigProperties.getTimeout();
		nacosPropertySourceBuilder = new NacosPropertySourceBuilder(configService,
				timeout);
		String name = nacosConfigProperties.getName();

		String dataIdPrefix = nacosConfigProperties.getPrefix();
		if (StringUtils.isEmpty(dataIdPrefix)) {
			dataIdPrefix = name;
		}

		if (StringUtils.isEmpty(dataIdPrefix)) {
			dataIdPrefix = env.getProperty("spring.application.name");
		}

		CompositePropertySource composite = new CompositePropertySource(
				NACOS_PROPERTY_SOURCE_NAME);

		loadSharedConfiguration(composite);
		loadExtConfiguration(composite);
		loadApplicationConfiguration(composite, dataIdPrefix, nacosConfigProperties, env);

		return composite;
	}

	/**
	 * load shared configuration.
	 */
	private void loadSharedConfiguration(
			CompositePropertySource compositePropertySource) {
		List<NacosConfigProperties.Config> sharedConfigs = nacosConfigProperties
				.getSharedConfigs();
		if (!CollectionUtils.isEmpty(sharedConfigs)) {
			checkConfiguration(sharedConfigs, "shared-configs");
			loadNacosConfiguration(compositePropertySource, sharedConfigs);
		}
	}

	/**
	 * load extensional configuration.
	 */
	private void loadExtConfiguration(CompositePropertySource compositePropertySource) {
		List<NacosConfigProperties.Config> extConfigs = nacosConfigProperties
				.getExtensionConfigs();
		if (!CollectionUtils.isEmpty(extConfigs)) {
			checkConfiguration(extConfigs, "extension-configs");
			loadNacosConfiguration(compositePropertySource, extConfigs);
		}
	}

	/**
	 * load configuration of application.
	 */
	private void loadApplicationConfiguration(
			CompositePropertySource compositePropertySource, String dataIdPrefix,
			NacosConfigProperties properties, Environment environment) {
		String fileExtension = properties.getFileExtension();
		String nacosGroup = properties.getGroup();
		// load directly once by default
		loadNacosDataIfPresent(compositePropertySource, dataIdPrefix, nacosGroup,
				fileExtension, true);
		// load with suffix, which have a higher priority than the default
		loadNacosDataIfPresent(compositePropertySource,
				dataIdPrefix + DOT + fileExtension, nacosGroup, fileExtension, true);
		// Loaded with profile, which have a higher priority than the suffix
		for (String profile : environment.getActiveProfiles()) {
			String dataId = dataIdPrefix + SEP1 + profile + DOT + fileExtension;
			loadNacosDataIfPresent(compositePropertySource, dataId, nacosGroup,
					fileExtension, true);
		}

	}

	private void loadNacosConfiguration(final CompositePropertySource composite,
			List<NacosConfigProperties.Config> configs) {
		for (NacosConfigProperties.Config config : configs) {
			String dataId = config.getDataId();
			String fileExtension = dataId.substring(dataId.lastIndexOf(DOT) + 1);
			loadNacosDataIfPresent(composite, dataId, config.getGroup(), fileExtension,
					config.isRefresh());
		}
	}

	private void checkConfiguration(List<NacosConfigProperties.Config> configs,
			String tips) {
		String[] dataIds = new String[configs.size()];
		for (int i = 0; i < configs.size(); i++) {
			String dataId = configs.get(i).getDataId();
			if (dataId == null || dataId.trim().length() == 0) {
				throw new IllegalStateException(String.format(
						"the [ spring.cloud.nacos.config.%s[%s] ] must give a dataId",
						tips, i));
			}
			dataIds[i] = dataId;
		}
		// Just decide that the current dataId must have a suffix
		NacosDataParserHandler.getInstance().checkDataId(dataIds);
	}

	private void loadNacosDataIfPresent(final CompositePropertySource composite,
			final String dataId, final String group, String fileExtension,
			boolean isRefreshable) {
		if (null == dataId || dataId.trim().length() < 1) {
			return;
		}
		if (null == group || group.trim().length() < 1) {
			return;
		}
		NacosPropertySource propertySource = this.loadNacosPropertySource(dataId, group,
				fileExtension, isRefreshable);
		this.addFirstPropertySource(composite, propertySource, false);
	}

	private NacosPropertySource loadNacosPropertySource(final String dataId,
			final String group, String fileExtension, boolean isRefreshable) {
		if (NacosContextRefresher.getRefreshCount() != 0) {
			if (!isRefreshable) {
				return NacosPropertySourceRepository.getNacosPropertySource(dataId,
						group);
			}
		}
		return nacosPropertySourceBuilder.build(dataId, group, fileExtension,
				isRefreshable);
	}

	/**
	 * Add the nacos configuration to the first place and maybe ignore the empty
	 * configuration.
	 */
	private void addFirstPropertySource(final CompositePropertySource composite,
			NacosPropertySource nacosPropertySource, boolean ignoreEmpty) {
		if (null == nacosPropertySource || null == composite) {
			return;
		}
		if (ignoreEmpty && nacosPropertySource.getSource().isEmpty()) {
			return;
		}
		composite.addFirstPropertySource(nacosPropertySource);
	}

	public void setNacosConfigManager(NacosConfigManager nacosConfigManager) {
		this.nacosConfigManager = nacosConfigManager;
	}

}
