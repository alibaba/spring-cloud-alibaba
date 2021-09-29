/*
 * Copyright 2013-2018 the original author or authors.
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
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;

import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.spring.util.PropertySourcesUtils;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.DeprecatedConfigurationProperty;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

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
@ConfigurationProperties(NacosConfigProperties.PREFIX)
public class NacosConfigProperties {

	/**
	 * Prefix of {@link NacosConfigProperties}.
	 */
	public static final String PREFIX = "spring.cloud.nacos.config";

	/**
	 * COMMAS , .
	 */
	public static final String COMMAS = ",";

	/**
	 * SEPARATOR , .
	 */
	public static final String SEPARATOR = "[,]";

	private static final Pattern PATTERN = Pattern.compile("-(\\w)");

	private static final Logger log = LoggerFactory
			.getLogger(NacosConfigProperties.class);

	@Autowired
	@JsonIgnore
	private Environment environment;

	@PostConstruct
	public void init() {
		this.overrideFromEnv();
	}

	private void overrideFromEnv() {
		if (StringUtils.isEmpty(this.getServerAddr())) {
			String serverAddr = environment
					.resolvePlaceholders("${spring.cloud.nacos.config.server-addr:}");
			if (StringUtils.isEmpty(serverAddr)) {
				serverAddr = environment.resolvePlaceholders(
						"${spring.cloud.nacos.server-addr:localhost:8848}");
			}
			this.setServerAddr(serverAddr);
		}
		if (StringUtils.isEmpty(this.getUsername())) {
			this.setUsername(
					environment.resolvePlaceholders("${spring.cloud.nacos.username:}"));
		}
		if (StringUtils.isEmpty(this.getPassword())) {
			this.setPassword(
					environment.resolvePlaceholders("${spring.cloud.nacos.password:}"));
		}
	}

	/**
	 * nacos config server address.
	 */
	private String serverAddr;

	/**
	 * the nacos authentication username.
	 */
	private String username;

	/**
	 * the nacos authentication password.
	 */
	private String password;

	/**
	 * encode for nacos config content.
	 */
	private String encode;

	/**
	 * nacos config group, group is config data meta info.
	 */
	private String group = "DEFAULT_GROUP";

	/**
	 * nacos config dataId prefix.
	 */
	private String prefix;

	/**
	 * the suffix of nacos config dataId, also the file extension of config content.
	 */
	private String fileExtension = "properties";

	/**
	 * timeout for get config from nacos.
	 */
	private int timeout = 3000;

	/**
	 * nacos maximum number of tolerable server reconnection errors.
	 */
	private String maxRetry;

	/**
	 * nacos get config long poll timeout.
	 */
	private String configLongPollTimeout;

	/**
	 * nacos get config failure retry time.
	 */
	private String configRetryTime;

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
	private String endpoint;

	/**
	 * namespace, separation configuration of different environments.
	 */
	private String namespace;

	/**
	 * access key for namespace.
	 */
	private String accessKey;

	/**
	 * secret key for namespace.
	 */
	private String secretKey;

	/**
	 * access key for namespace.
	 */
	private String ramRoleName;

	/**
	 * context path for nacos config server.
	 */
	private String contextPath;

	/**
	 * nacos config cluster name.
	 */
	private String clusterName;

	/**
	 * nacos config dataId name.
	 */
	private String name;

	/**
	 * a set of shared configurations .e.g:
	 * spring.cloud.nacos.config.shared-configs[0]=xxx .
	 */
	private List<Config> sharedConfigs;

	/**
	 * a set of extensional configurations .e.g:
	 * spring.cloud.nacos.config.extension-configs[0]=xxx .
	 */
	private List<Config> extensionConfigs;

	/**
	 * the master switch for refresh configuration, it default opened(true).
	 */
	private boolean refreshEnabled = true;

	// todo sts support

	public String getServerAddr() {
		return serverAddr;
	}

	public void setServerAddr(String serverAddr) {
		this.serverAddr = serverAddr;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getPrefix() {
		return prefix;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	public String getFileExtension() {
		return fileExtension;
	}

	public void setFileExtension(String fileExtension) {
		this.fileExtension = fileExtension;
	}

	public String getGroup() {
		return group;
	}

	public void setGroup(String group) {
		this.group = group;
	}

	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	public String getMaxRetry() {
		return maxRetry;
	}

	public void setMaxRetry(String maxRetry) {
		this.maxRetry = maxRetry;
	}

	public String getConfigLongPollTimeout() {
		return configLongPollTimeout;
	}

	public void setConfigLongPollTimeout(String configLongPollTimeout) {
		this.configLongPollTimeout = configLongPollTimeout;
	}

	public String getConfigRetryTime() {
		return configRetryTime;
	}

	public void setConfigRetryTime(String configRetryTime) {
		this.configRetryTime = configRetryTime;
	}

	public Boolean getEnableRemoteSyncConfig() {
		return enableRemoteSyncConfig;
	}

	public void setEnableRemoteSyncConfig(Boolean enableRemoteSyncConfig) {
		this.enableRemoteSyncConfig = enableRemoteSyncConfig;
	}

	public String getEndpoint() {
		return endpoint;
	}

	public void setEndpoint(String endpoint) {
		this.endpoint = endpoint;
	}

	public String getNamespace() {
		return namespace;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

	public String getAccessKey() {
		return accessKey;
	}

	public void setAccessKey(String accessKey) {
		this.accessKey = accessKey;
	}

	public String getSecretKey() {
		return secretKey;
	}

	public void setSecretKey(String secretKey) {
		this.secretKey = secretKey;
	}

	public String getRamRoleName() {
		return ramRoleName;
	}

	public void setRamRoleName(String ramRoleName) {
		this.ramRoleName = ramRoleName;
	}

	public String getEncode() {
		return encode;
	}

	public void setEncode(String encode) {
		this.encode = encode;
	}

	public String getContextPath() {
		return contextPath;
	}

	public void setContextPath(String contextPath) {
		this.contextPath = contextPath;
	}

	public String getClusterName() {
		return clusterName;
	}

	public void setClusterName(String clusterName) {
		this.clusterName = clusterName;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Environment getEnvironment() {
		return environment;
	}

	public void setEnvironment(Environment environment) {
		this.environment = environment;
	}

	public List<Config> getSharedConfigs() {
		return sharedConfigs;
	}

	public void setSharedConfigs(List<Config> sharedConfigs) {
		this.sharedConfigs = sharedConfigs;
	}

	public List<Config> getExtensionConfigs() {
		return extensionConfigs;
	}

	public void setExtensionConfigs(List<Config> extensionConfigs) {
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
	@DeprecatedConfigurationProperty(
			reason = "replaced to NacosConfigProperties#sharedConfigs and not use it at the same time.",
			replacement = PREFIX + ".shared-configs[x]")
	public String getSharedDataids() {
		return null == getSharedConfigs() ? null : getSharedConfigs().stream()
				.map(Config::getDataId).collect(Collectors.joining(COMMAS));
	}

	/**
	 * recommend to use {@link NacosConfigProperties#sharedConfigs} and not use it at the
	 * same time .
	 * @param sharedDataids the dataids for configurable multiple shared configurations ,
	 * multiple separated by commas .
	 */
	@Deprecated
	public void setSharedDataids(String sharedDataids) {
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
	@DeprecatedConfigurationProperty(
			reason = "replaced to NacosConfigProperties#sharedConfigs and not use it at the same time.",
			replacement = PREFIX + ".shared-configs[x].refresh")
	public String getRefreshableDataids() {
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
	public void setRefreshableDataids(String refreshableDataids) {
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
				.forEach((key, list) -> {
					list.stream()
							.reduce((a, b) -> new Config(a.getDataId(), a.getGroup(),
									a.isRefresh() || (b != null && b.isRefresh())))
							.ifPresent(result::add);
				});
		this.setSharedConfigs(result);
	}

	/**
	 * recommend to use
	 * {@link com.alibaba.cloud.nacos.NacosConfigProperties#extensionConfigs} and not use
	 * it at the same time .
	 * @return extensionConfigs
	 */
	@Deprecated
	@DeprecatedConfigurationProperty(
			reason = "replaced to NacosConfigProperties#extensionConfigs and not use it at the same time .",
			replacement = PREFIX + ".extension-configs[x]")
	public List<Config> getExtConfig() {
		return this.getExtensionConfigs();
	}

	@Deprecated
	public void setExtConfig(List<Config> extConfig) {
		this.setExtensionConfigs(extConfig);
	}

	/**
	 * recommend to use {@link NacosConfigManager#getConfigService()}.
	 * @return ConfigService
	 */
	@Deprecated
	public ConfigService configServiceInstance() {
		// The following code will be migrated
		return NacosConfigManager.createConfigService(this);
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
		properties.put(NAMESPACE, Objects.toString(this.namespace, ""));
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
		} else {
			properties.put(ENDPOINT, endpoint);
		}

		enrichNacosConfigProperties(properties);
		return properties;
	}

	private void enrichNacosConfigProperties(Properties nacosConfigProperties) {
		Map<String, Object> properties = PropertySourcesUtils
				.getSubProperties((ConfigurableEnvironment) environment, PREFIX);
		properties.forEach((k, v) -> nacosConfigProperties.putIfAbsent(resolveKey(k),
				String.valueOf(v)));
	}

	private String resolveKey(String key) {
		Matcher matcher = PATTERN.matcher(key);
		StringBuffer sb = new StringBuffer();
		while (matcher.find()) {
			matcher.appendReplacement(sb, matcher.group(1).toUpperCase());
		}
		matcher.appendTail(sb);
		return sb.toString();
	}

	@Override
	public String toString() {
		return "NacosConfigProperties{" + "serverAddr='" + serverAddr + '\''
				+ ", encode='" + encode + '\'' + ", group='" + group + '\'' + ", prefix='"
				+ prefix + '\'' + ", fileExtension='" + fileExtension + '\''
				+ ", timeout=" + timeout + ", maxRetry='" + maxRetry + '\''
				+ ", configLongPollTimeout='" + configLongPollTimeout + '\''
				+ ", configRetryTime='" + configRetryTime + '\''
				+ ", enableRemoteSyncConfig=" + enableRemoteSyncConfig + ", endpoint='"
				+ endpoint + '\'' + ", namespace='" + namespace + '\'' + ", accessKey='"
				+ accessKey + '\'' + ", secretKey='" + secretKey + '\''
				+ ", ramRoleName='" + ramRoleName + '\''
				+ ", contextPath='" + contextPath + '\'' + ", clusterName='" + clusterName
				+ '\'' + ", name='" + name + '\'' + '\'' + ", shares=" + sharedConfigs
				+ ", extensions=" + extensionConfigs + ", refreshEnabled="
				+ refreshEnabled + '}';
	}

	public static class Config {

		/**
		 * the data id of extended configuration.
		 */
		private String dataId;

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

		public String getDataId() {
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
