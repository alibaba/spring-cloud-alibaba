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

package com.alibaba.cloud.nacos.bridge;

import java.util.Map;
import java.util.Objects;
import java.util.Properties;

import com.alibaba.boot.nacos.config.NacosConfigConstants;
import com.alibaba.boot.nacos.config.util.AttributeExtractTask;
import com.alibaba.cloud.nacos.NacosConfigProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;

import static org.springframework.core.env.StandardEnvironment.SYSTEM_PROPERTIES_PROPERTY_SOURCE_NAME;

/**
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 */
public class NacosCloudBridgeBoot implements EnvironmentPostProcessor, Ordered {

	private static final Logger log = LoggerFactory.getLogger(NacosCloudBridgeBoot.class);

	private static final String NACOS_CLOUD_PREFIX = NacosConfigProperties.PREFIX;

	private static final String NACOS_BOOT_PREFIX = NacosConfigConstants.PREFIX;

	// Due to the Spring of the particularity of the Cloud, is to create two Context,
	// so the use of a variable to hold, which makes the configuration nacos-spring-cloud
	// through to nacos-springboot

	private static PropertySource<Map<String, Object>> holder;

	@Override
	public void postProcessEnvironment(ConfigurableEnvironment environment,
			SpringApplication application) {
		AttributeExtractTask extractTask = new AttributeExtractTask(NACOS_CLOUD_PREFIX,
				environment);
		try {
			Map<String, String> cloudNacosConfigProperties = extractTask.call();
			Properties bootNacosConfigProperties = cloudNacosConfigProperties.entrySet()
					.stream()
					// Prevent and spring-cloud-nacos with spring-boot-nacos configuration
					// conflicts, configuration and unconscious pull
					.filter(entry -> !entry.getKey()
							.startsWith(NACOS_CLOUD_PREFIX + ".ext-config"))
					.map(entry -> {
						String key = entry.getKey().replace(NACOS_CLOUD_PREFIX,
								NACOS_BOOT_PREFIX);
						String value = entry.getValue();
						return Tuple.of(key, value);
					}).collect(Properties::new, (m, e) -> m.setProperty(e.key, e.value),
							Properties::putAll);
			bootNacosConfigProperties = NacosBridgeUtils
					.filter(bootNacosConfigProperties);
			PropertySource<Map<String, Object>> propertySource = new PropertiesPropertySource(
					"nacos_boot_property", bootNacosConfigProperties);
			if (Objects.isNull(holder)) {
				holder = propertySource;
			}
			else {
				Map<String, Object> now = propertySource.getSource();
				now.putAll(holder.getSource());
			}
			// Because of springboot-nacos internal mechanism, temporarily shut down the
			// two loading plug-ins
			propertySource.getSource().put("nacos.config.bootstrap.enable", "false");
			propertySource.getSource().put("nacos.config.bootstrap.log.enable", "false");
			// In order to avoid the automatic analytic nacos-springboot configuration
			environment.getPropertySources()
					.addAfter(SYSTEM_PROPERTIES_PROPERTY_SOURCE_NAME, propertySource);
		}
		catch (Exception e) {
			log.error("NacosBootBridge has error : {}", e.getMessage());
		}
	}

	@Override
	public int getOrder() {
		return Ordered.LOWEST_PRECEDENCE - 20;
	}

	private static final class Tuple {

		private final String key;

		private final String value;

		private Tuple(String key, String value) {
			this.key = key;
			this.value = value;
		}

		public static Tuple of(String key, String value) {
			return new Tuple(key, value);
		}

	}

}
