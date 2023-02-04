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

package com.alibaba.cloud.kubernetes.commons;

import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;

/**
 * Kubernetes utils.
 *
 * <p>
 * Usually used to get kube config and create {@link KubernetesClient} instance.
 *
 * @author Freeman
 */
public final class KubernetesUtils {

	private KubernetesUtils() {
		throw new UnsupportedOperationException("No KubernetesUtil instances for you!");
	}

	private static final Config config = new ConfigBuilder().build();

	/**
	 * Get the kube config.
	 *
	 * <p>
	 * <strong>NOTE: {@link Config} needs to be a singleton, do NOT modify it.</strong>
	 *
	 * @return Config
	 */
	public static Config config() {
		return config;
	}

	/**
	 * Get the current namespace.
	 * <p>
	 * If in kubernetes, it will return the namespace of the pod.
	 * <p>
	 * If not in kubernetes, it will return the namespace of the kube config current
	 * context.
	 *
	 * @return namespace
	 */
	public static String currentNamespace() {
		return config.getNamespace();
	}

	/**
	 * Create a KubernetesClient instance.
	 *
	 * @return new KubernetesClient instance
	 */
	public static KubernetesClient newKubernetesClient() {
		return new DefaultKubernetesClient(config);
	}
}
