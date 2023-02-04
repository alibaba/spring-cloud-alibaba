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

import java.net.HttpURLConnection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.alibaba.cloud.kubernetes.commons.KubernetesClientHolder;
import com.alibaba.cloud.kubernetes.config.KubernetesConfigProperties;
import com.alibaba.cloud.kubernetes.config.exception.AbstractKubernetesConfigException;
import com.alibaba.cloud.kubernetes.config.exception.KubernetesConfigMissingException;
import com.alibaba.cloud.kubernetes.config.exception.KubernetesForbiddenException;
import com.alibaba.cloud.kubernetes.config.exception.KubernetesUnavailableException;
import com.alibaba.cloud.kubernetes.config.util.Pair;
import com.alibaba.cloud.kubernetes.config.util.Preference;
import com.alibaba.cloud.kubernetes.config.util.RefreshContext;
import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import org.apache.commons.logging.Log;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.boot.logging.DeferredLogFactory;
import org.springframework.cloud.endpoint.event.RefreshEvent;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.StandardEnvironment;

import static com.alibaba.cloud.kubernetes.config.util.Converters.toPropertySource;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;

/**
 * @author Freeman
 */
public class ConfigEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {
	/**
	 * Order of the post processor.
	 */
	public static final int ORDER = Ordered.LOWEST_PRECEDENCE - 10;

	private final Log log;
	private final KubernetesClient client;

	public ConfigEnvironmentPostProcessor(DeferredLogFactory logFactory) {
		this.log = logFactory.getLog(getClass());
		this.client = KubernetesClientHolder.getKubernetesClient();
	}

	@Override
	public void postProcessEnvironment(ConfigurableEnvironment environment,
			SpringApplication application) {
		Boolean enabled = environment.getProperty(
				KubernetesConfigProperties.PREFIX + ".enabled", Boolean.class, true);
		if (!enabled) {
			return;
		}

		KubernetesConfigProperties properties = getKubernetesConfigProperties(
				environment);

		if (isRefreshing()) {
			RefreshEvent event = RefreshContext.get().refreshEvent();
			Object resource = event.getSource();
			// Just add it, {@link
			// org.springframework.cloud.context.refresh.ContextRefresher} will
			// replace the PropertySource for you.
			if (resource instanceof ConfigMap) {
				environment.getPropertySources()
						.addLast(toPropertySource((ConfigMap) resource));
			}
			else if (resource instanceof Secret) {
				environment.getPropertySources()
						.addLast(toPropertySource((Secret) resource));
			}
			else {
				log.warn("[Kubernetes Config] Refreshed a unknown resource type: "
						+ resource.getClass());
			}
		}
		else {
			pullConfigMaps(properties, environment);
			pullSecrets(properties, environment);
		}
	}

	private static KubernetesConfigProperties getKubernetesConfigProperties(
			ConfigurableEnvironment environment) {
		return Optional
				.ofNullable(RefreshContext.get()).map(context -> context
						.applicationContext().getBean(KubernetesConfigProperties.class))
				.orElseGet(() -> {
					KubernetesConfigProperties prop = Binder.get(environment)
							.bind(KubernetesConfigProperties.PREFIX,
									KubernetesConfigProperties.class)
							.orElseGet(KubernetesConfigProperties::new);
					prop.afterPropertiesSet();
					return prop;
				});
	}

	private void pullConfigMaps(KubernetesConfigProperties properties,
			ConfigurableEnvironment environment) {
		properties.getConfigMaps().stream()
				.map(configmap -> Optional
						.ofNullable(propertySourceForConfigMap(configmap,
								properties.isFailOnMissingConfig()))
						.map(ps -> Pair.of(configmap.getPreference(), ps)).orElse(null))
				.filter(Objects::nonNull)
				.collect(groupingBy(Pair::key, mapping(Pair::value, toList())))
				.forEach((preference, remotePropertySources) -> {
					addPropertySourcesToEnvironment(environment, preference,
							remotePropertySources);
				});
	}

	private void pullSecrets(KubernetesConfigProperties properties,
			ConfigurableEnvironment environment) {
		properties.getSecrets().stream()
				.map(secret -> Optional
						.ofNullable(propertySourceForSecret(secret,
								properties.isFailOnMissingConfig()))
						.map(ps -> Pair.of(secret.getPreference(), ps)).orElse(null))
				.filter(Objects::nonNull)
				.collect(groupingBy(Pair::key, mapping(Pair::value, toList())))
				.forEach((preference, remotePropertySources) -> {
					addPropertySourcesToEnvironment(environment, preference,
							remotePropertySources);
				});
	}

	private static <T> void addPropertySourcesToEnvironment(
			ConfigurableEnvironment environment, Preference preference,
			List<EnumerablePropertySource<T>> remotePropertySources) {
		MutablePropertySources propertySources = environment.getPropertySources();
		switch (preference) {
		case LOCAL:
			// The latter config should win the previous config
			Collections.reverse(remotePropertySources);
			remotePropertySources.forEach(propertySources::addLast);
			break;
		case REMOTE:
			// we can't let it override the system environment properties
			remotePropertySources.forEach(ps -> propertySources.addAfter(
					StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME, ps));
			break;
		default:
			throw new IllegalArgumentException(
					"Unknown config preference: " + preference.name());
		}
	}

	private EnumerablePropertySource<?> propertySourceForConfigMap(
			KubernetesConfigProperties.ConfigMap cm, boolean isFailOnMissingConfig) {
		ConfigMap configMap;
		try {
			configMap = client.configMaps().inNamespace(cm.getNamespace())
					.withName(cm.getName()).get();
		}
		catch (KubernetesClientException e) {
			processException(ConfigMap.class, cm.getName(), cm.getNamespace(), e);
			return null;
		}
		if (configMap == null) {
			failApplicationStartUpIfNecessary(ConfigMap.class, cm.getName(),
					cm.getNamespace(), isFailOnMissingConfig);
			return null;
		}
		return toPropertySource(configMap);
	}

	private EnumerablePropertySource<?> propertySourceForSecret(
			KubernetesConfigProperties.Secret secret, boolean isFailOnMissingConfig) {
		Secret secretInK8s;
		try {
			secretInK8s = client.secrets().inNamespace(secret.getNamespace())
					.withName(secret.getName()).get();
		}
		catch (KubernetesClientException e) {
			processException(Secret.class, secret.getName(), secret.getNamespace(), e);
			return null;
		}
		if (secretInK8s == null) {
			failApplicationStartUpIfNecessary(Secret.class, secret.getName(),
					secret.getNamespace(), isFailOnMissingConfig);
			return null;
		}
		return toPropertySource(secretInK8s);
	}

	private void processException(Class<?> type, String name, String namespace,
			KubernetesClientException e) {
		if (!isRefreshing()) {
			throw kubernetesConfigException(type, name, namespace, e);
		}
		log.warn(String.format(
				"[Kubernetes Config] Kubernetes client exception while refreshing %s, so properties value won't change",
				type.getSimpleName()), e);
	}

	private static AbstractKubernetesConfigException kubernetesConfigException(
			Class<?> type, String name, String namespace, KubernetesClientException e) {
		// Usually the Service Account or user does not have enough privileges.
		if (e.getCode() == HttpURLConnection.HTTP_FORBIDDEN) {
			return new KubernetesForbiddenException(type, name, namespace, e);
		}
		// Can't connect to the kubernetes cluster.
		return new KubernetesUnavailableException(type, name, namespace, e);
	}

	/**
	 * Fail the application start up if necessary when the resource is missing.
	 *
	 * <p>
	 * NOTE: do nothing if the application is refreshing
	 *
	 * @param type the type of the resource
	 * @param name the name of the resource
	 * @param namespace the namespace of the resource
	 * @param isFailOnMissingConfig whether to fail the application start up
	 */
	private void failApplicationStartUpIfNecessary(Class<?> type, String name,
			String namespace, boolean isFailOnMissingConfig) {
		log.warn(String.format("[Kubernetes Config] %s '%s' not found in namespace '%s'",
				type.getSimpleName(), name, namespace));
		if (!isRefreshing() && isFailOnMissingConfig) {
			throw new KubernetesConfigMissingException(type, name, namespace, null);
		}
	}

	private static boolean isRefreshing() {
		return RefreshContext.get() != null;
	}

	@Override
	public int getOrder() {
		return ORDER;
	}
}
