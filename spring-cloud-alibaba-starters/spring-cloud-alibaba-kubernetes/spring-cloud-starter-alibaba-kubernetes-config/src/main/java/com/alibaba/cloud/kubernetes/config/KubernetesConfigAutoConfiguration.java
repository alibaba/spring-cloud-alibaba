/*
 * Copyright 2013-2023 the original author or authors.
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

package com.alibaba.cloud.kubernetes.config;

import com.alibaba.cloud.kubernetes.commons.KubernetesClientConfiguration;
import com.alibaba.cloud.kubernetes.config.core.ConfigWatcher;
import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.client.KubernetesClient;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * @author Freeman
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass({ KubernetesClient.class, ConfigMap.class })
@ConditionalOnProperty(prefix = KubernetesConfigProperties.PREFIX, name = "enabled", matchIfMissing = true)
@EnableConfigurationProperties(KubernetesConfigProperties.class)
@Import(KubernetesClientConfiguration.class)
public class KubernetesConfigAutoConfiguration {

	@Bean
	@ConditionalOnMissingBean
	public ConfigWatcher kubernetesConfigWatcher(KubernetesConfigProperties properties,
			KubernetesClient kubernetesClient) {
		return new ConfigWatcher(properties, kubernetesClient);
	}
}
