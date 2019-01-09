/*
 * Copyright (C) 2019 the original author or authors.
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
package org.springframework.cloud.alicloud.context.sms;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.alicloud.context.AliCloudProperties;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

import java.io.Serializable;

/**
 * @author pbting
 */
@ConfigurationProperties(prefix = "spring.cloud.alicloud.sms")
public class SmsConfigProperties implements Serializable {

	// 产品名称:云通信短信API产品,开发者无需替换
	public static final String smsProduct = "Dysmsapi";
	// 产品域名,开发者无需替换
	public static final String smsDomain = "dysmsapi.aliyuncs.com";

	private AliCloudProperties aliCloudProperties;

	/**
	 *
	 */
	private String reportQueueName;
	/**
	 *
	 */
	private String upQueueName;

	/**
	 *
	 */
	protected String connnectTimeout = "10000";

	/**
	 *
	 */
	protected String readTimeout = "10000";

	public SmsConfigProperties(AliCloudProperties aliCloudProperties) {
		this.aliCloudProperties = aliCloudProperties;
	}

	public String getConnnectTimeout() {
		return connnectTimeout;
	}

	public void setConnnectTimeout(String connnectTimeout) {
		this.connnectTimeout = connnectTimeout;
	}

	public String getReadTimeout() {
		return readTimeout;
	}

	public void setReadTimeout(String readTimeout) {
		this.readTimeout = readTimeout;
	}

	public void overiideFromEnv(Environment environment) {
		overiideCustomFromEnv(environment);
		if (StringUtils.isEmpty(connnectTimeout)) {
			String resolveResult = environment.resolveRequiredPlaceholders(
					"${spring.cloud.alibaba.sms.connect-timeout:}");
			this.setConnnectTimeout(
					StringUtils.isEmpty(resolveResult) ? "10000" : resolveResult);
		}

		if (StringUtils.isEmpty(readTimeout)) {
			String resolveResult = environment.resolveRequiredPlaceholders(
					"${spring.cloud.alibaba.sms.read-timeout:}");
			this.setReadTimeout(
					StringUtils.isEmpty(resolveResult) ? "10000" : resolveResult);
		}
	}

	public void overiideCustomFromEnv(Environment environment) {
		// nothing to do
	}

	public String getReportQueueName() {
		return reportQueueName;
	}

	public void setReportQueueName(String reportQueueName) {
		this.reportQueueName = reportQueueName;
	}

	public String getUpQueueName() {
		return upQueueName;
	}

	public String getAccessKeyId() {
		return aliCloudProperties.getAccessKey();
	}

	public String getAccessKeySecret() {
		return aliCloudProperties.getSecretKey();
	}

	public void setUpQueueName(String upQueueName) {
		this.upQueueName = upQueueName;
	}

}