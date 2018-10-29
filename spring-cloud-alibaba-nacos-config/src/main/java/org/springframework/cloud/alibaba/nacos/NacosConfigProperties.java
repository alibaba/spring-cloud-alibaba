/*
 * Copyright (C) 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.alibaba.nacos;

import java.util.Properties;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.config.ConfigService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

import static com.alibaba.nacos.api.PropertyKeyConst.ACCESS_KEY;
import static com.alibaba.nacos.api.PropertyKeyConst.CLUSTER_NAME;
import static com.alibaba.nacos.api.PropertyKeyConst.CONTEXT_PATH;
import static com.alibaba.nacos.api.PropertyKeyConst.ENCODE;
import static com.alibaba.nacos.api.PropertyKeyConst.ENDPOINT;
import static com.alibaba.nacos.api.PropertyKeyConst.NAMESPACE;
import static com.alibaba.nacos.api.PropertyKeyConst.SECRET_KEY;
import static com.alibaba.nacos.api.PropertyKeyConst.SERVER_ADDR;

/**
 * nacos properties
 *
 * @author leijuan
 * @author xiaojing
 */
@ConfigurationProperties("spring.cloud.nacos.config")
public class NacosConfigProperties {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(NacosConfigProperties.class);

	/**
	 * nacos config server address
	 */
	private String serverAddr = "";

	/**
	 * encode for nacos config content.
	 */
	private String encode = "";

	/**
	 * nacos config group, group is config data meta info.
	 */
	private String group = "DEFAULT_GROUP";

	/**
	 * nacos config dataId prefix
	 */
	private String prefix = "";
	/**
	 * the suffix of nacos config dataId, also the file extension of config content.
	 */
	private String fileExtension = "properties";

	/**
	 * timeout for get config from nacos.
	 */
	private int timeout = 3000;

	/**
	 * endpoint for Nacos, the domain name of a service, through which the server address
	 * can be dynamically obtained.
	 */
	private String endpoint = "";

	/**
	 * namespace, separation configuration of different environments.
	 */
	private String namespace = "";

	/**
	 * access key for namespace.
	 */
	private String accessKey = "";

	/**
	 * secret key for namespace.
	 */
	private String secretKey = "";

	/**
	 * context path for nacos config server.
	 */
	private String contextPath = "";

	/**
	 * nacos config cluster name
	 */
	private String clusterName = "";

	private ConfigService configService;

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

	@Override
	public String toString() {
		return "NacosConfigProperties{" + "serverAddr='" + serverAddr + '\''
				+ ", encode='" + encode + '\'' + ", group='" + group + '\'' + ", prefix='"
				+ prefix + '\'' + ", fileExtension='" + fileExtension + '\''
				+ ", timeout=" + timeout + ", endpoint='" + endpoint + '\''
				+ ", namespace='" + namespace + '\'' + ", accessKey='" + accessKey + '\''
				+ ", secretKey='" + secretKey + '\'' + ", contextPath='" + contextPath
				+ '\'' + ", clusterName='" + clusterName + '\'' + '}';
	}

	public void overrideFromEnv(Environment env) {

		if (StringUtils.isEmpty(this.getServerAddr())) {
			this.setServerAddr(
					env.resolvePlaceholders("${spring.cloud.nacos.config.server-addr:}"));
		}
		if (StringUtils.isEmpty(this.getEncode())) {
			this.setEncode(
					env.resolvePlaceholders("${spring.cloud.nacos.config.encode:}"));
		}
		if (StringUtils.isEmpty(this.getNamespace())) {
			this.setNamespace(
					env.resolvePlaceholders("${spring.cloud.nacos.config.namespace:}"));
		}
		if (StringUtils.isEmpty(this.getAccessKey())) {
			this.setAccessKey(
					env.resolvePlaceholders("${spring.cloud.nacos.config.access-key:}"));
		}
		if (StringUtils.isEmpty(this.getSecretKey())) {
			this.setSecretKey(
					env.resolvePlaceholders("${spring.cloud.nacos.config.secret-key:}"));
		}
		if (StringUtils.isEmpty(this.getContextPath())) {
			this.setContextPath(env
					.resolvePlaceholders("${spring.cloud.nacos.config.context-path:}"));
		}
		if (StringUtils.isEmpty(this.getClusterName())) {
			this.setClusterName(env
					.resolvePlaceholders("${spring.cloud.nacos.config.cluster-name:}"));
		}
		if (StringUtils.isEmpty(this.getEndpoint())) {
			this.setEndpoint(
					env.resolvePlaceholders("${spring.cloud.nacos.config.endpoint:}"));
		}
		if (StringUtils.isEmpty(this.getPrefix())) {
			this.setPrefix(
					env.resolvePlaceholders("${spring.cloud.nacos.config.prefix:}"));
		}
	}

	public ConfigService configServiceInstance() {

		if (null != configService) {
			return configService;
		}

		Properties properties = new Properties();
		properties.put(SERVER_ADDR, this.serverAddr);
		properties.put(ENCODE, this.encode);
		properties.put(NAMESPACE, this.namespace);
		properties.put(ACCESS_KEY, this.accessKey);
		properties.put(SECRET_KEY, this.secretKey);
		properties.put(CONTEXT_PATH, this.contextPath);
		properties.put(CLUSTER_NAME, this.clusterName);
		properties.put(ENDPOINT, this.endpoint);
		try {
			configService = NacosFactory.createConfigService(properties);
			return configService;
		}
		catch (Exception e) {
			LOGGER.error("create config service error!properties={},e=,", this, e);
			return null;
		}
	}
}
