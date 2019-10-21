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
import java.util.Properties;

import com.alibaba.cloud.nacos.diagnostics.analyzer.NacosConnectionFailureException;
import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.exception.NacosException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.alibaba.nacos.api.PropertyKeyConst.ACCESS_KEY;
import static com.alibaba.nacos.api.PropertyKeyConst.CLUSTER_NAME;
import static com.alibaba.nacos.api.PropertyKeyConst.CONFIG_LONG_POLL_TIMEOUT;
import static com.alibaba.nacos.api.PropertyKeyConst.CONFIG_RETRY_TIME;
import static com.alibaba.nacos.api.PropertyKeyConst.CONTEXT_PATH;
import static com.alibaba.nacos.api.PropertyKeyConst.ENABLE_REMOTE_SYNC_CONFIG;
import static com.alibaba.nacos.api.PropertyKeyConst.ENCODE;
import static com.alibaba.nacos.api.PropertyKeyConst.ENDPOINT;
import static com.alibaba.nacos.api.PropertyKeyConst.ENDPOINT_PORT;
import static com.alibaba.nacos.api.PropertyKeyConst.MAX_RETRY;
import static com.alibaba.nacos.api.PropertyKeyConst.NAMESPACE;
import static com.alibaba.nacos.api.PropertyKeyConst.SECRET_KEY;
import static com.alibaba.nacos.api.PropertyKeyConst.SERVER_ADDR;

/**
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 */
public class NacosConfigManager {

	private static final Logger log = LoggerFactory.getLogger(NacosConfigManager.class);

	private static ConfigService service = null;

	private NacosConfigProperties nacosConfigProperties;

	public NacosConfigManager(NacosConfigProperties nacosConfigProperties) {
		this.nacosConfigProperties = nacosConfigProperties;
	}

	public ConfigService getConfigService() {
		if (Objects.isNull(service)) {
			try {
				service = NacosFactory
						.createConfigService(this.assembleConfigServiceProperties());
			}
			catch (NacosException e) {
				throw new NacosConnectionFailureException(
						nacosConfigProperties.getServerAddr(), e.getMessage(), e);
			}
		}
		return service;
	}

	/**
	 * assemble properties for configService. (cause by rename : Remove the interference
	 * of auto prompts when writing,because autocue is based on get method.
	 */
	private Properties assembleConfigServiceProperties() {
		Properties properties = new Properties();
		properties.put(SERVER_ADDR,
				Objects.toString(nacosConfigProperties.getServerAddr(), ""));
		properties.put(ENCODE, Objects.toString(nacosConfigProperties.getEncode(), ""));
		properties.put(NAMESPACE,
				Objects.toString(nacosConfigProperties.getNamespace(), ""));
		properties.put(ACCESS_KEY,
				Objects.toString(nacosConfigProperties.getAccessKey(), ""));
		properties.put(SECRET_KEY,
				Objects.toString(nacosConfigProperties.getSecretKey(), ""));
		properties.put(CONTEXT_PATH,
				Objects.toString(nacosConfigProperties.getContextPath(), ""));
		properties.put(CLUSTER_NAME,
				Objects.toString(nacosConfigProperties.getClusterName(), ""));
		properties.put(MAX_RETRY,
				Objects.toString(nacosConfigProperties.getMaxRetry(), ""));
		properties.put(CONFIG_LONG_POLL_TIMEOUT,
				Objects.toString(nacosConfigProperties.getConfigLongPollTimeout(), ""));
		properties.put(CONFIG_RETRY_TIME,
				Objects.toString(nacosConfigProperties.getConfigRetryTime(), ""));
		properties.put(ENABLE_REMOTE_SYNC_CONFIG,
				Objects.toString(nacosConfigProperties.getEnableRemoteSyncConfig(), ""));
		String endpoint = Objects.toString(nacosConfigProperties.getEndpoint(), "");
		if (endpoint.contains(":")) {
			int index = endpoint.indexOf(":");
			properties.put(ENDPOINT, endpoint.substring(0, index));
			properties.put(ENDPOINT_PORT, endpoint.substring(index + 1));
		}
		else {
			properties.put(ENDPOINT, endpoint);
		}
		return properties;
	}

	public NacosConfigProperties getNacosConfigProperties() {
		return nacosConfigProperties;
	}

}
