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

package org.springframework.cloud.alicloud.context.oss;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

import com.alibaba.cloud.context.AliCloudAuthorizationMode;
import com.aliyun.oss.ClientBuilderConfiguration;

/**
 * {@link ConfigurationProperties} for configuring OSS.
 *
 * @author <a href="mailto:fangjian0423@gmail.com">Jim</a>
 * @author xiaolongzuo
 */
@ConfigurationProperties("spring.cloud.alicloud.oss")
public class OssProperties {

	@Value("${spring.cloud.alicloud.oss.authorization-mode:AK_SK}")
	private AliCloudAuthorizationMode authorizationMode;

	private String endpoint;

	private StsToken sts;

	private ClientBuilderConfiguration config;

	public AliCloudAuthorizationMode getAuthorizationMode() {
		return authorizationMode;
	}

	public void setAuthorizationMode(AliCloudAuthorizationMode authorizationMode) {
		this.authorizationMode = authorizationMode;
	}

	public ClientBuilderConfiguration getConfig() {
		return config;
	}

	public void setConfig(ClientBuilderConfiguration config) {
		this.config = config;
	}

	public String getEndpoint() {
		return endpoint;
	}

	public void setEndpoint(String endpoint) {
		this.endpoint = endpoint;
	}

	public StsToken getSts() {
		return sts;
	}

	public void setSts(StsToken sts) {
		this.sts = sts;
	}

	public static class StsToken {

		private String accessKey;

		private String secretKey;

		private String securityToken;

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

		public String getSecurityToken() {
			return securityToken;
		}

		public void setSecurityToken(String securityToken) {
			this.securityToken = securityToken;
		}

	}

}
