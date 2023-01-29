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

import java.util.concurrent.atomic.AtomicReference;

import io.fabric8.kubernetes.client.KubernetesClient;

/**
 * KubernetesClient holder, we need to ensure that only one KubernetesClient instance in
 * one application.
 *
 * <p>
 * Note: there's only one KubernetesClient instance in <strong>one application</strong>,
 * but one JVM may have multiple instances, because the tests need to use multiple
 * instances.
 *
 * @author Freeman
 */
public final class KubernetesClientHolder {

	private KubernetesClientHolder() {
		throw new UnsupportedOperationException("No KubernetesClientHolder instances for you!");
	}

	private static final AtomicReference<KubernetesClient> kubernetesClient = new AtomicReference<>();

	public static synchronized KubernetesClient getKubernetesClient() {
		KubernetesClient client = kubernetesClient.get();
		if (client == null) {
			kubernetesClient.set(KubernetesUtils.newKubernetesClient());
		}
		return kubernetesClient.get();
	}

	public static synchronized void remove() {
		kubernetesClient.set(null);
	}
}
