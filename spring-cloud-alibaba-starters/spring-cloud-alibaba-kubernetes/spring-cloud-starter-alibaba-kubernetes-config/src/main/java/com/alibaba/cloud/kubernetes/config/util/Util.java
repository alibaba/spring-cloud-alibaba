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

package com.alibaba.cloud.kubernetes.config.util;

import java.util.Optional;

import com.alibaba.cloud.kubernetes.config.KubernetesConfigProperties;
import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.Secret;

/**
 * @author Freeman
 */
public final class Util {

	private Util() {
		throw new UnsupportedOperationException("No Util instances for you!");
	}

	public static ResourceKey resourceKey(KubernetesConfigProperties.ConfigMap configMap,
			KubernetesConfigProperties properties) {
		return new ResourceKey(ConfigMap.class.getSimpleName(), configMap.getName(),
				namespace(configMap, properties), refreshable(configMap, properties));
	}

	public static ResourceKey resourceKey(KubernetesConfigProperties.Secret secret,
			KubernetesConfigProperties properties) {
		return new ResourceKey(Secret.class.getSimpleName(), secret.getName(),
				namespace(secret, properties), refreshable(secret, properties));
	}

	public static String namespace(KubernetesConfigProperties.ConfigMap configMap,
			KubernetesConfigProperties properties) {
		return Optional.ofNullable(configMap.getNamespace())
				.orElseGet(properties::getNamespace);
	}

	public static String namespace(KubernetesConfigProperties.Secret secret,
			KubernetesConfigProperties properties) {
		return Optional.ofNullable(secret.getNamespace())
				.orElseGet(properties::getNamespace);
	}

	public static boolean refreshable(KubernetesConfigProperties.ConfigMap configMap,
			KubernetesConfigProperties properties) {
		return Optional.ofNullable(configMap.getRefreshable())
				.orElseGet(properties::isRefreshable);
	}

	public static boolean refreshable(KubernetesConfigProperties.Secret secret,
			KubernetesConfigProperties properties) {
		return Optional.ofNullable(secret.getRefreshable())
				.orElseGet(properties::isRefreshable);
	}

	public static ConfigPreference preference(
			KubernetesConfigProperties.ConfigMap configMap,
			KubernetesConfigProperties properties) {
		return Optional.ofNullable(configMap.getPreference())
				.orElseGet(properties::getPreference);
	}

	public static ConfigPreference preference(KubernetesConfigProperties.Secret secret,
			KubernetesConfigProperties properties) {
		return Optional.ofNullable(secret.getPreference())
				.orElseGet(properties::getPreference);
	}
}
