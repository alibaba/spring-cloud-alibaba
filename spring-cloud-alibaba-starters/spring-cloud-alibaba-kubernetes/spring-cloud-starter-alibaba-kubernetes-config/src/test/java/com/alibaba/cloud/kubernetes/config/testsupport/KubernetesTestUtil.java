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

package com.alibaba.cloud.kubernetes.config.testsupport;

import java.io.IOException;

import com.alibaba.cloud.kubernetes.commons.KubernetesUtils;
import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.client.KubernetesClient;

import org.springframework.core.io.ClassPathResource;

/**
 * @author Freeman
 */
public final class KubernetesTestUtil {

	private KubernetesTestUtil() {
		throw new UnsupportedOperationException(
				"No KubernetesTestUtil instances for you!");
	}

	static KubernetesClient cli = KubernetesUtils.newKubernetesClient();

	public static ConfigMap configMap(String classpathFile) {
		try {
			return cli.configMaps().load(new ClassPathResource(classpathFile).getURL())
					.get();
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static Secret secret(String classpathFile) {
		try {
			return cli.secrets().load(new ClassPathResource(classpathFile).getURL())
					.get();
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static ConfigMap createOrReplaceConfigMap(String classpathFile) {
		return cli.resource(configMap(classpathFile)).createOrReplace();
	}

	public static void deleteConfigMap(String classpathFile) {
		cli.resource(configMap(classpathFile)).delete();
	}

	public static Secret createOrReplaceSecret(String classpathFile) {
		return cli.resource(secret(classpathFile)).createOrReplace();
	}

	public static void deleteSecret(String classpathFile) {
		cli.resource(secret(classpathFile)).delete();
	}
}
