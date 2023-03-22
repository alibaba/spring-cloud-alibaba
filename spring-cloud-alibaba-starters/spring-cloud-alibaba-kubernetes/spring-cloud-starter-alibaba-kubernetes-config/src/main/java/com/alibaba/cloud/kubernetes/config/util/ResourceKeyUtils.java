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

import com.alibaba.cloud.kubernetes.config.KubernetesConfigProperties;
import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.Secret;

/**
 * {@link ResourceKeyUtils} used to generate {@link ResourceKey}.
 *
 * @author Freeman
 */
public final class ResourceKeyUtils {

	private ResourceKeyUtils() {
		throw new UnsupportedOperationException("No ResourceKeyUtils instances for you!");
	}

	/**
	 * Generate a {@link ResourceKey} from {@link KubernetesConfigProperties.ConfigMap}.
	 *
	 * @param configMap {@link KubernetesConfigProperties.ConfigMap}
	 * @return {@link ResourceKey}
	 */
	public static ResourceKey resourceKey(
			KubernetesConfigProperties.ConfigMap configMap) {
		return new ResourceKey(ConfigMap.class.getSimpleName(), configMap.getName(),
				configMap.getNamespace());
	}

	/**
	 * Generate a {@link ResourceKey} from {@link KubernetesConfigProperties.Secret}.
	 *
	 * @param secret {@link KubernetesConfigProperties.Secret}
	 * @return {@link ResourceKey}
	 */
	public static ResourceKey resourceKey(KubernetesConfigProperties.Secret secret) {
		return new ResourceKey(Secret.class.getSimpleName(), secret.getName(),
				secret.getNamespace());
	}
}
