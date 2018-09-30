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

package org.springframework.cloud.alibaba.oss;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;

import com.aliyun.oss.ClientBuilderConfiguration;

/**
 * {@link ConfigurationProperties} for configuring OSS.
 *
 * @author <a href="mailto:fangjian0423@gmail.com">Jim</a>
 */
@ConfigurationProperties(prefix = OSSConstants.PREFIX)
public class OSSProperties {

	private static final Logger logger = LoggerFactory.getLogger(OSSProperties.class);

	public static final Map<String, String> endpointMap = new HashMap<>();

	static {
		endpointMap.put("cn-beijing", "http://oss-cn-beijing.aliyuncs.com");
		endpointMap.put("cn-qingdao", "http://oss-cn-qingdao.aliyuncs.com");
		endpointMap.put("cn-hangzhou", "http://oss-cn-hangzhou.aliyuncs.com");
		endpointMap.put("cn-hongkong", "http://oss-cn-hongkong.aliyuncs.com");
		endpointMap.put("cn-shenzhen", "http://oss-cn-shenzhen.aliyuncs.com");
		endpointMap.put("us-west-1", "http://oss-us-west-1.aliyuncs.com");
		endpointMap.put("ap-southeast-1", "http://oss-ap-southeast-1.aliyuncs.com");
	}

	private ClientBuilderConfiguration configuration;

	private String accessKeyId;

	private String secretAccessKey;

	private String region;

	private String endpoint;

	// support ram sts
	private String securityToken;

	public ClientBuilderConfiguration getConfiguration() {
		return configuration;
	}

	public void setConfiguration(ClientBuilderConfiguration configuration) {
		this.configuration = configuration;
	}

	public String getAccessKeyId() {
		return accessKeyId;
	}

	public void setAccessKeyId(String accessKeyId) {
		this.accessKeyId = accessKeyId;
	}

	public String getSecretAccessKey() {
		return secretAccessKey;
	}

	public void setSecretAccessKey(String secretAccessKey) {
		this.secretAccessKey = secretAccessKey;
	}

	public String getEndpoint() {
		return endpoint;
	}

	public void setEndpoint(String endpoint) {
		this.endpoint = endpoint;
	}

	public String getSecurityToken() {
		return securityToken;
	}

	public void setSecurityToken(String securityToken) {
		this.securityToken = securityToken;
	}

	public String getRegion() {
		return region;
	}

	public void setRegion(String region) {
		if (!endpointMap.containsKey(region)) {
			String errorStr = "error region: " + region + ", please choose from "
					+ Arrays.toString(endpointMap.keySet().toArray());
			logger.error(errorStr);
			throw new IllegalArgumentException(errorStr);
		}
		this.region = region;
		this.setEndpoint(endpointMap.get(region));
	}
}
