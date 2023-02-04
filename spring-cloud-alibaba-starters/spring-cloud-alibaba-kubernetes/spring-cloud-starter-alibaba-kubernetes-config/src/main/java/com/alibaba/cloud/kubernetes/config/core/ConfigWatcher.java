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

import static com.alibaba.cloud.kubernetes.config.util.ResourceKeyUtils.resourceKey;

/**
 * Watcher for config resources change.
 *
 * @author Freeman
 */
public class ConfigWatcher
		implements SmartInitializingSingleton, ApplicationContextAware, DisposableBean {
	private static final Logger log = LoggerFactory.getLogger(ConfigWatcher.class);

	private final Map<ResourceKey, SharedIndexInformer<ConfigMap>> configmapInformers = new LinkedHashMap<>();
	private final Map<ResourceKey, SharedIndexInformer<Secret>> secretInformers = new LinkedHashMap<>();
	private final KubernetesConfigProperties properties;
	private final KubernetesClient client;

	private ApplicationContext context;

	public ConfigWatcher(KubernetesConfigProperties properties, KubernetesClient client) {
		this.properties = properties;
		this.client = client;
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
		if (log.isInfoEnabled()) {
			log.info("[Kubernetes Config] ConfigMap and Secret informers closed");
		}
	}

	private void watchRefreshableResources(KubernetesConfigProperties properties,
			KubernetesClient client) {
		properties.getConfigMaps().stream()
				.filter(KubernetesConfigProperties.ConfigMap::getRefreshable)
				.forEach(configmap -> {
					SharedIndexInformer<ConfigMap> informer = client.configMaps()
							.inNamespace(configmap.getNamespace())
							.withName(configmap.getName())
							.inform(new HasMetadataResourceEventHandler<>(context,
									properties));
					configmapInformers.put(resourceKey(configmap), informer);
				});
		log(configmapInformers);
		properties.getSecrets().stream()
				.filter(KubernetesConfigProperties.Secret::getRefreshable)
				.forEach(secret -> {
					SharedIndexInformer<Secret> informer = client.secrets()
							.inNamespace(secret.getNamespace()).withName(secret.getName())
							.inform(new HasMetadataResourceEventHandler<>(context,
									properties));
					secretInformers.put(resourceKey(secret), informer);
				});
		log(secretInformers);
	}

	private static <T extends HasMetadata> void log(
			Map<ResourceKey, SharedIndexInformer<T>> informers) {
		List<String> names = informers.keySet().stream().map(resourceKey -> String
				.join(".", resourceKey.name(), resourceKey.namespace()))
				.collect(Collectors.toList());
		if (!names.isEmpty() && log.isInfoEnabled()) {
			log.info("[Kubernetes Config] Start watching {}s: {}",
					informers.keySet().iterator().next().type(), names);
		}
	}
}
