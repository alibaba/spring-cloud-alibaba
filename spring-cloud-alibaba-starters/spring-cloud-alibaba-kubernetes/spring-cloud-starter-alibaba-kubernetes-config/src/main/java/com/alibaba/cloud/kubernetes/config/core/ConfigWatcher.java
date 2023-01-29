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

package com.alibaba.cloud.kubernetes.config.core;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.alibaba.cloud.kubernetes.config.KubernetesConfigProperties;
import com.alibaba.cloud.kubernetes.config.util.ResourceKey;
import com.alibaba.cloud.kubernetes.config.util.Util;
import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.informers.SharedIndexInformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;

/**
 * Watcher for config resource changes.
 *
 * @author Freeman
 */
public class ConfigWatcher implements SmartInitializingSingleton, ApplicationContextAware,
		EnvironmentAware, DisposableBean {
	private static final Logger log = LoggerFactory.getLogger(ConfigWatcher.class);

	private final Map<ResourceKey, SharedIndexInformer<ConfigMap>> configmapInformers = new LinkedHashMap<>();
	private final Map<ResourceKey, SharedIndexInformer<Secret>> secretInformers = new LinkedHashMap<>();
	private final KubernetesConfigProperties properties;
	private final KubernetesClient client;

	private ApplicationContext context;
	private ConfigurableEnvironment environment;

	public ConfigWatcher(KubernetesConfigProperties properties, KubernetesClient client) {
		this.properties = properties;
		this.client = client;
	}

	@Override
	public void setEnvironment(Environment environment) {
		if (!(environment instanceof ConfigurableEnvironment)) {
			throw new IllegalStateException(
					"Environment must be an instance of ConfigurableEnvironment");
		}
		this.environment = (ConfigurableEnvironment) environment;
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		this.context = applicationContext;
	}

	@Override
	public void afterSingletonsInstantiated() {
		watchRefreshableResources(properties, client);
	}

	@Override
	public void destroy() {
		configmapInformers.values().forEach(SharedIndexInformer::close);
		secretInformers.values().forEach(SharedIndexInformer::close);
		log.info("ConfigMap and Secret informers closed");
	}

	private void watchRefreshableResources(KubernetesConfigProperties properties,
			KubernetesClient client) {
		properties.getConfigMaps().stream().filter(cm -> Util.refreshable(cm, properties))
				.forEach(cm -> configmapInformers.put(Util.resourceKey(cm, properties),
						client.configMaps().inNamespace(Util.namespace(cm, properties))
								.withName(cm.getName())
								.inform(new HasMetadataResourceEventHandler<>(context,
										environment, properties))));
		log(configmapInformers);
		properties.getSecrets().stream()
				.filter(secret -> Util.refreshable(secret, properties))
				.forEach(secret -> secretInformers.put(
						Util.resourceKey(secret, properties),
						client.secrets().inNamespace(Util.namespace(secret, properties))
								.withName(secret.getName())
								.inform(new HasMetadataResourceEventHandler<>(context,
										environment, properties))));
		log(secretInformers);
	}

	private <T extends HasMetadata> void log(
			Map<ResourceKey, SharedIndexInformer<T>> informers) {
		List<String> names = informers.keySet().stream().map(resourceKey -> String
				.join(".", resourceKey.name(), resourceKey.namespace()))
				.collect(Collectors.toList());
		if (!names.isEmpty() && log.isInfoEnabled()) {
			log.info("Start watching {}s: {}",
					informers.keySet().iterator().next().type(), names);
		}
	}
}
