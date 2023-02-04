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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.alibaba.cloud.kubernetes.commons.KubernetesUtils;
import com.alibaba.cloud.kubernetes.config.util.Preference;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;

/**
 * Spring Cloud Alibaba Kubernetes Config properties.
 *
 * @author Freeman
 */
@ConfigurationProperties(KubernetesConfigProperties.PREFIX)
public class KubernetesConfigProperties implements InitializingBean {
	/**
	 * Prefix of {@link KubernetesConfigProperties}.
	 */
	public static final String PREFIX = "spring.cloud.k8s.config";

	/**
	 * Whether to enable the kubernetes config feature.
	 */
	private boolean enabled = true;

	/**
	 * Default namespace for ConfigMaps and Secrets.
	 * <p>
	 * If in Kubernetes environment, use the namespace of the current pod.
	 * <p>
	 * If not in Kubernetes environment, use the namespace of the current context.
	 */
	private String namespace = determineNamespace();

	/**
	 * Config preference, default is {@link Preference#REMOTE}, means remote
	 * configurations 'win', will override the local configurations.
	 */
	private Preference preference = Preference.REMOTE;

	/**
	 * Whether to refresh environment when remote resource was deleted, default value is
	 * {@code false}.
	 * <p>
	 * The default value is {@code false} to prevent app arises abnormal situation from
	 * resource being deleted by mistake.
	 */
	private boolean refreshOnDelete = false;

	/**
	 * Whether to fail when the config (configmap/secret) is missing, default value is
	 * {@code true}.
	 * <p>
	 * The default value is true to prevent unintended problems caused by not
	 * synchronizing the configuration between environments.
	 */
	private boolean failOnMissingConfig = true;

	/**
	 * Whether to enable the auto refresh feature, default value is {@code false}.
	 */
	private boolean refreshable = false;

	private List<ConfigMap> configMaps = new ArrayList<>();

	private List<Secret> secrets = new ArrayList<>();

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public String getNamespace() {
		return namespace;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

	public Preference getPreference() {
		return preference;
	}

	public void setPreference(Preference preference) {
		this.preference = preference;
	}

	public List<ConfigMap> getConfigMaps() {
		return configMaps;
	}

	public void setConfigMaps(List<ConfigMap> configMaps) {
		this.configMaps = configMaps;
	}

	public List<Secret> getSecrets() {
		return secrets;
	}

	public void setSecrets(List<Secret> secrets) {
		this.secrets = secrets;
	}

	public boolean isRefreshable() {
		return refreshable;
	}

	public void setRefreshable(boolean refreshable) {
		this.refreshable = refreshable;
	}

	public boolean isRefreshOnDelete() {
		return refreshOnDelete;
	}

	public void setRefreshOnDelete(boolean refreshOnDelete) {
		this.refreshOnDelete = refreshOnDelete;
	}

	public boolean isFailOnMissingConfig() {
		return failOnMissingConfig;
	}

	public void setFailOnMissingConfig(boolean failOnMissingConfig) {
		this.failOnMissingConfig = failOnMissingConfig;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		KubernetesConfigProperties that = (KubernetesConfigProperties) o;
		return enabled == that.enabled && refreshOnDelete == that.refreshOnDelete
				&& failOnMissingConfig == that.failOnMissingConfig
				&& refreshable == that.refreshable
				&& Objects.equals(namespace, that.namespace)
				&& preference == that.preference
				&& Objects.equals(configMaps, that.configMaps)
				&& Objects.equals(secrets, that.secrets);
	}

	@Override
	public int hashCode() {
		return Objects.hash(enabled, namespace, preference, refreshOnDelete,
				failOnMissingConfig, configMaps, secrets, refreshable);
	}

	@Override
	public String toString() {
		return "KubernetesConfigProperties{" + "enabled=" + enabled + ", namespace='"
				+ namespace + '\'' + ", preference=" + preference + ", refreshOnDelete="
				+ refreshOnDelete + ", failOnMissingConfig=" + failOnMissingConfig
				+ ", configMaps=" + configMaps + ", secrets=" + secrets
				+ ", refreshEnabled=" + refreshable + '}';
	}

	@Override
	public void afterPropertiesSet() {
		mergeConfigmaps();
		mergeSecrets();
	}

	private void mergeConfigmaps() {
		for (ConfigMap configMap : configMaps) {
			if (!StringUtils.hasText(configMap.getName())) {
				throw new IllegalArgumentException("ConfigMap name must not be empty.");
			}
			if (configMap.getNamespace() == null) {
				configMap.setNamespace(namespace);
			}
			if (configMap.getRefreshable() == null) {
				configMap.setRefreshable(refreshable);
			}
			if (configMap.getPreference() == null) {
				configMap.setPreference(preference);
			}
		}
	}

	private void mergeSecrets() {
		for (Secret secret : secrets) {
			if (!StringUtils.hasText(secret.getName())) {
				throw new IllegalArgumentException("Secret name must not be empty.");
			}
			if (secret.getNamespace() == null) {
				secret.setNamespace(namespace);
			}
			if (secret.getRefreshable() == null) {
				secret.setRefreshable(refreshable);
			}
			if (secret.getPreference() == null) {
				secret.setPreference(preference);
			}
		}
	}

	private static String determineNamespace() {
		String ns = KubernetesUtils.currentNamespace();
		return StringUtils.hasText(ns) ? ns : "default";
	}

	public static class ConfigMap {
		/**
		 * ConfigMap name.
		 */
		private String name;
		/**
		 * Namespace, using
		 * <span color="orange">{@code spring.cloud.k8s.config.namespace}</span> if not
		 * set.
		 */
		private String namespace;
		/**
		 * Whether to enable the auto refresh on current ConfigMap, using
		 * <span color="orange">{@code spring.cloud.k8s.config.refreshable}</span> if not
		 * set.
		 */
		private Boolean refreshable;
		/**
		 * Config preference, using
		 * <span color="orange">{@code spring.cloud.k8s.config.preference}</span> if not
		 * set.
		 */
		private Preference preference;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getNamespace() {
			return namespace;
		}

		public void setNamespace(String namespace) {
			this.namespace = namespace;
		}

		public Boolean getRefreshable() {
			return refreshable;
		}

		public void setRefreshable(Boolean refreshable) {
			this.refreshable = refreshable;
		}

		public Preference getPreference() {
			return preference;
		}

		public void setPreference(Preference preference) {
			this.preference = preference;
		}

		@Override
		public String toString() {
			return "ConfigMap{" + "name='" + name + '\'' + ", namespace='" + namespace
					+ '\'' + ", refreshable=" + refreshable + ", preference=" + preference
					+ '}';
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (o == null || getClass() != o.getClass()) {
				return false;
			}
			ConfigMap configMap = (ConfigMap) o;
			return Objects.equals(name, configMap.name)
					&& Objects.equals(namespace, configMap.namespace)
					&& Objects.equals(refreshable, configMap.refreshable)
					&& preference == configMap.preference;
		}

		@Override
		public int hashCode() {
			return Objects.hash(name, namespace, refreshable, preference);
		}
	}

	public static class Secret {
		/**
		 * Secret name.
		 */
		private String name;
		/**
		 * Namespace, using
		 * <span color="orange">{@code spring.cloud.k8s.config.namespace}</span> if not
		 * set.
		 */
		private String namespace;
		/**
		 * Whether to enable the auto refresh on current Secret, default value is
		 * {@code false}.
		 * <p>
		 * Because Secret is usually used to save sensitive information, the auto refresh
		 * function is not enabled by default. Please consider using ConfigMap if there is
		 * an auto refresh requirement.
		 */
		private Boolean refreshable = false;
		/**
		 * Config preference, using
		 * <span color="orange">{@code spring.cloud.k8s.config.preference}</span> if not
		 * set.
		 */
		private Preference preference;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getNamespace() {
			return namespace;
		}

		public void setNamespace(String namespace) {
			this.namespace = namespace;
		}

		public Boolean getRefreshable() {
			return refreshable;
		}

		public void setRefreshable(Boolean refreshable) {
			this.refreshable = refreshable;
		}

		public Preference getPreference() {
			return preference;
		}

		public void setPreference(Preference preference) {
			this.preference = preference;
		}

		@Override
		public String toString() {
			return "Secret{" + "name='" + name + '\'' + ", namespace='" + namespace + '\''
					+ ", refreshable=" + refreshable + ", preference=" + preference + '}';
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (o == null || getClass() != o.getClass()) {
				return false;
			}
			Secret secret = (Secret) o;
			return Objects.equals(name, secret.name)
					&& Objects.equals(namespace, secret.namespace)
					&& Objects.equals(refreshable, secret.refreshable)
					&& preference == secret.preference;
		}

		@Override
		public int hashCode() {
			return Objects.hash(name, namespace, refreshable, preference);
		}
	}
}
