/*
 * Copyright 2013-present the original author or authors.
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

package com.alibaba.cloud.nacos;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.alibaba.cloud.nacos.utils.PropertySourcesUtils;
import com.alibaba.cloud.nacos.utils.StringUtils;
import com.alibaba.nacos.api.config.ConfigService;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.jspecify.annotations.Nullable;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.DeprecatedConfigurationProperty;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;

import static com.alibaba.nacos.api.PropertyKeyConst.ACCESS_KEY;
import static com.alibaba.nacos.api.PropertyKeyConst.CLUSTER_NAME;
import static com.alibaba.nacos.api.PropertyKeyConst.CONFIG_LONG_POLL_TIMEOUT;
import static com.alibaba.nacos.api.PropertyKeyConst.CONFIG_RETRY_TIME;
import static com.alibaba.nacos.api.PropertyKeyConst.ENABLE_REMOTE_SYNC_CONFIG;
import static com.alibaba.nacos.api.PropertyKeyConst.ENCODE;
import static com.alibaba.nacos.api.PropertyKeyConst.ENDPOINT;
import static com.alibaba.nacos.api.PropertyKeyConst.ENDPOINT_PORT;
import static com.alibaba.nacos.api.PropertyKeyConst.MAX_RETRY;
import static com.alibaba.nacos.api.PropertyKeyConst.NAMESPACE;
import static com.alibaba.nacos.api.PropertyKeyConst.PASSWORD;
import static com.alibaba.nacos.api.PropertyKeyConst.RAM_ROLE_NAME;
import static com.alibaba.nacos.api.PropertyKeyConst.SECRET_KEY;
import static com.alibaba.nacos.api.PropertyKeyConst.SERVER_ADDR;
import static com.alibaba.nacos.api.PropertyKeyConst.USERNAME;

/**
 * Nacos properties.
 *
 * @author leijuan
 * @author xiaojing
 * @author pbting
 * @author <a href="mailto:lyuzb@lyuzb.com">lyuzb</a>
 */
public class NacosConfigProperties {

	/**
	 * COMMAS , .
	 */
	public static final String COMMAS = ",";

	/**
	 * SEPARATOR , .
	 */
	public static final String SEPARATOR = "[,]";

	/**
	 * Nacos default namespace .
	 */
	public static final String DEFAULT_NAMESPACE = "public";

	/**
	 * Nacos default server and port.
	 */
	public static final String DEFAULT_ADDRESS = "127.0.0.1:8848";

	private static final Pattern PATTERN = Pattern.compile("-(\\w)");

	private static final Logger log = LoggerFactory
			.getLogger(NacosConfigProperties.class);

	@Autowired
	@JsonIgnore
	private @Nullable Environment environment;
	/**
	 * nacos config server address.
	 */
	private @Nullable String serverAddr;
	/**
	 * the nacos authentication username.
	 */
	private @Nullable String username;
	/**
	 * the nacos authentication password.
	 */
	private @Nullable String password;
	/**
	 * encode for nacos config content.
	 */
	private @Nullable String encode;
	/**
	 * nacos config group, group is config data meta info.
	 */
	private @Nullable String group = "DEFAULT_GROUP";
	/**
	 * nacos config dataId prefix.
	 */
	private @Nullable String prefix;
	/**
	 * the suffix of nacos config dataId, also the file extension of config content.
	 */
	private @Nullable String fileExtension = "properties";
	/**
	 * timeout for get config from nacos.
	 */
	private int timeout = 3000;
	/**
	 * nacos maximum number of tolerable server reconnection errors.
	 */
	private @Nullable String maxRetry;
	/**
	 * nacos get config long poll timeout.
	 */
	private @Nullable String configLongPollTimeout;
	/**
	 * nacos get config failure retry time.
	 */
	private @Nullable String configRetryTime;
	/**
	 * If you want to pull it yourself when the program starts to get the configuration
	 * for the first time, and the registered Listener is used for future configuration
	 * updates, you can keep the original code unchanged, just add the system parameter:
	 * enableRemoteSyncConfig = "true" ( But there is network overhead); therefore we
	 * recommend that you use {@link ConfigService#getConfigAndSignListener} directly.
	 */
	private boolean enableRemoteSyncConfig = false;
	/**
	 * endpoint for Nacos, the domain name of a service, through which the server address
	 * can be dynamically obtained.
	 */
	private @Nullable String endpoint;
	/**
	 * namespace, separation configuration of different environments.
	 */
	private @Nullable String namespace;
	/**
	 * access key for namespace.
	 */
	private @Nullable String accessKey;
	/**
	 * secret key for namespace.
	 */
	private @Nullable String secretKey;
	/**
	 * role name for aliyun ram.
	 */
	private @Nullable String ramRoleName;
	/**
	 * context path for nacos config server.
	 */
	private @Nullable String contextPath;
	/**
	 * nacos config cluster name.
	 */
	private @Nullable String clusterName;
	/**
	 * nacos config dataId name.
	 */
	private @Nullable String name;
	/**
	 * a set of shared configurations .e.g:
	 * spring.cloud.nacos.config.shared-configs[0]=xxx .
	 */
	private @Nullable List<Config> sharedConfigs;
	/**
	 * a set of extensional configurations .e.g:
	 * spring.cloud.nacos.config.extension-configs[0]=xxx .
	 */
	private @Nullable List<Config> extensionConfigs;
	/**
	 * the master switch for refresh configuration, it default opened(true).
	 */
	private boolean refreshEnabled = true;

	@PostConstruct
	public void init() {
		this.overrideFromEnv();
	}

	private void overrideFromEnv() {
		if (environment == null) {
			return;
		}

		String prefix = NacosPropertiesPrefixer.getPrefix(environment);

		if (StringUtils.isEmpty(this.getServerAddr())) {
			String serverAddr = environment
					.resolvePlaceholders("${" + prefix + ".config.server-addr:}");
			if (StringUtils.isEmpty(serverAddr)) {
				serverAddr = environment.resolvePlaceholders(
						"${" + prefix + ".server-addr:127.0.0.1:8848}");
			}
			this.setServerAddr(serverAddr);
		}
		if (StringUtils.isEmpty(this.getUsername())) {
			this.setUsername(
					environment.resolvePlaceholders("${" + prefix + ".username:}"));
		}
		if (StringUtils.isEmpty(this.getPassword())) {
			this.setPassword(
					environment.resolvePlaceholders("${" + prefix + ".password:}"));
		}
	}

	// todo sts support

	public @Nullable String getServerAddr() {
		return serverAddr;
	}

	public void setServerAddr(@Nullable String serverAddr) {
		this.serverAddr = serverAddr;
	}

	public @Nullable String getUsername() {
		return username;
	}

	public void setUsername(@Nullable String username) {
		this.username = username;
	}

	public @Nullable String getPassword() {
		return password;
	}

	public void setPassword(@Nullable String password) {
		this.password = password;
	}

	public @Nullable String getPrefix() {
		return prefix;
	}

	public void setPrefix(@Nullable String prefix) {
		this.prefix = prefix;
	}

	public @Nullable String getFileExtension() {
		return fileExtension;
	}

	public void setFileExtension(@Nullable String fileExtension) {
		this.fileExtension = fileExtension;
	}

	public @Nullable String getGroup() {
		return group;
	}

	public void setGroup(@Nullable String group) {
		this.group = group;
	}

	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	public @Nullable String getMaxRetry() {
		return maxRetry;
	}

	public void setMaxRetry(@Nullable String maxRetry) {
		this.maxRetry = maxRetry;
	}

	public @Nullable String getConfigLongPollTimeout() {
		return configLongPollTimeout;
	}

	public void setConfigLongPollTimeout(@Nullable String configLongPollTimeout) {
		this.configLongPollTimeout = configLongPollTimeout;
	}

	public @Nullable String getConfigRetryTime() {
		return configRetryTime;
	}

	public void setConfigRetryTime(@Nullable String configRetryTime) {
		this.configRetryTime = configRetryTime;
	}

	public Boolean getEnableRemoteSyncConfig() {
		return enableRemoteSyncConfig;
	}

	public void setEnableRemoteSyncConfig(@Nullable Boolean enableRemoteSyncConfig) {
		this.enableRemoteSyncConfig = Boolean.TRUE.equals(enableRemoteSyncConfig);
	}

	public @Nullable String getEndpoint() {
		return endpoint;
	}

	public void setEndpoint(@Nullable String endpoint) {
		this.endpoint = endpoint;
	}

	public @Nullable String getNamespace() {
		return namespace;
	}

	public void setNamespace(@Nullable String namespace) {
		this.namespace = namespace;
	}

	public @Nullable String getAccessKey() {
		return accessKey;
	}

	public void setAccessKey(@Nullable String accessKey) {
		this.accessKey = accessKey;
	}

	public @Nullable String getSecretKey() {
		return secretKey;
	}

	public void setSecretKey(@Nullable String secretKey) {
		this.secretKey = secretKey;
	}

	public @Nullable String getRamRoleName() {
		return ramRoleName;
	}

	public void setRamRoleName(@Nullable String ramRoleName) {
		this.ramRoleName = ramRoleName;
	}

	public @Nullable String getEncode() {
		return encode;
	}

	public void setEncode(@Nullable String encode) {
		this.encode = encode;
	}

	public @Nullable String getContextPath() {
		return contextPath;
	}

	public void setContextPath(@Nullable String contextPath) {
		this.contextPath = contextPath;
	}

	public @Nullable String getClusterName() {
		return clusterName;
	}

	public void setClusterName(@Nullable String clusterName) {
		this.clusterName = clusterName;
	}

	public @Nullable String getName() {
		return name;
	}

	public void setName(@Nullable String name) {
		this.name = name;
	}

	public @Nullable Environment getEnvironment() {
		return environment;
	}

	public void setEnvironment(@Nullable Environment environment) {
		this.environment = environment;
	}

	public @Nullable List<Config> getSharedConfigs() {
		return sharedConfigs;
	}

	public void setSharedConfigs(@Nullable List<Config> sharedConfigs) {
		this.sharedConfigs = sharedConfigs;
	}

	public @Nullable List<Config> getExtensionConfigs() {
		return extensionConfigs;
	}

	public void setExtensionConfigs(@Nullable List<Config> extensionConfigs) {
		this.extensionConfigs = extensionConfigs;
	}

	public boolean isRefreshEnabled() {
		return refreshEnabled;
	}

	public void setRefreshEnabled(boolean refreshEnabled) {
		this.refreshEnabled = refreshEnabled;
	}

	/**
	 * recommend to use {@link NacosConfigProperties#sharedConfigs} .
	 * @return string
	 */
	@Deprecated
	@DeprecatedConfigurationProperty(reason = "use spring.config.import instead")
	public @Nullable String getSharedDataids() {
		return null == getSharedConfigs() ? null
				: getSharedConfigs().stream().map(Config::getDataId)
				.collect(Collectors.joining(COMMAS));
	}

	/**
	 * recommend to use {@link NacosConfigProperties#sharedConfigs} and not use it at the
	 * same time .
	 * @param sharedDataids the dataids for configurable multiple shared configurations ,
	 *     multiple separated by commas .
	 */
	@Deprecated
	public void setSharedDataids(@Nullable String sharedDataids) {
		if (null != sharedDataids && sharedDataids.trim().length() > 0) {
			List<Config> list = new ArrayList<>();
			Stream.of(sharedDataids.split(SEPARATOR))
					.forEach(dataId -> list.add(new Config(dataId.trim())));
			this.compatibleSharedConfigs(list);
		}
	}

	/**
	 * Not providing support,the need to refresh is specified by the respective refresh
	 * configuration and not use it at the same time .
	 * @return string
	 */
	@Deprecated
	public @Nullable String getRefreshableDataids() {
		return null == getSharedConfigs() ? null
				: getSharedConfigs().stream().filter(Config::isRefresh)
				.map(Config::getDataId).collect(Collectors.joining(COMMAS));
	}

	/**
	 * Not providing support,the need to refresh is specified by the respective refresh
	 * configuration and not use it at the same time .
	 * @param refreshableDataids refreshable dataids ,multiple separated by commas .
	 */
	@Deprecated
	public void setRefreshableDataids(@Nullable String refreshableDataids) {
		if (null != refreshableDataids && refreshableDataids.trim().length() > 0) {
			List<Config> list = new ArrayList<>();
			Stream.of(refreshableDataids.split(SEPARATOR)).forEach(
					dataId -> list.add(new Config(dataId.trim()).setRefresh(true)));
			this.compatibleSharedConfigs(list);
		}
	}

	private void compatibleSharedConfigs(List<Config> configList) {
		if (null != this.getSharedConfigs()) {
			configList.addAll(this.getSharedConfigs());
		}
		List<Config> result = new ArrayList<>();
		configList.stream()
				.collect(Collectors.groupingBy(cfg -> (cfg.getGroup() + cfg.getDataId()),
						LinkedHashMap::new, Collectors.toList()))
				.forEach((key, list) -> list.stream()
					.reduce((a, b) -> new Config(a.getDataId(), a.getGroup(),
								a.isRefresh() || (b != null && b.isRefresh())))
						.ifPresent(result::add));
		this.setSharedConfigs(result);
	}

	/**
	 * recommend to use
	 * {@link com.alibaba.cloud.nacos.NacosConfigProperties#extensionConfigs} and not use
	 * it at the same time .
	 * @return extensionConfigs
	 */
	@Deprecated
	@DeprecatedConfigurationProperty(reason = "use spring.config.import instead")
	public @Nullable List<Config> getExtConfig() {
		return this.getExtensionConfigs();
	}

	@Deprecated
	public void setExtConfig(@Nullable List<Config> extConfig) {
		this.setExtensionConfigs(extConfig);
	}

	/**
	 * recommend to use {@link NacosConfigManager#getConfigService()}.
	 * @return ConfigService
	 */
	@Deprecated
	public ConfigService configServiceInstance() {
		// The following code will be migrated
		return NacosConfigManager.getInstance(this).getConfigService();
	}

	/**
	 * recommend to use {@link NacosConfigProperties#assembleConfigServiceProperties()}.
	 * @return ConfigServiceProperties
	 */
	@Deprecated
	public Properties getConfigServiceProperties() {
		return this.assembleConfigServiceProperties();
	}

	/**
	 * assemble properties for configService. (cause by rename : Remove the interference
	 * of auto prompts when writing,because autocue is based on get method.
	 * @return properties
	 */
	public Properties assembleConfigServiceProperties() {
		Properties properties = new Properties();
		properties.put(SERVER_ADDR, Objects.toString(this.serverAddr, ""));
		properties.put(USERNAME, Objects.toString(this.username, ""));
		properties.put(PASSWORD, Objects.toString(this.password, ""));
		properties.put(ENCODE, Objects.toString(this.encode, ""));
		properties.put(NAMESPACE, this.resolveNamespace());
		properties.put(ACCESS_KEY, Objects.toString(this.accessKey, ""));
		properties.put(SECRET_KEY, Objects.toString(this.secretKey, ""));
		properties.put(RAM_ROLE_NAME, Objects.toString(this.ramRoleName, ""));
		properties.put(CLUSTER_NAME, Objects.toString(this.clusterName, ""));
		properties.put(MAX_RETRY, Objects.toString(this.maxRetry, ""));
		properties.put(CONFIG_LONG_POLL_TIMEOUT,
				Objects.toString(this.configLongPollTimeout, ""));
		properties.put(CONFIG_RETRY_TIME, Objects.toString(this.configRetryTime, ""));
		properties.put(ENABLE_REMOTE_SYNC_CONFIG,
				Objects.toString(this.enableRemoteSyncConfig, ""));
		String endpoint = Objects.toString(this.endpoint, "");
		if (endpoint.contains(":")) {
			int index = endpoint.indexOf(":");
			properties.put(ENDPOINT, endpoint.substring(0, index));
			properties.put(ENDPOINT_PORT, endpoint.substring(index + 1));
		}
		else {
			properties.put(ENDPOINT, endpoint);
		}

		enrichNacosConfigProperties(properties);

		// set default value when serverAddr and endpoint is empty
		if (StringUtils.isEmpty(this.serverAddr) && StringUtils.isEmpty(this.endpoint)) {
			properties.put(SERVER_ADDR, DEFAULT_ADDRESS);
		}

		return properties;
	}

	/**
	 * refer
	 * https://github.com/alibaba/spring-cloud-alibaba/issues/2872
	 * https://github.com/alibaba/spring-cloud-alibaba/issues/2869 .
	 */
	private String resolveNamespace() {
		if (DEFAULT_NAMESPACE.equals(this.namespace)) {
			log.info("set nacos config namespace 'public' to ''");
			return "";
		}
		else {
			return Objects.toString(this.namespace, "");
		}
	}

	protected void enrichNacosConfigProperties(Properties nacosConfigProperties) {
		if (environment == null) {
			return;
		}
		String prefix = NacosPropertiesPrefixer.getPrefix(environment);

		Map<String, Object> properties = PropertySourcesUtils
				.getSubProperties((ConfigurableEnvironment) environment, prefix + ".config");
		properties.forEach((k, v) -> nacosConfigProperties.putIfAbsent(resolveKey(k),
				String.valueOf(v)));
	}

	protected String resolveKey(String key) {
		Matcher matcher = PATTERN.matcher(key);
		StringBuffer sb = new StringBuffer();
		while (matcher.find()) {
			matcher.appendReplacement(sb, matcher.group(1).toUpperCase(Locale.ROOT));
		}
		matcher.appendTail(sb);
		return sb.toString();
	}

	/**
	 * refer
	 * https://github.com/alibaba/spring-cloud-alibaba/issues/4242
	 * Mask sensitive fields in logs to avoid credential leakage.
	 */
	private static @Nullable String mask(@Nullable String value) {
		return (value == null || value.isEmpty()) ? value : "******";
	}

	@Override
	public String toString() {
		return "NacosConfigProperties{"
				+ "serverAddr='" + serverAddr + '\''
				+ ", encode='" + encode + '\''
				+ ", group='" + group + '\''
				+ ", prefix='" + prefix + '\''
				+ ", fileExtension='" + fileExtension + '\''
				+ ", timeout=" + timeout
				+ ", maxRetry='" + maxRetry + '\''
				+ ", configLongPollTimeout='" + configLongPollTimeout + '\''
				+ ", configRetryTime='" + configRetryTime + '\''
				+ ", enableRemoteSyncConfig=" + enableRemoteSyncConfig
				+ ", endpoint='" + endpoint + '\''
				+ ", namespace='" + namespace + '\''
				+ ", accessKey='" + mask(accessKey) + '\''
				+ ", secretKey='" + mask(secretKey) + '\''
				+ ", ramRoleName='" + ramRoleName + '\''
				+ ", contextPath='" + contextPath + '\''
				+ ", clusterName='" + clusterName + '\''
				+ ", name='" + name + '\''
				+ ", shares=" + sharedConfigs
				+ ", extensions=" + extensionConfigs
				+ ", refreshEnabled=" + refreshEnabled
				+ '}';
	}

	public static class Config {

		/**
		 * the data id of extended configuration.
		 */
		private @Nullable String dataId;

		/**
		 * the group of extended configuration, the default value is DEFAULT_GROUP.
		 */
		private String group = "DEFAULT_GROUP";

		/**
		 * whether to support dynamic refresh, the default does not support .
		 */
		private boolean refresh = false;

		public Config() {
		}

		public Config(String dataId) {
			this.dataId = dataId;
		}

		public Config(String dataId, String group) {
			this(dataId);
			this.group = group;
		}

		public Config(String dataId, boolean refresh) {
			this(dataId);
			this.refresh = refresh;
		}

		public Config(String dataId, String group, boolean refresh) {
			this(dataId, group);
			this.refresh = refresh;
		}

		public @Nullable String getDataId() {
			return dataId;
		}

		public Config setDataId(String dataId) {
			this.dataId = dataId;
			return this;
		}

		public String getGroup() {
			return group;
		}

		public Config setGroup(String group) {
			this.group = group;
			return this;
		}

		public boolean isRefresh() {
			return refresh;
		}

		public Config setRefresh(boolean refresh) {
			this.refresh = refresh;
			return this;
		}

		@Override
		public String toString() {
			return "Config{" + "dataId='" + dataId + '\'' + ", group='" + group + '\''
					+ ", refresh=" + refresh + '}';
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (o == null || getClass() != o.getClass()) {
				return false;
			}
			Config config = (Config) o;
			return refresh == config.refresh && Objects.equals(dataId, config.dataId)
					&& Objects.equals(group, config.group);
		}

		@Override
		public int hashCode() {
			return Objects.hash(dataId, group, refresh);
		}

	}

}
