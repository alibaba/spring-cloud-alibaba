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

import java.util.Objects;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.exception.NacosException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.util.StringUtils;

import static com.alibaba.nacos.api.PropertyKeyConst.ENDPOINT;
import static com.alibaba.nacos.api.PropertyKeyConst.ENDPOINT_PORT;

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

	private static final Pattern PATTERN = Pattern.compile("-(\\w)");

	private static final Logger log = LoggerFactory
			.getLogger(NacosConfigProperties.class);

	@Autowired
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
				serverAddr = environment
						.resolvePlaceholders("${spring.cloud.nacos.server-addr:}");
			}
			this.setServerAddr(serverAddr);
		}
	}

	/**
	 * nacos config server address.
	 */
	private String serverAddr;

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
	 * the dataids for configurable multiple shared configurations , multiple separated by
	 * commas .
	 */
	private String sharedDataids;

	/**
	 * refreshable dataids , multiple separated by commas .
	 */
	private String refreshableDataids;

	/**
	 * a set of extended configurations .
	 */
	private List<Config> extConfig;

	private static ConfigService configService;

	// todo sts support

	public String getServerAddr() {
		return serverAddr;
	}

	public void setServerAddr(String serverAddr) {
		this.serverAddr = serverAddr;
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

	public String getSharedDataids() {
		return sharedDataids;
	}

	public void setSharedDataids(String sharedDataids) {
		this.sharedDataids = sharedDataids;
	}

	public String getRefreshableDataids() {
		return refreshableDataids;
	}

	public void setRefreshableDataids(String refreshableDataids) {
		this.refreshableDataids = refreshableDataids;
	}

	public List<Config> getExtConfig() {
		return extConfig;
	}

	public void setExtConfig(List<Config> extConfig) {
		this.extConfig = extConfig;
	}

	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return ConfigService
	 */
	@Deprecated
	public ConfigService configServiceInstance() {
		if (null == configService) {
			try {
				configService = NacosFactory
						.createConfigService(getConfigServiceProperties());
			}
			catch (NacosException e) {
				log.error("create naming service error!properties={},e=,", this, e);
				return null;
			}
		}
		return configService;
	}

	public Properties getConfigServiceProperties() {
		Properties properties = new Properties();
		String endpoint = Objects.toString(this.endpoint, "");
		if (endpoint.contains(":")) {
			int index = endpoint.indexOf(":");
			properties.put(ENDPOINT, endpoint.substring(0, index));
			properties.put(ENDPOINT_PORT, endpoint.substring(index + 1));
		}
		else {
			properties.put(ENDPOINT, endpoint);
		}

		enrichNacosProperties(properties);
		return properties;
	}

	@Override
	public String toString() {
		return "NacosConfigProperties{" + "serverAddr='" + serverAddr + '\''
				+ ", encode='" + encode + '\'' + ", group='" + group + '\'' + ", prefix='"
				+ prefix + '\'' + ", fileExtension='" + fileExtension + '\''
				+ ", timeout=" + timeout + ", endpoint='" + endpoint + '\''
				+ ", namespace='" + namespace + '\'' + ", accessKey='" + accessKey + '\''
				+ ", secretKey='" + secretKey + '\'' + ", contextPath='" + contextPath
				+ '\'' + ", clusterName='" + clusterName + '\'' + ", name='" + name + '\''
				+ ", sharedDataids='" + sharedDataids + '\'' + ", refreshableDataids='"
				+ refreshableDataids + '\'' + ", extConfig=" + extConfig + '}';
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

		public String getDataId() {
			return dataId;
		}

		public void setDataId(String dataId) {
			this.dataId = dataId;
		}

		public String getGroup() {
			return group;
		}

		public void setGroup(String group) {
			this.group = group;
		}

		public boolean isRefresh() {
			return refresh;
		}

		public void setRefresh(boolean refresh) {
			this.refresh = refresh;
		}

	}

	private void enrichNacosProperties(Properties properties) {
		Map<String, Object> configurationItem = getConfigurationItemFromEnv(PREFIX);
		configurationItem.forEach((k, v) -> {
			if (!properties.contains(k)) {
				properties.put(k, v);
			}
		});
	}

	private Map<String, Object> getConfigurationItemFromEnv(String prefix) {
		Map<String, Object> configurationItems = new HashMap<>();
		ConfigurableEnvironment configurableEnvironment = (ConfigurableEnvironment) environment;

		MutablePropertySources propertySources = configurableEnvironment.getPropertySources();
		propertySources.stream().forEach(propertySource -> {
			if (propertySource instanceof MapPropertySource) {
				MapPropertySource mps = (MapPropertySource) propertySource;
				mps.getSource().forEach((key, value) -> {
					if (StringUtils.startsWithIgnoreCase(key, prefix)) {
						configurationItems.put(resolveKey(key.substring(prefix.length() + 1)), value);
					}
				});
			}
		});
		return configurationItems;
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

}
