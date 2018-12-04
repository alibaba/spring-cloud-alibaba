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

package org.springframework.cloud.alibaba.nacos.client;

import com.alibaba.nacos.api.config.ConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.alibaba.nacos.NacosConfigProperties;
import org.springframework.cloud.bootstrap.config.PropertySourceLocator;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertySource;
import org.springframework.util.StringUtils;
import java.util.Arrays;
import java.util.List;

/**
 * @author xiaojing
 */
@Order(0)
public class NacosPropertySourceLocator implements PropertySourceLocator {

	private static final Logger logger = LoggerFactory
			.getLogger(NacosPropertySourceLocator.class);
	private static final String NACOS_PROPERTY_SOURCE_NAME = "NACOS";
	private static final String SEP1 = "-";
	private static final String DOT = ".";
	private static final String SHARED_CONFIG_SEPRATOR_CHAR = "[,]";
	private static final List<String> SUPPORT_FILE_EXTENSION = Arrays.asList("properties",
			"yaml", "yml");

	@Autowired
	private NacosConfigProperties nacosConfigProperties;

	private NacosPropertySourceBuilder nacosPropertySourceBuilder;

	@Override
	public PropertySource<?> locate(Environment env) {

		ConfigService configService = nacosConfigProperties.configServiceInstance();

		if (null == configService) {
			logger.warn(
					"no instance of config service found, can't load config from nacos");
			return null;
		}
		long timeout = nacosConfigProperties.getTimeout();
		nacosPropertySourceBuilder = new NacosPropertySourceBuilder(configService,
				timeout);

		String name = nacosConfigProperties.getName();

		String nacosGroup = nacosConfigProperties.getGroup();
		String dataIdPrefix = nacosConfigProperties.getPrefix();
		if (StringUtils.isEmpty(dataIdPrefix)) {
			dataIdPrefix = name;
		}

		String fileExtension = nacosConfigProperties.getFileExtension();

		CompositePropertySource composite = new CompositePropertySource(
				NACOS_PROPERTY_SOURCE_NAME);

		loadSharedConfiguration(composite);
		loadExtConfiguration(composite);
		loadApplicationConfiguration(composite, nacosGroup, dataIdPrefix, fileExtension);

		return composite;
	}

	private void loadSharedConfiguration(
			CompositePropertySource compositePropertySource) {
		String sharedDataIds = nacosConfigProperties.getSharedDataids();
		String refreshDataIds = nacosConfigProperties.getRefreshableDataids();

		if (sharedDataIds == null || sharedDataIds.trim().length() == 0) {
			return;
		}

		String[] sharedDataIdArry = sharedDataIds.split(SHARED_CONFIG_SEPRATOR_CHAR);
		checkDataIdFileExtension(sharedDataIdArry);

		for (int i = 0; i < sharedDataIdArry.length; i++) {
			String dataId = sharedDataIdArry[i];
			String fileExtension = dataId.substring(dataId.lastIndexOf(".") + 1);
			boolean isRefreshable = checkDataIdIsRefreshbable(refreshDataIds,
					sharedDataIdArry[i]);

			loadNacosDataIfPresent(compositePropertySource, dataId, "DEFAULT_GROUP",
					fileExtension, isRefreshable);
		}
	}

	private void loadExtConfiguration(CompositePropertySource compositePropertySource) {
		if (nacosConfigProperties.getExtConfig() == null
				|| nacosConfigProperties.getExtConfig().isEmpty()) {
			return;
		}

		List<NacosConfigProperties.Config> extConfigs = nacosConfigProperties
				.getExtConfig();
		checkExtConfiguration(extConfigs);

		for (NacosConfigProperties.Config config : extConfigs) {
			String dataId = config.getDataId();
			String fileExtension = dataId.substring(dataId.lastIndexOf(".") + 1);
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
						"the [ spring.cloud.nacos.config.ext-config[%s] ] must give a dataid",
						i));
			}
			dataIds[i] = dataId;
		}
		checkDataIdFileExtension(dataIds);
	}

	private void loadApplicationConfiguration(
			CompositePropertySource compositePropertySource, String nacosGroup,
			String dataIdPrefix, String fileExtension) {
		loadNacosDataIfPresent(compositePropertySource,
				dataIdPrefix + DOT + fileExtension, nacosGroup, fileExtension, true);
		for (String profile : nacosConfigProperties.getActiveProfiles()) {
			String dataId = dataIdPrefix + SEP1 + profile + DOT + fileExtension;
			loadNacosDataIfPresent(compositePropertySource, dataId, nacosGroup,
					fileExtension, true);
		}
	}

	private void loadNacosDataIfPresent(final CompositePropertySource composite,
			final String dataId, final String group, String fileExtension,
			boolean isRefreshable) {
		NacosPropertySource ps = nacosPropertySourceBuilder.build(dataId, group,
				fileExtension, isRefreshable);
		if (ps != null) {
			composite.addFirstPropertySource(ps);
		}
	}

	private static void checkDataIdFileExtension(String[] sharedDataIdArry) {
		StringBuilder stringBuilder = new StringBuilder();
		outline: for (int i = 0; i < sharedDataIdArry.length; i++) {
			for (String fileExtension : SUPPORT_FILE_EXTENSION) {
				if (sharedDataIdArry[i].indexOf(fileExtension) > 0) {
					continue outline;
				}
			}
			stringBuilder.append(sharedDataIdArry[i] + ",");
		}

		if (stringBuilder.length() > 0) {
			String result = stringBuilder.substring(0, stringBuilder.length() - 1);
			throw new IllegalStateException(String.format(
					"[%s] must contains file extension with properties|yaml|yml",
					result));
		}
	}

	private boolean checkDataIdIsRefreshbable(String refreshDataIds,
			String sharedDataId) {
		if (refreshDataIds == null || "".equals(refreshDataIds)) {
			return false;
		}

		String[] refreshDataIdArry = refreshDataIds.split(SHARED_CONFIG_SEPRATOR_CHAR);
		for (String refreshDataId : refreshDataIdArry) {
			if (refreshDataId.equals(sharedDataId)) {
				return true;
			}
		}

		return false;
	}

}
