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

package com.alibaba.cloud.nacos.client;

import java.util.List;

import com.alibaba.cloud.nacos.NacosConfigManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.bootstrap.config.PropertySourceLocator;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertySource;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.alibaba.cloud.nacos.NacosConfigProperties;
import com.alibaba.cloud.nacos.NacosPropertySourceRepository;
import com.alibaba.cloud.nacos.parser.NacosDataParserHandler;
import com.alibaba.cloud.nacos.refresh.NacosContextRefresher;
import com.alibaba.nacos.api.config.ConfigService;

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
	private static final String SHARED_CONFIG_SEPARATOR_CHAR = "[,]";

	private NacosPropertySourceBuilder nacosPropertySourceBuilder;

	private NacosConfigProperties nacosConfigProperties;

	private NacosConfigManager nacosConfigManager;

	public NacosPropertySourceLocator(NacosConfigManager nacosConfigManager,
			NacosConfigProperties nacosConfigProperties) {
		this.nacosConfigManager = nacosConfigManager;
		this.nacosConfigProperties = nacosConfigProperties;
	}

	@Override
	public PropertySource<?> locate(Environment env) {

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

	private void loadSharedConfiguration(
			CompositePropertySource compositePropertySource) {
		String sharedDataIds = nacosConfigProperties.getSharedDataids();
		String refreshDataIds = nacosConfigProperties.getRefreshableDataids();

		if (sharedDataIds == null || sharedDataIds.trim().length() == 0) {
			return;
		}

		String[] sharedDataIdArray = sharedDataIds.split(SHARED_CONFIG_SEPARATOR_CHAR);
		checkDataIdFileExtension(sharedDataIdArray);

		for (int i = 0; i < sharedDataIdArray.length; i++) {
			String dataId = sharedDataIdArray[i];
			String fileExtension = dataId.substring(dataId.lastIndexOf(".") + 1);
			boolean isRefreshable = checkDataIdIsRefreshable(refreshDataIds,
					sharedDataIdArray[i]);

			loadNacosDataIfPresent(compositePropertySource, dataId, "DEFAULT_GROUP",
					fileExtension, isRefreshable);
		}
	}

	private void loadExtConfiguration(CompositePropertySource compositePropertySource) {
		List<NacosConfigProperties.Config> extConfigs = nacosConfigProperties
				.getExtConfig();

		if (CollectionUtils.isEmpty(extConfigs)) {
			return;
		}

		checkExtConfiguration(extConfigs);

		for (NacosConfigProperties.Config config : extConfigs) {
			String dataId = config.getDataId();
			String fileExtension = dataId.substring(dataId.lastIndexOf(DOT) + 1);
			loadNacosDataIfPresent(compositePropertySource, dataId, config.getGroup(),
					fileExtension, config.isRefresh());
		}
	}

	private void checkExtConfiguration(List<NacosConfigProperties.Config> extConfigs) {
		String[] dataIds = new String[extConfigs.size()];
		for (int i = 0; i < extConfigs.size(); i++) {
			String dataId = extConfigs.get(i).getDataId();
			if (dataId == null || dataId.trim().length() == 0) {
				throw new IllegalStateException(String.format(
						"the [ spring.cloud.nacos.config.ext-config[%s] ] must give a dataId",
						i));
			}
			dataIds[i] = dataId;
		}
		checkDataIdFileExtension(dataIds);
	}

	private void loadApplicationConfiguration(
			CompositePropertySource compositePropertySource, String dataIdPrefix,
			NacosConfigProperties properties, Environment environment) {

		String fileExtension = properties.getFileExtension();
		String nacosGroup = properties.getGroup();

		loadNacosDataIfPresent(compositePropertySource,
				dataIdPrefix + DOT + fileExtension, nacosGroup, fileExtension, true);
		for (String profile : environment.getActiveProfiles()) {
			String dataId = dataIdPrefix + SEP1 + profile + DOT + fileExtension;
			loadNacosDataIfPresent(compositePropertySource, dataId, nacosGroup,
					fileExtension, true);
		}
	}

	private void loadNacosDataIfPresent(final CompositePropertySource composite,
			final String dataId, final String group, String fileExtension,
			boolean isRefreshable) {
		if (NacosContextRefresher.getRefreshCount() != 0) {
			NacosPropertySource ps;
			if (!isRefreshable) {
				ps = NacosPropertySourceRepository.getNacosPropertySource(dataId);
			}
			else {
				ps = nacosPropertySourceBuilder.build(dataId, group, fileExtension, true);
			}

			composite.addFirstPropertySource(ps);
		}
		else {
			NacosPropertySource ps = nacosPropertySourceBuilder.build(dataId, group,
					fileExtension, isRefreshable);
			composite.addFirstPropertySource(ps);
		}
	}

	private static void checkDataIdFileExtension(String[] dataIdArray) {
		if (dataIdArray == null || dataIdArray.length < 1) {
			throw new IllegalStateException("The dataId cannot be empty");
		}
		// Just decide that the current dataId must have a suffix
		NacosDataParserHandler.getInstance().checkDataId(dataIdArray);
	}

	private boolean checkDataIdIsRefreshable(String refreshDataIds, String sharedDataId) {
		if (StringUtils.isEmpty(refreshDataIds)) {
			return false;
		}

		String[] refreshDataIdArray = refreshDataIds.split(SHARED_CONFIG_SEPARATOR_CHAR);
		for (String refreshDataId : refreshDataIdArray) {
			if (refreshDataId.equals(sharedDataId)) {
				return true;
			}
		}

		return false;
	}

}
