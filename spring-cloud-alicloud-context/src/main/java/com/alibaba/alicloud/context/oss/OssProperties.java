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

package com.alibaba.alicloud.context.oss;

import com.alibaba.cloud.context.AliCloudAuthorizationMode;
import com.aliyun.oss.ClientBuilderConfiguration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * {@link ConfigurationProperties} for configuring OSS.
 *
 * @author <a href="mailto:fangjian0423@gmail.com">Jim</a>
 * @author xiaolongzuo
 */
@ConfigurationProperties("spring.cloud.alicloud.oss")
public class OssProperties {

	/**
	 * Authorization Mode, please see <a href=
	 * "https://help.aliyun.com/document_detail/32010.html?spm=a2c4g.11186623.6.659.29f145dc3KOwTh">oss
	 * docs</a>.
	 */
	@Value("${spring.cloud.alicloud.oss.authorization-mode:AK_SK}")
	private AliCloudAuthorizationMode authorizationMode;

	/**
	 * Endpoint, please see <a href=
	 * "https://help.aliyun.com/document_detail/32010.html?spm=a2c4g.11186623.6.659.29f145dc3KOwTh">oss
	 * docs</a>.
	 */
	private String endpoint;

	/**
	 * Sts token, please see <a href=
	 * "https://help.aliyun.com/document_detail/32010.html?spm=a2c4g.11186623.6.659.29f145dc3KOwTh">oss
	 * docs</a>.
	 */
	private StsToken sts;

	/**
	 * Client Configuration, please see <a href=
	 * "https://help.aliyun.com/document_detail/32010.html?spm=a2c4g.11186623.6.659.29f145dc3KOwTh">oss
	 * docs</a>.
	 */
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
